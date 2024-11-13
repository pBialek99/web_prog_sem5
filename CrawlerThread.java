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
    private final DBConn dbConn;

    public CrawlerThread(String url, DBConn dbConn) {
        this.url = url;
        this.dbConn = dbConn;
    }

    @Override
    public Void call() {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                dbConn.insertRow(absUrl, 0);  // 0 oznacza, że link jeszcze nie został odwiedzony
            }

            // Ustaw link jako odwiedzony (przy pomocy metody insertRow)
            dbConn.insertRow(url, 1);  // Po odwiedzeniu linku zwiększamy wartość "seen" na 1

            System.out.println("Processed URL: " + url);
        } catch (IOException e) {
            System.out.println("Failed to crawl URL: " + url + " - " + e.getMessage());
        }

        return null;
    }
}
