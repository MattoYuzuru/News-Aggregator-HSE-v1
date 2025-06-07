package com.news.ai.client;

import java.util.List;

public interface AIClient {
    String summarize(String content) throws Exception;

    String classifyRegion(String content) throws Exception;

    List<String> generateTags(String content) throws Exception;

    Integer evaluateArticle(String content) throws Exception;
}