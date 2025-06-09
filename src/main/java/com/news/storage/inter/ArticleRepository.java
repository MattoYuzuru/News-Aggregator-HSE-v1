package com.news.storage;

import com.news.model.Article;
import com.news.model.ArticleFilter;
import com.news.model.ArticleStatus;

import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface ArticleRepository {
    void save(Article article);

    void deleteById(Long id);

    Optional<Article> findByUrl(String url);

    Optional<Article> findById(Long id);

    Optional<Long> findIdByUrl(String url);

    List<Article> findAll();

    List<Article> findByStatus(ArticleStatus status);

    void update(Article article);

    List<Article> findArticlesWithFilters(ArticleFilter filter);

    void deleteOlderThanDays(int days);

    Optional<List<Article>> findBySubstrInTitle(String substr);

    Optional<List<Article>> findBySubstrInContent(String substr);

    Optional<List<Article>> findByTags(List<String> tagNames);

    Optional<List<Article>> findBySubstrInContentAndTitle(String contentSubstring, String titleSubstring);

    Optional<List<Article>> findBySubstrInContentAndTags(String contentSubstr, List<String> tagNames);

    Optional<List<Article>> findBySubstrInTitleAndTags(String titleSubstr, List<String> tagNames);

    Optional<List<Article>> findBySubstrInContentAndTitleAndTags(String contentSubstr, String titleSubstr, List<String> tagNames);
}