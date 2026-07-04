package database;

import models.enums.DifficultyLevel;
import models.enums.Gender;
import models.user.SecurityQuestion;
import models.user.User;
import utils.FileStore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class UserDAO {
    private static final String USERS_DIR = "users";

    private static String fileFor(String username) {
        return USERS_DIR + "/" + username + ".properties";
    }

    /** Insert a new user. Returns true if successful. */
    public boolean insertUser(User user) {
        if (existsByUsername(user.getUsername())) {
            return false;
        }
        return writeUser(user);
    }

    private boolean writeUser(User user) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("username", user.getUsername());
        values.put("passwordHash", user.getPasswordHash());
        values.put("nickname", user.getNickname());
        values.put("email", user.getEmail());
        values.put("gender", user.getGender() == null ? "PREFER NOT TO SAY" : user.getGender().toString());
        values.put("difficulty", String.valueOf(user.getDifficultyLevel().getLevelNumber()));
        values.put("coins", String.valueOf(user.getCoins().getAmount()));
        values.put("diamonds", String.valueOf(user.getDiamonds().getAmount()));
        values.put("pots", String.valueOf(user.getPots().getAmount()));
        values.put("securityQuestion", user.getSecurityQuestion().getQuestion());
        values.put("securityAnswer", user.getSecurityQuestion().getAnswer());
        values.put("highestScore", String.valueOf(user.getHighestScore()));
        values.put("numberOfGames", String.valueOf(user.getNumberOfGames()));
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            lines.add(entry.getKey() + "=" + entry.getValue());
        }
        return FileStore.writeLines(fileFor(user.getUsername()), lines);
    }

    /** Find user by username. Returns null if not found. */
    public User findByUsername(String username) {
        if (username == null || !FileStore.exists(fileFor(username))) {
            return null;
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : FileStore.readLines(fileFor(username))) {
            int sep = line.indexOf('=');
            if (sep > 0) {
                values.put(line.substring(0, sep), line.substring(sep + 1));
            }
        }
        return mapToUser(values);
    }

    private User mapToUser(Map<String, String> values) {
        User user = new User();
        user.setUsername(values.get("username"));
        user.setPasswordHash(values.get("passwordHash"));
        user.setNickname(values.get("nickname"));
        user.setEmail(values.get("email"));
        user.setGender(Gender.getByName(values.getOrDefault("gender", "")));
        user.setDifficultyLevel(DifficultyLevel.getDifficultyByLevel(
                parseInt(values.get("difficulty"), 3)));
        user.getCoins().setAmount(parseInt(values.get("coins"), 0));
        user.getDiamonds().setAmount(parseInt(values.get("diamonds"), 0));
        user.getPots().setAmount(parseInt(values.get("pots"), 0));
        user.setSecurityQuestion(new SecurityQuestion(
                values.getOrDefault("securityQuestion", ""),
                values.getOrDefault("securityAnswer", "")));
        user.setHighestScore(parseInt(values.get("highestScore"), 0));
        user.setNumberOfGames(parseInt(values.get("numberOfGames"), 0));
        return user;
    }

    private static int parseInt(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    /** Update user's password. */
    public boolean updatePassword(String username, String newHashedPassword) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        user.setPasswordHash(newHashedPassword);
        return writeUser(user);
    }

    /** Update profile fields that can be changed (username, nickname, email). */
    public boolean updateProfile(String oldUsername, String newUsername, String nickname, String email) {
        User user = findByUsername(oldUsername);
        if (user == null) {
            return false;
        }
        user.setUsername(newUsername);
        user.setNickname(nickname);
        user.setEmail(email);
        if (!oldUsername.equals(newUsername)) {
            utils.UserDataStore.evict(oldUsername);
            FileStore.rename(fileFor(oldUsername), fileFor(newUsername));
            FileStore.rename("user_" + oldUsername + ".properties",
                    "user_" + newUsername + ".properties");
            FileStore.rename("news_" + oldUsername + ".txt", "news_" + newUsername + ".txt");
        }
        return writeUser(user);
    }

    /** Update coins, diamonds, pots counts. */
    public boolean updateCurrencies(String username, int coins, int diamonds, int pots) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        user.getCoins().setAmount(coins);
        user.getDiamonds().setAmount(diamonds);
        user.getPots().setAmount(pots);
        return writeUser(user);
    }

    /** Update difficulty level. */
    public boolean updateDifficulty(String username, DifficultyLevel level) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        user.setDifficultyLevel(level);
        return writeUser(user);
    }

    /** Update highest score (miopoint). */
    public boolean updateHighestScore(String username, int score) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        user.setHighestScore(score);
        return writeUser(user);
    }

    /** Increment the played-games counter shown in the profile. */
    public boolean incrementGamesPlayed(String username) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        user.setNumberOfGames(user.getNumberOfGames() + 1);
        return writeUser(user);
    }

    /** Check if a username exists. */
    public boolean existsByUsername(String username) {
        return FileStore.exists(fileFor(username));
    }

    /** Get user's security question (for password reset). */
    public SecurityQuestion getSecurityQuestion(String username) {
        User user = findByUsername(username);
        return user == null ? null : user.getSecurityQuestion();
    }

    /** Loads all users (for the leaderboard). */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for (String fileName : FileStore.listFiles(USERS_DIR)) {
            if (fileName.endsWith(".properties")) {
                User user = findByUsername(fileName.substring(0, fileName.length() - ".properties".length()));
                if (user != null) {
                    users.add(user);
                }
            }
        }
        return users;
    }
}
