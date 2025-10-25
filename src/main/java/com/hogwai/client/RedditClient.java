package com.hogwai.client;

import com.hogwai.model.record.RedditListing;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;
import org.reactivestreams.Publisher;

@Client("https://www.reddit.com")
@Header(name = "User-Agent", value = "TrendAnalyzer/1.0")
public interface RedditClient {
    @Get("/r/{subreddit}.json")
    Publisher<RedditListing> fetchListing(
            @PathVariable String subreddit,
            @QueryValue("t") String timeFilter,
            @QueryValue("limit") int limit
    );

    default Publisher<RedditListing> fetchPosts(String subreddit, String timeFilter, int limit) {
        return fetchListing(subreddit, timeFilter, limit);
    }
}