package com.news.executor.impl.system.export;

import com.news.model.Article;

import java.util.List;

public interface Exporter {
    String export(List<Article> articles);
}
