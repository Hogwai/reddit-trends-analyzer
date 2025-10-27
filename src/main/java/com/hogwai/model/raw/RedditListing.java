package com.hogwai.model.raw;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public record RedditListing(
        String kind,
        ListingData data
) { }
