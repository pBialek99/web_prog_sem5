package net.webcrawler;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CrawlerThread implements Callable<Void> {
    
    private final String url;
    private final DBConn db;

    public CrawlerThread(String url, DBConn db) {
        
        this.url = url;
        this.db = db;
    }

    @Override
    public Void call() {
        
        try {
            Document doc = Jsoup.connect(url).get();
            Elements urls = doc.select("a[href]");

            urls.forEach(u -> db.insertRow(u.absUrl("href"), 0));

            db.insertRow(url, 1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }
}
