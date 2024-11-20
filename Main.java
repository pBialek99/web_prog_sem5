import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    private static final String DB = "jdbc:sqlite:C:/Users/Krem/Tools/sqlite/chinook.db";
    private static final int MAX_DEPTH = 3;
    private static final int THREAD_POOL = 10;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL);
        Set<Future<?>> tasks = new HashSet<>();

        try (Connection connection = connect()) {
            createTable(connection);
            insertRow(connection, "https://ii.up.krakow.pl", 0, 0);

            while (!tasks.isEmpty() || getNotSeen(connection) != null) {
                String nextUrl = getNotSeen(connection);

                if (nextUrl != null) {
                    int currentDepth = getDepth(connection, nextUrl);

                    if (currentDepth < MAX_DEPTH) {
                        Runnable crawlerTask = () -> crawl(connection, nextUrl, currentDepth + 1, MAX_DEPTH);
                        tasks.add(executor.submit(crawlerTask));
                    }

                    updateSeen(connection, nextUrl);
                }

                tasks.removeIf(Future::isDone);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB);
    }

    private static void createTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS urls ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "url TEXT NOT NULL UNIQUE, "
                + "depth INTEGER NOT NULL DEFAULT 0, "
                + "seen INTEGER NOT NULL DEFAULT 0);";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void insertRow(Connection connection, String url, int depth, int seen) throws SQLException {
        String sql = "INSERT OR IGNORE INTO urls (url, depth, seen) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, url);
            stmt.setInt(2, depth);
            stmt.setInt(3, seen);
            stmt.executeUpdate();
        }
    }

    private static void updateSeen(Connection connection, String url) throws SQLException {
        String sql = "UPDATE urls SET seen = 1 WHERE url = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, url);
            stmt.executeUpdate();
        }
    }

    private static String getNotSeen(Connection connection) throws SQLException {
        String sql = "SELECT url FROM urls WHERE seen = 0 LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("url");
            }
        }
        return null;
    }

    private static int getDepth(Connection connection, String url) throws SQLException {
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

    private static boolean isValid(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private static void crawl(Connection connection, String url, int depth, int maxDepth) {
        if (depth > maxDepth) {
            return;
        }

        try {
            Document doc = Jsoup.connect(url).timeout(10000).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String absUrl = link.absUrl("href");

                if (!isValid(absUrl)) {
                    continue;
                }

                insertRow(connection, absUrl, depth, 0);
                System.out.println(Thread.currentThread().getName() + ": " + absUrl);
            }
        } catch (IOException | SQLException e) {
            System.err.println(Thread.currentThread().getName() + " - Error: " + url);
            System.err.println(e.getMessage());
        }
    }
}
