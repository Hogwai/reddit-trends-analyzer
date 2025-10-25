package com.hogwai.controller;

import com.hogwai.model.RedditPost;
import com.hogwai.service.AnalyticsService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Controller("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Get("/top-posts")
    public List<RedditPost> getTopPosts(
            @QueryValue String subreddit,
            @QueryValue(defaultValue = "7") int days,
            @QueryValue(defaultValue = "10") int limit) {

        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);

        return analyticsService.getTopPostsBySubreddit(subreddit, startDate, endDate, limit);
    }

    @Get("/top-keywords")
    public List<Map.Entry<String, Long>> getTopKeywords(
            @QueryValue String subreddit,
            @QueryValue(defaultValue = "30") int days,
            @QueryValue(defaultValue = "20") int limit) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);
        return analyticsService.getTopKeywords(subreddit, startDate, endDate, limit);
    }

    @Get("/compare")
    public Map<String, Long> compareTerms(
            @QueryValue String subreddit,
            @QueryValue String terms,
            @QueryValue(defaultValue = "30") int days) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);
        return analyticsService.compareTerms(subreddit, startDate, endDate, terms);
    }

    @Get("/flair-distribution")
    public Map<String, Long> getFlairDistribution(@QueryValue String subreddit) {
        return analyticsService.getFlairDistribution(subreddit);
    }
}
