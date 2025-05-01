package com.news.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class NHKParser {
    private static final String BASE_URL = "https://www3.nhk.or.jp";
    private static final String NEWS_LIST_URL = BASE_URL + "/nhkworld/en/news/list/";

    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect(NEWS_LIST_URL).get();
            Elements articles = doc.select("div.c-articleList article.c-article");

            for (Element article : articles) {
                String title = article.selectFirst("h3.c-article__title a").text();
                String relativeLink = article.selectFirst("h3.c-article__title a").attr("href");
                String url = BASE_URL + relativeLink;
                String time = article.selectFirst("div.c-article__date").text();

                System.out.println("Title: " + title);
                System.out.println("URL: " + url);
                System.out.println("Published: " + time);
                System.out.println("------------");
            }
        } catch (IOException e) {
            System.out.println("Failed to connect to NHK: " + e.getMessage());
        }

        // main title : p-article2__title
        // main content : p-article2__content / p-article__body
        // main tags ul : p-newsDetail__tags c-tags2
        // lists c-articleMediaList / c-articleMedia__title / c-article__date
        // https://www3.nhk.or.jp/nhkworld/en/news/list/?p=1 / 2
    }
}
