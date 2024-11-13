package net.webcrawler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerThread implements Callable<Void> {
    
    private final String url;
    private final DBConn conn;

    public CrawlerThread(String url, DBConn conn) {
        
        this.url = url;
        this.conn = conn;
    }

    @Override
    public Void call() {
        
        try {
            Document doc = Jsoup.connect(url).get();
            Elements urls = doc.select("a[href]");

            urls.forEach(u -> conn.insertRow(u.absUrl("href"), 0));

            conn.insertRow(url, 1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }
}
