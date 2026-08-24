package server;

import database.UserDAO;
import models.enums.DifficultyLevel;
import models.enums.Gender;
import models.enums.regexes.Authentication;
import models.user.SecurityQuestion;
import models.user.User;
import net.Packet;
import net.Protocol;
import utils.PasswordHasher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class AccountService {

    private static final Set<String> GUEST_ROUTES = Set.of(
            Protocol.SIGNUP, Protocol.LOGIN, Protocol.SECURITY_QUESTION, Protocol.RESET_PASSWORD);

    private final GameServer server;
    private final UserDAO users = new UserDAO();
    private final Map<Long, User> pending = new ConcurrentHashMap<>();

    public AccountService(GameServer server) {
        this.server = server;
    }

    public boolean isOpenToGuests(String type) {
        return GUEST_ROUTES.contains(type);
    }

    public void register(Map<String, BiConsumer<ClientSession, Packet>> routes) {
        routes.put(Protocol.SIGNUP, this::signup);
        routes.put(Protocol.LOGIN, this::login);
        routes.put(Protocol.LOGOUT, this::logout);
        routes.put(Protocol.SECURITY_QUESTION, this::securityQuestion);
        routes.put(Protocol.RESET_PASSWORD, this::resetPassword);
        routes.put(Protocol.WHO_IS_ONLINE, this::who);
    }

    private void signup(ClientSession session, Packet request) {
        if ("finish".equals(request.str("step"))) {
            finishSignup(session, request);
            return;
        }
        String username = request.str("username");
        String problem = validate(username, request.str("password"),
                request.str("confirm"), request.str("nickname"),
                request.str("email"), request.str("gender"));
        if (problem != null) {
            session.deny(request, problem);
            return;
        }
        User draft = new User();
        draft.setUsername(username);
        draft.setPasswordHash(PasswordHasher.hash(request.str("password")));
        draft.setNickname(request.str("nickname"));
        draft.setEmail(request.str("email"));
        draft.setGender(Gender.getByName(request.str("gender")));
        pending.put(session.id(), draft);
        session.ok(request);
    }

    private String validate(String username, String password, String confirm,
                            String nickname, String email, String gender) {
        if (!Authentication.USERNAME.matches(username)) {
            return Authentication.USERNAME.getErrorMessage();
        }
        if (users.existsByUsername(username)) {
            return "That username is already taken.";
        }
        if (password == null || !password.equals(confirm)) {
            return "Passwords do not match.";
        }
        if (!Authentication.PASSWORD.matches(password)) {
            return Authentication.PASSWORD.getErrorMessage();
        }
        if (nickname == null || nickname.length() < 3 || nickname.length() > 30) {
            return "Nickname must be between 3 and 30 characters.";
        }
        if (!Authentication.EMAIL.matches(email)) {
            return Authentication.EMAIL.getErrorMessage();
        }
        if (!"male".equalsIgnoreCase(gender) && !"female".equalsIgnoreCase(gender)) {
            return "Gender must be 'male' or 'female'.";
        }
        return null;
    }

    private void finishSignup(ClientSession session, Packet request) {
        User draft = pending.remove(session.id());
        if (draft == null) {
            session.deny(request, "Start the sign-up again — the server has no details for you.");
            return;
        }
        String answer = request.str("answer");
        if (answer == null || answer.isBlank() || !answer.equals(request.str("confirm"))) {
            session.deny(request, "Answers do not match.");
            return;
        }
        if (users.existsByUsername(draft.getUsername())) {
            session.deny(request, "That username was taken while you were signing up.");
            return;
        }
        draft.setDifficultyLevel(DifficultyLevel.MEDIUM);
        draft.setHighestScore(0);
        draft.setSecurityQuestion(new SecurityQuestion(request.str("question"), answer));
        if (!users.insertUser(draft)) {
            session.deny(request, "The server could not save the account.");
            return;
        }
        Log.say("New account: " + draft.getUsername() + ".");
        session.ok(request);
    }

    private void login(ClientSession session, Packet request) {
        String username = request.str("username");
        User user = users.findByUsername(username);
        if (user == null) {
            session.deny(request, "Username does not exist.");
            return;
        }
        if (!PasswordHasher.hash(request.str("password")).equals(user.getPasswordHash())) {
            session.deny(request, "Incorrect password.");
            return;
        }
        session.setUsername(user.getUsername());
        server.bind(user.getUsername(), session);
        Log.say(user.getUsername() + " signed in from " + session.where() + ".");
        session.ok(request, Packet.of(request.type()).put("username", user.getUsername()));
    }

    private void logout(ClientSession session, Packet request) {
        session.close();
    }

    private void securityQuestion(ClientSession session, Packet request) {
        SecurityQuestion question = users.getSecurityQuestion(request.str("username"));
        if (question == null) {
            session.deny(request, "Username does not exist.");
            return;
        }
        session.ok(request, Packet.of(request.type()).put("question", question.getQuestion()));
    }

    private void resetPassword(ClientSession session, Packet request) {
        String username = request.str("username");
        SecurityQuestion question = users.getSecurityQuestion(username);
        if (question == null) {
            session.deny(request, "Username does not exist.");
            return;
        }
        if (!question.getAnswer().equals(request.str("answer"))) {
            session.deny(request, "That is not the right answer.");
            return;
        }
        String password = request.str("password");
        if (!Authentication.PASSWORD.matches(password)) {
            session.deny(request, Authentication.PASSWORD.getErrorMessage());
            return;
        }
        users.updatePassword(username, PasswordHasher.hash(password));
        session.ok(request);
    }

    private void who(ClientSession session, Packet request) {
        List<Object> names = new ArrayList<>();
        for (String name : server.onlineNames()) {
            if (!name.equals(session.username())) {
                names.add(name);
            }
        }
        session.ok(request, Packet.of(request.type()).put("names", names));
    }
}
