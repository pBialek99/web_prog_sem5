package net.webcrawler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WebCrawler {
    
    private final DBConn conn;
    private final ExecutorService executor;

    public WebCrawler(int threads) {
        
        this.conn = new DBConn();
        this.executor = Executors.newFixedThreadPool(threads);
    }

    private String getUnvisited() {
        
        String sql = "SELECT url FROM urls WHERE seen = 0 LIMIT 1";
        try (Statement stmt = conn.connect().createStatement();
             ResultSet r = stmt.executeQuery(sql)) {
            if (r.next()) {
                return r.getString("url");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void startCrawler(String start) {
        
        conn.connect();
        conn.createTable();
        conn.insertRow(start, 0);
        
        List<Future<Void>> tasks = new ArrayList<>();

        while (true) {
            String visit = getUnvisited();

            if (visit == null) {
                break;
            }

            CrawlerThread task = new CrawlerThread(visit, conn);
            tasks.add(executor.submit(task));
        }

        for (Future<Void> t : tasks) {
            try {
                t.get();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        executor.shutdown();
        conn.disconnect();
    }

    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler(10);
        crawler.startCrawler("https://ii.up.krakow.pl");
    }
}
