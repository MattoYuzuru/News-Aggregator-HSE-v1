package com.news.storage;

import com.news.model.Article;
import com.news.model.ArticleFilter;
import com.news.model.ArticleStatus;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    void save(Article article);
    void deleteById(Integer id);
    Optional<Article> findByUrl(String url);
    Optional<Article> findById(Integer id);
    Optional<List<Article>> findBySubstrInContent(String substr);
    Optional<List<Article>> findBySubstrInTitle(String substr);
    Optional<Integer> findIdByUrl(String url);
    List<Article> findAll();
    List<Article> findByStatus(ArticleStatus status);
    void update(Article article);
    List<Article> findArticlesWithFilters(ArticleFilter filter);
    void deleteOlderThanDays(int days);
}
