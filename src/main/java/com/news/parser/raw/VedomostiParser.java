package com.news.parser.raw;

import com.news.model.Article;
import com.news.parser.ArticleEnricher;
import com.news.parser.Parser;
import com.news.parser.enriched.VedomostiArticleParser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class VedomostiParser implements Parser {

    private static final String BASE_URL = "https://www.vedomosti.ru";
    private static final List<String> SECTION_URLS = List.of(
            "/tourism",
            "/science",
            "/technologies"
    );
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final ArticleEnricher enricher = new VedomostiArticleParser();

    @Override
    public List<Article> fetchArticles() {
        List<Article> allArticles = new ArrayList<>();
        for (String section : SECTION_URLS) {
            allArticles.addAll(parseFromSection(BASE_URL + section));
        }
        return allArticles;
    }

    private static List<Article> parseFromSection(String url) {
        System.setProperty("webdriver.chrome.driver", "/usr/sbin/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

        List<Article> sectionArticles = new ArrayList<>();

        try {
            System.out.println("Loading page: " + url);
            driver.get(url);

            // wait for the page to load completely
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.articles-cards-list__cell")));

            Thread.sleep(2000);

            List<WebElement> articleCards = driver.findElements(By.cssSelector(
                    "div.articles-cards-list__cell > div.grid-cell__body > a"));

            System.out.println("Found " + articleCards.size() + " article cards on " + url);

            for (int i = 0; i < articleCards.size(); i++) {
                WebElement card = articleCards.get(i);
                try {
                    String href = card.getAttribute("href");
                    if (href == null || href.isEmpty()) {
                        System.err.println("Card " + (i + 1) + ": No href found, skipping");
                        continue;
                    }

                    String link = href.startsWith("http") ? href : BASE_URL + href;

                    String title = extractTitle(card);
                    if (title.equals("Title not found")) {
                        System.err.println("Card " + (i + 1) + ": Could not extract title, skipping");
                        continue;
                    }

                    String imageUrl = extractImageUrl(card);
                    String author = extractAuthor(card);

                    Article article = Article.builder()
                            .url(link)
                            .title(title)
                            .imageUrl(imageUrl)
                            .author(author)
                            .build();

                    sectionArticles.add(article);

                } catch (Exception e) {
                    System.err.println("Error fetching article card " + (i + 1) + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading page " + url + ": " + e.getMessage());
        } finally {
            driver.quit();
        }

        System.out.println("Successfully parsed " + sectionArticles.size() + " articles from " + url);
        return sectionArticles;
    }

    private static String extractTitle(WebElement card) {
        // Strat 1: try data attribute first
        try {
            String dataHeadline = card.getAttribute("data-vr-headline");
            if (dataHeadline != null && !dataHeadline.trim().isEmpty()) {
                return dataHeadline.trim();
            }
        } catch (Exception ignored) {
        }

        // Strat 2: try multiple selectors using findElements
        String[] titleSelectors = {
                ".card__title__container > span",
                ".card__title span",
                ".card__title",
                "span[data-v-5c4030ee]",
                ".card__header .card__title span"
        };

        for (String selector : titleSelectors) {
            try {
                List<WebElement> titleElements = card.findElements(By.cssSelector(selector));
                for (WebElement titleElement : titleElements) {
                    String title = titleElement.getText().trim();
                    if (!title.isEmpty()) {
                        return title;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Strat 3: try data-vr-title as final fb
        try {
            String dataTitle = card.getAttribute("data-vr-title");
            if (dataTitle != null && !dataTitle.trim().isEmpty()) {
                return dataTitle.trim();
            }
        } catch (Exception ignored) {
        }

        return "Title not found";
    }

    private static String extractImageUrl(WebElement card) {
        String[] imageSelectors = {
                "img.card__background-image",
                "img.base-image",
                ".card__image img",
                ".card__background img",
                "img"
        };

        for (String selector : imageSelectors) {
            try {
                List<WebElement> imgElements = card.findElements(By.cssSelector(selector));

                for (WebElement img : imgElements) {
                    String imageUrl = img.getAttribute("src");

                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = img.getAttribute("data-thumbnail");
                    }

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        if (imageUrl.startsWith("/")) {
                            imageUrl = BASE_URL + imageUrl;
                        }
                        return imageUrl;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String extractAuthor(WebElement card) {
        String[] authorSelectors = {
                ".card-author__name",
                ".card__footer .card-author__name",
                "[class*='author'] span"
        };

        for (String selector : authorSelectors) {
            try {
                List<WebElement> authorElements = card.findElements(By.cssSelector(selector));
                for (WebElement authorElement : authorElements) {
                    String author = authorElement.getText().trim();
                    if (!author.isEmpty()) {
                        return author;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Override
    public ArticleEnricher getEnricher() {
        return enricher;
    }
}