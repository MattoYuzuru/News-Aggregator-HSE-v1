package com.news.ai.operation;

import com.news.model.Article;

public interface AIOperation<T> {
    T execute(Article article) throws Exception;
    String getOperationName();
}