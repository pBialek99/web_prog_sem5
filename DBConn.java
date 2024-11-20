import java.sql.*;

public class DBConn {
    private static final String DB = "jdbc:sqlite:C:/Users/Krem/Tools/sqlite/chinook.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB);
    }

    public static void createTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS urls ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "url TEXT NOT NULL UNIQUE, "
                + "depth INTEGER NOT NULL DEFAULT 0, "
                + "seen INTEGER NOT NULL DEFAULT 0);";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static void insertRow(Connection connection, String url, int depth, int seen) throws SQLException {
        String sql = "INSERT OR IGNORE INTO urls (url, depth, seen) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, url);
            stmt.setInt(2, depth);
            stmt.setInt(3, seen);
            stmt.executeUpdate();
        }
    }

    public static void updateSeen(Connection connection, String url) throws SQLException {
        String sql = "UPDATE urls SET seen = 1 WHERE url = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, url);
            stmt.executeUpdate();
        }
    }

    public static String getNotSeen(Connection connection) throws SQLException {
        String sql = "SELECT url FROM urls WHERE seen = 0 LIMIT 1";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("url");
            }
        }
        return null;
    }

    public static int getDepth(Connection connection, String url) throws SQLException {
        String sql = "SELECT depth FROM urls WHERE url = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, url);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("depth");
            }
        }
        return 0;
    }
}
