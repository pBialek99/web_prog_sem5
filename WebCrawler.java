import java.sql.*;
import java.util.*;

public class WebCrawler {
    private static final int MAX_DEPTH = 3;
    private static final int THREAS = 10;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        Set<Future<?>> tasks = new HashSet<>();

        try (Connection connection = DBConn.connect()) {
            DBConn.createTable(connection);
            DBConn.insertRow(connection, "https://ii.up.krakow.pl", 0, 0);

            while (!tasks.isEmpty() || DBConn.getNotSeen(connection) != null) {
                String nextUrl = DBConn.getNotSeen(connection);

                if (nextUrl != null) {
                    int currentDepth = DBConn.getDepth(connection, nextUrl);

                    if (currentDepth < MAX_DEPTH) {
                        Runnable crawlerTask = new CrawlerThread(connection, nextUrl, currentDepth + 1, MAX_DEPTH);
                        tasks.add(executor.submit(crawlerTask));
                    }

                    DBConn.updateSeen(connection, nextUrl);
                }

                tasks.removeIf(Future::isDone);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
