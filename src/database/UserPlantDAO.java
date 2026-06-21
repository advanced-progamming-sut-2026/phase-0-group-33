package database;

import models.entities.plant.StaticPlant;
import models.user.UserPlantDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class UserPlantDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private final StaticPlantDAO staticPlantDAO = new StaticPlantDAO();

    public boolean unlockPlant(String username, int plantId) {
        String sql = "INSERT INTO user_plants (username, plant_id, upgrade_level) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE upgrade_level = upgrade_level";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, plantId);
            return stmt.executeUpdate() >= 0; // >= 0 because duplicate key returns 0 update count
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean upgradePlant(String username, int plantId) {
        String sql = "UPDATE user_plants SET upgrade_level = upgrade_level + 1 WHERE username = ? AND plant_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, plantId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UserPlantDTO> getUserPlantsWithStats(String username) {
        List<UserPlantDTO> unlockedPlants = new ArrayList<>();
        String sql = "SELECT up.upgrade_level, sp.* " +
                "FROM user_plants up " +
                "JOIN static_plants sp ON up.plant_id = sp.id " +
                "WHERE up.username = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UserPlantDTO dto = new UserPlantDTO();
                    dto.setUsername(username);
                    dto.setUpgradeLevel(rs.getInt("upgrade_level"));

                    // Utilize the mapping logic from the static DAO
                    StaticPlant basePlant = staticPlantDAO.mapRowToStaticPlant(rs);
                    dto.setBasePlant(basePlant);

                    unlockedPlants.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return unlockedPlants;
    }
}