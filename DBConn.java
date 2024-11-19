package net.webcrawler;

import java.sql.*;

public class DBConn {
    private Connection db;
    
    // connection to DB
    public Connection connect() {
        String url = "jdbc:sqlite:C:/Users/Krem/Tools/sqlite/chinook.db";

        // connect to DB only if not already connected
        if (db == null) {
            try {
                db = DriverManager.getConnection(url);
                // System.out.println("Connected to SQLite.");
                
                return db;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            
                return null;
            }
        }
    }

    // table creation
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS urls ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "url TEXT NOT NULL,"
                + "seen INTEGER NOT NULL"
                + ")";

        try (Statement stmt = db.createStatement()) {
            stmt.execute(sql);
            // System.out.println("Table created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // row insertion
    public void insertRow(String url, int seen) {
        String sqlInsert = "INSERT OR IGNORE INTO urls (url, seen) VALUES (?, ?)";
        String sqlUpdate = "UPDATE urls SET seen = seen + 1 WHERE url = ?";
    
        try (PreparedStatement insertStmt = db.prepareStatement(sqlInsert); PreparedStatement updateStmt = db.prepareStatement(sqlUpdate)) {
            insertStmt.setString(1, url);
            insertStmt.setInt(2, seen);
            insertStmt.executeUpdate();
    
            if (seen > 0) {
                updateStmt.setString(1, url);
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // table dropping
    public void dropTable() {
        String sql = "DROP TABLE IF EXISTS urls";

        try (Statement stmt = db.createStatement()) {
            stmt.execute(sql);
            // System.out.println("Table dropped.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // disconnect DB
    public void disconnect() {
        try {
            if (db != null && !db.isClosed()) {
                db.close();
                db = null;
                // System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
