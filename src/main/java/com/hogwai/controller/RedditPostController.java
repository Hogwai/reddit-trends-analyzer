package com.hogwai.controller;

import com.hogwai.model.RedditPost;
import com.hogwai.service.RedditPostService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller("/reddit")
public class RedditPostController {
    private final RedditPostService redditPostService;

    public RedditPostController(RedditPostService redditPostService) {
        this.redditPostService = redditPostService;
    }

    @Post("/fetch")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CompletableFuture<List<RedditPost>> fetchPosts(
            @QueryValue String subreddit,
            @QueryValue(defaultValue = "month") String timeFilter,
            @QueryValue(defaultValue = "100") int limit) {
        CompletableFuture<List<RedditPost>> completableFuture = new CompletableFuture<>();

        redditPostService.fetchAndSavePosts(subreddit, timeFilter, limit)
                         .subscribe(
                                 completableFuture::complete,
                                 completableFuture::completeExceptionally
                         );

        return completableFuture;
    }

    @Get("/posts")
    public List<RedditPost> getPosts() {
        return redditPostService.getAllPosts();
    }
}
