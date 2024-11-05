package net.webcrawler;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DBConn {
    private Connection conn;

    // connection to DB
    public Connection connect() {
        String url = "jdbc:sqlite:C:/Users/Krem/Tools/sqlite/chinook.db";

        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connected to SQLite.");
            return conn;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // table creation
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS urls ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "url TEXT NOT NULL,"
                + "seen INTEGER NOT NULL"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // row insertion
    public void insertRow(String url, int seen) {

        String sql = "INSERT INTO urls (url, seen) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, url);
            stmt.setInt(2, seen);
            stmt.executeUpdate();
            System.out.println("Inserted row.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // table dropping
    public void dropTable() {
        String sql = "DROP TABLE IF EXISTS urls";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table dropped.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // disconnect DB
    public void disconnect() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                conn = null;
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
