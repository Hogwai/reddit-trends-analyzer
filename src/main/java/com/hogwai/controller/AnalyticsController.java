package com.hogwai.controller;

import com.hogwai.model.Flair;
import com.hogwai.model.Keyword;
import com.hogwai.model.SummarizedPost;
import com.hogwai.service.AnalyticsService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Get("/top-posts")
    public List<SummarizedPost> getTopPosts(
            @QueryValue String subreddit,
            @QueryValue(defaultValue = "7") int days,
            @QueryValue(defaultValue = "10") int limit) {

        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);

        return analyticsService.getTopPostsBySubreddit(subreddit, startDate, endDate, limit);
    }

    @Get("/top-keywords")
    public List<Keyword> getTopKeywords(
            @QueryValue String subreddit,
            @QueryValue(defaultValue = "30") int days,
            @QueryValue(defaultValue = "10") int limit) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);
        return analyticsService.getTopKeywords(subreddit, startDate, endDate, limit);
    }

    @Get("/compare-keywords")
    public List<Keyword> compareTerms(
            @QueryValue String subreddit,
            @QueryValue String keywords,
            @QueryValue(defaultValue = "30") int days) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);
        return analyticsService.compareKeywords(subreddit, startDate, endDate, keywords);
    }

    @Get("/top-flairs")
    public List<Flair> getFlairDistribution(@QueryValue String subreddit) {
        return analyticsService.getFlairDistribution(subreddit);
    }
}
