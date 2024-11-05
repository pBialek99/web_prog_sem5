package net.webcrawler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;


public class WebCrawler {
    public static void main(String ...args) {
        String url = "https://ii.uken.krakow.pl";
        if (args.length == 1) {
            url = args[0];
        }

        startCrawler(url);
    }

    public static void startCrawler(String url) {
        DBConn db = new DBConn();
        db.connect();
        db.createTable();

        try {

        } catch (IOException e){
            System.out.println(e.getMessage());
        } finally {
            db.disconnect();
        }
    }
}
