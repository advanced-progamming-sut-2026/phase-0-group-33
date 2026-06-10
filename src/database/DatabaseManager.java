package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;

    private static final String URL      = "jdbc:mysql://89.32.248.183:3306/PVZ2?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "pvz2";
    private static final String PASSWORD = "Admin@1234";

    private DatabaseManager() {

    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}