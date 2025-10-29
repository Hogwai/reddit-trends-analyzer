package com.hogwai.controller;

import com.hogwai.model.RedditPost;
import com.hogwai.service.RedditPostService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.util.List;

@Controller("/reddit")
@ExecuteOn(TaskExecutors.BLOCKING)
public class RedditPostController {
    private final RedditPostService redditPostService;

    public RedditPostController(RedditPostService redditPostService) {
        this.redditPostService = redditPostService;
    }

    @Post("/fetch")
    public List<RedditPost> fetchPosts(
            @QueryValue String subreddit,
            @QueryValue(defaultValue = "month") String timeFilter,
            @QueryValue(defaultValue = "100") int limit) {
        return redditPostService.fetchAndSavePosts(subreddit, timeFilter, limit);
    }

    @Get("/posts")
    public List<RedditPost> getPosts() {
        return redditPostService.getAllPosts();
    }
}
