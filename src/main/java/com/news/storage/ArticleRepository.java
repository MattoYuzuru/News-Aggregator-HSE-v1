package com.news.storage;

import com.news.model.Article;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    void save(Article article);
    void saveAll(List<Article> articles);
    Optional<Article> findByUrl(String url);
    Optional<Integer> findIdByUrl(String url);
    List<Article> findAll();
    void deleteOlderThanDays(int days);
}
