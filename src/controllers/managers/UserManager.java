package controllers.managers;

import database.UserDAO;
import models.enums.DifficultyLevel;
import models.enums.Gender;
import models.user.SecurityQuestion;
import models.user.User;
import utils.PasswordHasher;
import models.Result;
import models.enums.regexes.Authentication;

public class UserManager {
    private static UserManager instance;
    private final UserDAO userDAO;
    private User currentUser;

    // Pending registration data (step 1)
    private String pendingUsername;
    private String pendingPasswordHash;
    private String pendingNickname;
    private String pendingEmail;
    private Gender pendingGender;

    private UserManager() {
        userDAO = new UserDAO();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }

    public Result logout() {
        currentUser = null;
        Result r = new Result();
        r.setSuccess(true);
        r.addMessage("Logged out successfully.");
        return r;
    }

    // ─── REGISTRATION STEP 1 ──────────────────
    public Result registerUser(String username, String password, String passwordConfirm,
                               String nickname, String email, String gender) {
        Result result = new Result();

        if (!Authentication.USERNAME.matches(username)) {
            result.addMessage(Authentication.USERNAME.getErrorMessage());
            result.setSuccess(false);
            return result;
        }
        if (userDAO.existsByUsername(username)) {
            result.addMessage("Username already exists.");
            result.setSuccess(false);
            return result;
        }
        if (!password.equals(passwordConfirm)) {
            result.addMessage("Passwords do not match.");
            result.setSuccess(false);
            return result;
        }
        if (!Authentication.PASSWORD.matches(password)) {
            result.addMessage(Authentication.PASSWORD.getErrorMessage());
            result.setSuccess(false);
            return result;
        }
        if (nickname.length() < 3 || nickname.length() > 30) {
            result.addMessage("Nickname must be between 3 and 30 characters.");
            result.setSuccess(false);
            return result;
        }
        if (!Authentication.EMAIL.matches(email)) {
            result.addMessage(Authentication.EMAIL.getErrorMessage());
            result.setSuccess(false);
            return result;
        }
        if (!gender.equalsIgnoreCase("male") && !gender.equalsIgnoreCase("female")) {
            result.addMessage("Gender must be 'male' or 'female'.");
            result.setSuccess(false);
            return result;
        }

        // Store pending data
        pendingUsername = username;
        pendingPasswordHash = PasswordHasher.hash(password);
        pendingNickname = nickname;
        pendingEmail = email;
        pendingGender = Gender.getByName(gender);

        result.setSuccess(true);
        result.addMessage("Validation passed. Please pick a security question.");
        return result;
    }

    // ─── REGISTRATION STEP 2 ──────────────────
    public Result completeRegistration(String question, String answer) {
        Result result = new Result();
        if (pendingUsername == null) {
            result.addMessage("No pending registration.");
            result.setSuccess(false);
            return result;
        }
        User user = new User();
        user.setUsername(pendingUsername);
        user.setPasswordHash(pendingPasswordHash);
        user.setNickname(pendingNickname);
        user.setEmail(pendingEmail);
        user.setGender(pendingGender);
        user.setDifficultyLevel(DifficultyLevel.MEDIUM);
        user.getCoins().setAmount(0);
        user.getDiamonds().setAmount(0);
        user.getPots().setAmount(0);
        user.setHighestScore(0);
        user.setSecurityQuestion(new SecurityQuestion(question, answer));

        if (userDAO.insertUser(user)) {
            clearPending();
            currentUser = user;
            result.setSuccess(true);
            result.addMessage("Account created successfully. You are now logged in.");
        } else {
            result.addMessage("Database error. Please try again.");
            result.setSuccess(false);
        }
        return result;
    }

    private void clearPending() {
        pendingUsername = null;
        pendingPasswordHash = null;
        pendingNickname = null;
        pendingEmail = null;
        pendingGender = null;
    }

    // ─── LOGIN ─────────────────────────────────
    public Result login(String username, String password) {
        Result result = new Result();
        User user = userDAO.findByUsername(username);
        if (user == null) {
            result.addMessage("Username does not exist.");
            result.setSuccess(false);
            return result;
        }
        String hashedInput = PasswordHasher.hash(password);
        if (!hashedInput.equals(user.getPasswordHash())) {
            result.addMessage("Incorrect password.");
            result.setSuccess(false);
            return result;
        }
        currentUser = user;
        result.setSuccess(true);
        result.addMessage("Login successful.");
        return result;
    }

    // ─── FORGOT PASSWORD ──────────────────────
    public Result getSecurityQuestionForUser(String username) {
        Result result = new Result();
        User user = userDAO.findByUsername(username);
        if (user == null) {
            result.addMessage("User not found.");
            result.setSuccess(false);
            return result;
        }
        result.setData(user.getSecurityQuestion());
        result.setSuccess(true);
        return result;
    }

    public Result verifySecurityAnswer(String username, String answer) {
        Result result = new Result();
        SecurityQuestion sq = (SecurityQuestion) getSecurityQuestionForUser(username).getData();
        if (sq == null) {
            result.addMessage("User not found.");
            result.setSuccess(false);
            return result;
        }
        if (sq.getAnswer().equalsIgnoreCase(answer)) {
            result.setSuccess(true);
            result.addMessage("Correct answer. You may now set a new password.");
        } else {
            result.addMessage("Wrong answer.");
            result.setSuccess(false);
        }
        return result;
    }

    public Result resetPassword(String username, String newPassword) {
        Result result = new Result();
        if (!Authentication.PASSWORD.matches(newPassword)) {
            result.addMessage(Authentication.PASSWORD.getErrorMessage());
            result.setSuccess(false);
            return result;
        }
        String hash = PasswordHasher.hash(newPassword);
        if (userDAO.updatePassword(username, hash)) {
            result.setSuccess(true);
            result.addMessage("Password updated. You can now log in.");
        } else {
            result.addMessage("Password update failed.");
            result.setSuccess(false);
        }
        return result;
    }

    // ─── PROFILE CHANGES ──────────────────────
    public Result changeUsername(String newUsername) {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        if (newUsername.equals(currentUser.getUsername())) {
            result.addMessage("New username is the same as the current one.");
            result.setSuccess(false);
            return result;
        }
        if (!Authentication.USERNAME.matches(newUsername)) {
            result.addMessage(Authentication.USERNAME.getErrorMessage());
            result.setSuccess(false);
            return result;
        }
        if (userDAO.existsByUsername(newUsername)) {
            result.addMessage("Username already taken.");
            result.setSuccess(false);
            return result;
        }
        if (userDAO.updateProfile(currentUser.getUsername(), newUsername,
                currentUser.getNickname(), currentUser.getEmail())) {
            currentUser.setUsername(newUsername);
            result.setSuccess(true);
            result.addMessage("Username changed.");
        } else {
            result.addMessage("Update failed.");
            result.setSuccess(false);
        }
        return result;
    }

    public Result changeNickname(String newNickname) {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        if (newNickname.equals(currentUser.getNickname())) {
            result.addMessage("New nickname is the same as the current one.");
            result.setSuccess(false);
            return result;
        }
        if (newNickname.length() < 3 || newNickname.length() > 30) {
            result.addMessage("Nickname must be 3-30 characters.");
            result.setSuccess(false);
            return result;
        }
        if (userDAO.updateProfile(currentUser.getUsername(), currentUser.getUsername(),
                newNickname, currentUser.getEmail())) {
            currentUser.setNickname(newNickname);
            result.setSuccess(true);
            result.addMessage("Nickname changed.");
        } else {
            result.addMessage("Update failed.");
            result.setSuccess(false);
        }
        return result;
    }

    public Result changeEmail(String newEmail) {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        if (newEmail.equals(currentUser.getEmail())) {
            result.addMessage("New email is the same.");
            result.setSuccess(false);
            return result;
        }
        if (!Authentication.EMAIL.matches(newEmail)) {
            result.addMessage(Authentication.EMAIL.getErrorMessage());
            result.setSuccess(false);
            return result;
        }
        if (userDAO.updateProfile(currentUser.getUsername(), currentUser.getUsername(),
                currentUser.getNickname(), newEmail)) {
            currentUser.setEmail(newEmail);
            result.setSuccess(true);
            result.addMessage("Email changed.");
        } else {
            result.addMessage("Update failed.");
            result.setSuccess(false);
        }
        return result;
    }

    public Result changePassword(String oldPassword, String newPassword) {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        if (!PasswordHasher.hash(oldPassword).equals(currentUser.getPasswordHash())) {
            result.addMessage("Old password is incorrect.");
            result.setSuccess(false);
            return result;
        }
        if (!Authentication.PASSWORD.matches(newPassword)) {
            result.addMessage(Authentication.PASSWORD.getErrorMessage());
            result.setSuccess(false);
            return result;
        }
        String hash = PasswordHasher.hash(newPassword);
        if (userDAO.updatePassword(currentUser.getUsername(), hash)) {
            currentUser.setPasswordHash(hash);
            result.setSuccess(true);
            result.addMessage("Password changed.");
        } else {
            result.addMessage("Update failed.");
            result.setSuccess(false);
        }
        return result;
    }

    public Result changeDifficulty(DifficultyLevel level) {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        if (level.getLevelNumber() < 1 || level.getLevelNumber() > 5) {
            result.addMessage("Difficulty must be 1-5.");
            result.setSuccess(false);
            return result;
        }
        if (userDAO.updateDifficulty(currentUser.getUsername(), level)) {
            currentUser.setDifficultyLevel(level);
            result.setSuccess(true);
            result.addMessage("Difficulty set to " + level + ".");
        } else {
            result.addMessage("Update failed.");
            result.setSuccess(false);
        }
        return result;
    }

    // ─── CURRENCIES ────────────────────────────
    public Result addCoins(int amount) {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        int newVal = currentUser.getCoins().getAmount() + amount;
        currentUser.getCoins().setAmount(newVal);
        syncCurrencies();
        result.setSuccess(true);
        result.addMessage(amount + " coins added. Total: " + newVal);
        return result;
    }

    public Result addDiamonds(int amount) {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        int newVal = currentUser.getDiamonds().getAmount() + amount;
        currentUser.getDiamonds().setAmount(newVal);
        syncCurrencies();
        result.setSuccess(true);
        result.addMessage(amount + " diamonds added. Total: " + newVal);
        return result;
    }

    public Result spendCoins(int amount) {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        int current = currentUser.getCoins().getAmount();
        if (current < amount) {
            result.addMessage("Not enough coins. You have " + current + ".");
            result.setSuccess(false);
            return result;
        }
        currentUser.getCoins().setAmount(current - amount);
        syncCurrencies();
        result.setSuccess(true);
        result.addMessage("Spent " + amount + " coins. Remaining: " + (current - amount));
        return result;
    }

    public Result spendDiamonds(int amount) {
        // similar logic
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        int current = currentUser.getDiamonds().getAmount();
        if (current < amount) {
            result.addMessage("Not enough diamonds. You have " + current + ".");
            result.setSuccess(false);
            return result;
        }
        currentUser.getDiamonds().setAmount(current - amount);
        syncCurrencies();
        result.setSuccess(true);
        result.addMessage("Spent " + amount + " diamonds. Remaining: " + (current - amount));
        return result;
    }

    private void syncCurrencies() {
        if (currentUser != null) {
            userDAO.updateCurrencies(currentUser.getUsername(),
                    currentUser.getCoins().getAmount(),
                    currentUser.getDiamonds().getAmount(),
                    currentUser.getPots().getAmount());
        }
    }

    public Result updateHighestScore(int score) {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        if (score > currentUser.getHighestScore()) {
            currentUser.setHighestScore(score);
            userDAO.updateHighestScore(currentUser.getUsername(), score);
            result.setSuccess(true);
            result.addMessage("New high score: " + score);
        } else {
            result.setSuccess(true);
            result.addMessage("Score not higher than current high score.");
        }
        return result;
    }

    public Result getUserInfo() {
        Result result = new Result();
        if (currentUser == null) {
            result.addMessage("Not logged in.");
            result.setSuccess(false);
            return result;
        }
        // Create a simple data object or use a Map
        String info = String.format(
                "Username: %s, Nickname: %s, Coins: %d, Diamonds: %d, Pots: %d, Difficulty: %s, High score: %d",
                currentUser.getUsername(),
                currentUser.getNickname(),
                currentUser.getCoins().getAmount(),
                currentUser.getDiamonds().getAmount(),
                currentUser.getPots().getAmount(),
                currentUser.getDifficultyLevel(),
                currentUser.getHighestScore()
        );
        result.setData(info);
        result.setSuccess(true);
        return result;
    }
}