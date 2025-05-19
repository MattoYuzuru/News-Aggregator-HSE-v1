package com.news.model;

public enum ArticleStatus {
    RAW, // just parsed from the main pages (url at least)
    ENRICHED, // detailed info from the article (content etc)
    ANALYZED, // AI fills summary, region and tags
    ERROR // In case of AI failing
}
