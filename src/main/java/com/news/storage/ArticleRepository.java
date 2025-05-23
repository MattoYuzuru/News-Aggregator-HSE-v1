package com.news.storage;

import com.news.model.Article;
import com.news.model.ArticleStatus;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    void save(Article article);
    void saveAll(List<Article> articles);
    Optional<Article> findByUrl(String url);
    Optional<Integer> findIdByUrl(String url);
    List<Article> findAll();
    List<Article> findByStatus(ArticleStatus status);
    List<Article> findByStatusAndSource(ArticleStatus status, String source, int limit);
    void update(Article article);
    void deleteOlderThanDays(int days);
}
