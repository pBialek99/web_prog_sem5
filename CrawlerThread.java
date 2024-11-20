import org.jsoup.*;
import java.io.IOException;
import java.sql.*;

import DBConn;

public class CrawlerThread implements Runnable {
    private final Connection connection;
    private final String url;
    private final int depth;
    private final int maxDepth;

    public CrawlerThread(Connection connection, String url, int depth, int maxDepth) {
        this.connection = connection;
        this.url = url;
        this.depth = depth;
        this.maxDepth = maxDepth;
    }

    private static boolean isValid(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    @Override
    public void run() {
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

                DBConn.insertRow(connection, absUrl, depth, 0);
                System.out.println(Thread.currentThread().getName() + ": " + absUrl);
            }
        } catch (IOException | SQLException e) {
            System.err.println(Thread.currentThread().getName() + " - Error: " + url);
            System.err.println(e.getMessage());
        }
    }
}
