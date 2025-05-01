package com.news.model;

import java.time.LocalDateTime;

public record Article(String title, String url, String summary, LocalDateTime publishedAt) {
    @Override
    public String toString() {
        return this.title + ": " + this.url + " of " + this.publishedAt;
    }
}
