package com.hogwai.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public record SummarizedPost(
        String id,
        Long createdUtc,
        String author,
        String title,
        String url,
        Integer score,
        Integer numComments,
        Double upvoteRatio,
        Boolean isOriginalContent,
        String linkFlairText) {
}
