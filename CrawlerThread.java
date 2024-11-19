package net.webcrawler;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.concurrent.Callable;
import java.util.ArrayList;

public class CrawlerThread implements Callable<Void> {
    private final String url;
    private final DBConn db;
    private final int depth;
    private final int maxDepth;

    public CrawlerThread(String url, DBConn db, int depth, int maxDepth) {
        this.url = url;
        this.db = db;
        this.depth = depth;
        this.maxDepth = maxDepth;
    }

    @Override
    public Void call() {
        if (depth > maxDepth) {
            return null;
        }

        try {
            Document doc = Jsoup.connect(url).get();
            Elements urls = doc.select("a[href]");

            db.insertRow(url, 1);

            urls.forEach(u -> {
                db.insertRow(u.absUrl("href"), 0);

                if (depth + 1 <= maxDepth) {
                    new WebCrawler(depth + 1, maxDepth).crawl(depth + 1, u.absUrl("href"), new ArrayList<>());
                }
            });
        } catch (IOException e) {
            System.out.println("Error crawling URL: " + url);
            System.out.println(e.getMessage());
        }

        return null;
    }
}
