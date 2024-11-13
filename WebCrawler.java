package net.webcrawler;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler {
    private DBConn db;
    private ExecutorService executor;
    private int MAX_DEPTH;

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

    private void crawl(int depth, String url, List<Future<Void>> tasks) {
        if (depth > MAX_DEPTH) return;

        CrawlerThread task = new CrawlerThread(url, db, depth + 1, MAX_DEPTH);
        tasks.add.(executor.submit(task));   
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
    
    private void startCrawler(String start) {
        db.connect();
        db.createTable();
        db.insertRow(start, 0);
        
        List<Future<Void>> tasks = new ArrayList<>();

        crawl(1, start, tasks)

        finishCrawler(tasks);
    }

    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler(10, 100);
        crawler.startCrawler("https://ii.up.krakow.pl");
    }
}
