package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DatabaseSeeder {

    public static void seedPlantsFromCSV(String filePath) {
        String sql = "INSERT IGNORE INTO static_plants (id, name, category, tags, cost, base_hp, base_damage, base_ability, plant_food_effect, lvl_2_upgrade, lvl_3_upgrade, lvl_4_upgrade, action_interval, recharge_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isHeader = true;

            // Note: Simple split setup. Assumes the CSV is clean and has no commas inside unescaped text fields.
            // If descriptions contain commas, a robust CSV library (like OpenCSV) is required here.
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Regex to split by comma, ignoring commas inside quotes

                stmt.setInt(1, Integer.parseInt(data[0].trim()));
                stmt.setString(2, data[1].trim().replace("\"", ""));
                stmt.setString(3, data[2].trim().replace("\"", ""));
                stmt.setString(4, data[3].trim().replace("\"", ""));
                stmt.setInt(5, Integer.parseInt(data[4].trim()));
                stmt.setInt(6, Integer.parseInt(data[5].trim()));
                stmt.setString(7, data[6].trim().replace("\"", ""));
                stmt.setString(8, data[7].trim().replace("\"", ""));
                stmt.setString(9, data[8].trim().replace("\"", ""));
                stmt.setString(10, data[9].trim().replace("\"", ""));
                stmt.setString(11, data[10].trim().replace("\"", ""));
                stmt.setString(12, data[11].trim().replace("\"", ""));
                stmt.setString(13, data[12].trim().replace("\"", ""));
                stmt.setInt(14, Integer.parseInt(data[13].trim()));

                stmt.addBatch();
            }

            stmt.executeBatch();
            System.out.println("Static Plant database successfully seeded from CSV.");

        } catch (Exception e) {
            System.err.println("Failed to seed plants from CSV: " + e.getMessage());
        }
    }
}