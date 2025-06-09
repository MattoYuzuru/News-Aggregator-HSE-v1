package com.news.storage.inter;

import com.news.model.ArticleStatus;

import java.util.List;
import java.util.Map;

public interface ArticleStatsRepository {
    Map<String, Long> countBySource();
    Map<String, Long> countByLanguage();
    Map<String, Map<String, Long>> countBySourceAndStatus();
    Map<String, Long> getDateRangeStats();
    List<String> getTopAuthors(int limit);
    Map<String, Long> getTopTags(int limit);
    List<String> getTopRatedArticles(int limit);

    long countAllArticles();

    long countByStatus(ArticleStatus status);
}