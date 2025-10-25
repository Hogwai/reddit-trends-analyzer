package com.hogwai.model.record;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public record RedditListing(
        String kind,
        ListingData data
) { }
