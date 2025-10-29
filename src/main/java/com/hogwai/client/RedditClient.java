package com.hogwai.client;

import com.hogwai.model.raw.RedditListing;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

@Client("https://www.reddit.com")
@Header(name = "User-Agent", value = "TrendAnalyzer/1.0")
public interface RedditClient {
    @Get("/r/{subreddit}.json")
    RedditListing fetchListing(
            @PathVariable String subreddit,
            @QueryValue("t") String temporality,
            @QueryValue("limit") int limit
    );
}