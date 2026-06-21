package database;

import models.entities.plant.StaticPlant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class StaticPlantDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public StaticPlant getBasePlant(int id) {
        String sql = "SELECT * FROM static_plants WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStaticPlant(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected StaticPlant mapRowToStaticPlant(ResultSet rs) throws SQLException {
        StaticPlant p = new StaticPlant();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setTags(rs.getString("tags"));
        p.setCost(rs.getInt("cost"));
        p.setBaseHp(rs.getInt("base_hp"));
        p.setBaseDamage(rs.getString("base_damage"));
        p.setBaseAbility(rs.getString("base_ability"));
        p.setPlantFoodEffect(rs.getString("plant_food_effect"));
        p.setLvl2Upgrade(rs.getString("lvl_2_upgrade"));
        p.setLvl3Upgrade(rs.getString("lvl_3_upgrade"));
        p.setLvl4Upgrade(rs.getString("lvl_4_upgrade"));
        p.setActionInterval(rs.getString("action_interval"));
        p.setRechargeTime(rs.getInt("recharge_time"));
        return p;
    }
}