package com.news.model;

import java.time.LocalDateTime;

public record Article(String title, String url, String summary, LocalDateTime publishedAt) {

}
