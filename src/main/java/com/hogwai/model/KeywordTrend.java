package com.hogwai.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Introspected
@Serdeable
public record KeywordTrend(
        String keyword,
        String timeframe,
        List<KeywordTrendPoint> points
) {
}