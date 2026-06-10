package database;

import models.user.SecurityQuestion;
import models.user.User;
import java.sql.*;

public class UserDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    /** Insert a new user. Returns true if successful. */
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, nickname, email, gender, " +
                "difficulty_level, coins, diamonds, pots, security_question, security_answer, " +
                "highest_score) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getNickname());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getGenderString());
            stmt.setInt(6, user.getDifficultyLevelValue());
            stmt.setInt(7, user.getCoins().getAmount());
            stmt.setInt(8, user.getDiamonds().getAmount());
            stmt.setInt(9, user.getPots().getAmount());
            stmt.setString(10, user.getSecurityQuestion().getQuestion());
            stmt.setString(11, user.getSecurityQuestion().getAnswer());
            stmt.setInt(12, user.getHighestScore());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Find user by username. Returns null if not found. */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Update user's password. */
    public boolean updatePassword(String username, String newHashedPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Update profile fields that can be changed (username, nickname, email). */
    public boolean updateProfile(String oldUsername, String newUsername, String nickname, String email) {
        String sql = "UPDATE users SET username = ?, nickname = ?, email = ? WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newUsername);
            stmt.setString(2, nickname);
            stmt.setString(3, email);
            stmt.setString(4, oldUsername);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Update coins, diamonds, pots counts. */
    public boolean updateCurrencies(String username, int coins, int diamonds, int pots) {
        String sql = "UPDATE users SET coins = ?, diamonds = ?, pots = ? WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, coins);
            stmt.setInt(2, diamonds);
            stmt.setInt(3, pots);
            stmt.setString(4, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Update difficulty level. */
    public boolean updateDifficulty(String username, int level) {
        String sql = "UPDATE users SET difficulty_level = ? WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, level);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Update highest score. */
    public boolean updateHighestScore(String username, int score) {
        String sql = "UPDATE users SET highest_score = ? WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, score);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Check if a username exists. */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Get user's security question (for password reset). */
    public SecurityQuestion getSecurityQuestion(String username) {
        String sql = "SELECT security_question, security_answer FROM users WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new SecurityQuestion(rs.getString("security_question"),
                        rs.getString("security_answer"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Helper to map a ResultSet row to a User object. */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));  // stored hash
        user.setNickname(rs.getString("nickname"));
        user.setEmail(rs.getString("email"));
        user.setGender(rs.getString("gender"));
        user.setDifficultyLevel(rs.getInt("difficulty_level"));
        user.getCoins().setAmount(rs.getInt("coins"));
        user.getDiamonds().setAmount(rs.getInt("diamonds"));
        user.getPots().setAmount(rs.getInt("pots"));
        user.setSecurityQuestion(new SecurityQuestion(
                rs.getString("security_question"),
                rs.getString("security_answer")));
        user.setHighestScore(rs.getInt("highest_score"));
        return user;
    }
}