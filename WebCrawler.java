package net.webcrawler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WebCrawler {
    private DBConn db;
    private ExecutorService executor;

    public WebCrawler(int threads) {
        this.db = new DBConn();
        this.executor = Executors.newFixedThreadPool(threads);
    }

    private String getNext() {
        String sql = "SELECT url FROM urls WHERE seen = 0 LIMIT 1";
        
        try (Statement stmt = db.connect().createStatement();
             ResultSet r = stmt.executeQuery(sql)) {
            
            if (r.next()) {
                return r.getString("url");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return null;
    }

    private void finishCrawler(List<Future<Void>> tasks) {
        for (Future<Void> t : tasks) {
            try {
                t.get();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        executor.shutdown();
        db.disconnect();
    }
    
    public void startCrawler(String start) {
        db.connect();
        db.createTable();
        db.insertRow(start, 0);
        
        List<Future<Void>> tasks = new ArrayList<>();

        while (true) {
            String visit = getNext();

            if (visit == null) break;

            CrawlerThread task = new CrawlerThread(visit, db);
            tasks.add(executor.submit(task));
        }

        finishCrawler(tasks);
    }

    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler(10);
        crawler.startCrawler("https://ii.up.krakow.pl");
    }
}
