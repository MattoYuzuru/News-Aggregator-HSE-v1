package com.news.parser.enriched;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.parser.ArticleEnricher;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.news.ConfigLoader.FORMATTER;
import static com.news.ConfigLoader.TIMEOUT;

public class VedomostiArticleParser implements ArticleEnricher {
    @Override
    public void enrich(Article article) throws IOException {
//        <div class="article-meta"><!----> <time datetime="2025-05-30T17:34:52.838+03:00" class="article-meta__date">30 мая, 17:34 /</time> <span class="tags"><span class="tags__tag tags__tag--salmon"><!----> <a href="/technologies/trendsrub">Тренды</a></span></span> <!----></div>
//        article-boxes-list article__boxes

        System.setProperty("webdriver.chrome.driver", "/usr/sbin/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

        try {
            driver.get(article.getUrl());

            // wait for the page to load completely
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.article-boxes-list__item")));

            Thread.sleep(2000);

            List<WebElement> div = driver.findElements(By.cssSelector(
                    "div.article-boxes-list__item > div.box-paragraph > p"));

            StringBuilder content = new StringBuilder();

            for (int i = 0; i < div.size(); i++) {
                WebElement p = div.get(i);
                try {
                    content.append(p.getText());
                } catch (Exception e) {
                    System.err.println("Error fetching paragraph" + e.getMessage());
                }
            }

            WebElement time = driver.findElement(By.cssSelector(
                    "div.article__meta > div.article-meta > time"
            ));

            LocalDateTime publishedAt = null;
            try {
                String timeAttribute = time.getAttribute("datetime");
                if (timeAttribute != null) {
                    publishedAt = LocalDateTime.parse(timeAttribute, FORMATTER);
                }
            } catch (Exception e) {
                System.err.println("Error fetching time" + e.getMessage());
            }

            article.setPublishedAt(publishedAt);
            article.setContent(content.toString());
            article.setStatus(ArticleStatus.ENRICHED);

        } catch (Exception e) {
            System.err.println("Error loading page " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    @Override
    public boolean supports(Article article) {
        return article.getUrl().contains("vedomosti.ru");
    }
}
