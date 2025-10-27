package com.hogwai.model.raw;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Introspected
@Serdeable
public record ListingData(
        String after,
        Integer dist,
        String modhash,
        String geo_filter,
        List<RedditChild> children
) {
}