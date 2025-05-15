package com.news.aianalysis;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class EnrichmentResult {
    private String summary;
    private String region;
    private List<String> tags;
    public static EnrichmentResult empty() {
        return EnrichmentResult.builder()
                .summary(null)
                .region(null)
                .tags(List.of())
                .build();
    }
}
