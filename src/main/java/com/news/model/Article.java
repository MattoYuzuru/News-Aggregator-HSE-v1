package com.news.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Article {
    @ToString.Exclude
    private Long id;
    private String title;
    private String url;
    @ToString.Exclude
    private String summary;
    private String content;
    private String region;
    private List<String> tags = new ArrayList<>();
    private LocalDateTime publishedAt;
    private String author;
    private String sourceName;
    @ToString.Exclude
    private String imageUrl;
    @ToString.Exclude
    private String language;
    @ToString.Exclude
    private ArticleStatus status;
}

