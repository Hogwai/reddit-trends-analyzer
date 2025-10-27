package com.hogwai.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public record Flair(
        String keyword,
        Long frequency
) {
}