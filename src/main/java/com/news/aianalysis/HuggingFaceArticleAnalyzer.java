package com.news.aianalysis;

import com.news.ConfigLoader;
import com.news.model.Article;
import java.util.List;


public class HuggingFaceArticleAnalyzer implements ArticleAnalyzer {

    String apiKey = ConfigLoader.getApiKey();
    private final HuggingFaceClient client = new HuggingFaceClient(apiKey);

    @Override
    public EnrichmentResult analyze(Article article) throws InterruptedException {
        String content = article.getContent();
        if (content == null || content.isBlank() || content.equals("content")) {
            return EnrichmentResult.empty();
        }

        String summary = client.summarize(content);
        System.out.println(summary);
        String region = client.classifyRegion(content);
        System.out.println(region);
        List<String> tags = client.generateTags(content);
        System.out.println(tags);
        Thread.sleep(5000);

        return EnrichmentResult.builder()
                .summary(summary)
                .region(region)
                .tags(tags)
                .build();
    }
}
