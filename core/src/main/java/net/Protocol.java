package net;

public final class Protocol {

    public static final int DEFAULT_PORT = 7331;
    public static final String DEFAULT_HOST = "127.0.0.1";

    public static final String REQ = "id";
    public static final String OK = "ok";
    public static final String MESSAGE = "msg";

    public static final String SIGNUP = "signup";
    public static final String LOGIN = "login";
    public static final String LOGOUT = "logout";
    public static final String SECURITY_QUESTION = "security-question";
    public static final String RESET_PASSWORD = "reset-password";
    public static final String WHO_IS_ONLINE = "who";

    public static final String FILE_READ = "file-read";
    public static final String FILE_WRITE = "file-write";
    public static final String FILE_LIST = "file-list";
    public static final String FILE_RENAME = "file-rename";
    public static final String FILE_DELETE = "file-delete";
    public static final String FILE_EXISTS = "file-exists";

    public static final String LEADERBOARD = "leaderboard";
    public static final String SUBMIT_SCORE = "submit-score";

    public static final String INVITE = "invite";
    public static final String INVITE_OFFER = "invite-offer";
    public static final String INVITE_ANSWER = "invite-answer";
    public static final String INVITE_CANCELLED = "invite-cancelled";
    public static final String QUEUE_JOIN = "queue-join";
    public static final String QUEUE_LEAVE = "queue-leave";

    public static final String MATCH_START = "match-start";
    public static final String MATCH_STATE = "match-state";
    public static final String MATCH_OVER = "match-over";
    public static final String MATCH_LEAVE = "match-leave";
    public static final String MATCH_INTENT = "match-intent";
    public static final String REACTION = "reaction";
    public static final String REACTION_IN = "reaction-in";

    public static final String ROLE_PLANTS = "plants";
    public static final String ROLE_ZOMBIES = "zombies";

    public static final String INTENT_PLANT = "plant";
    public static final String INTENT_ZOMBIE = "zombie";
    public static final String INTENT_SUN = "sun";
    public static final String INTENT_SHOVEL = "shovel";

    private Protocol() {
    }
}
