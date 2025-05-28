package com.news.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ArticleFilter {
    private final String source;
    private final ArticleStatus status;
    private final String language;
    private final String author;
    private final LocalDateTime publishedAfter;
    private final LocalDateTime publishedBefore;
    private final boolean todayOnly;
    private final List<String> tags;
    private final Integer limit;
    private final Integer offset;
    private final String sortBy;
    private final boolean ascending;

    private ArticleFilter(Builder builder) {
        this.source = builder.source;
        this.status = builder.status;
        this.language = builder.language;
        this.author = builder.author;
        this.publishedAfter = builder.publishedAfter;
        this.publishedBefore = builder.publishedBefore;
        this.todayOnly = builder.todayOnly;
        this.tags = builder.tags;
        this.limit = builder.limit;
        this.offset = builder.offset;
        this.sortBy = builder.sortBy;
        this.ascending = builder.ascending;
    }

    public static class Builder {
        private String source;
        private ArticleStatus status;
        private String language;
        private String author;
        private LocalDateTime publishedAfter;
        private LocalDateTime publishedBefore;
        private boolean todayOnly;
        private List<String> tags = new ArrayList<>();
        private Integer limit;
        private Integer offset;
        private String sortBy = "published_at";
        private boolean ascending = false;

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder status(ArticleStatus status) {
            this.status = status;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder author(String author) {  // Fixed: now returns Builder
            this.author = author;
            return this;
        }

        public Builder publishedAfter(LocalDateTime publishedAfter) {  // Fixed: now returns Builder
            this.publishedAfter = publishedAfter;
            return this;
        }

        public Builder publishedBefore(LocalDateTime publishedBefore) {  // Fixed: now returns Builder
            this.publishedBefore = publishedBefore;
            return this;
        }

        public Builder todayOnly(boolean todayOnly) {  // Fixed: now returns Builder
            this.todayOnly = todayOnly;
            return this;
        }

        public Builder addTag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = new ArrayList<>(tags);
            return this;
        }

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder ascending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }

        public ArticleFilter build() {
            return new ArticleFilter(this);
        }
    }
}