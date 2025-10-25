package com.hogwai.service;

import com.hogwai.client.RedditClient;
import com.hogwai.model.RedditPost;
import com.hogwai.model.record.*;
import com.hogwai.repository.RedditPostRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Singleton
public class RedditPostService {
    private static final Logger LOG = LoggerFactory.getLogger(RedditPostService.class);
    private final RedditClient redditClient;
    private final RedditPostRepository redditPostRepository;
    private final TextProcessingService textProcessingService;

    public RedditPostService(RedditClient redditClient,
                             RedditPostRepository redditPostRepository,
                             TextProcessingService textProcessingService) {
        this.redditClient = redditClient;
        this.redditPostRepository = redditPostRepository;
        this.textProcessingService = textProcessingService;
    }

    public Single<List<RedditPost>> fetchAndSavePosts(String subreddit, String timeFilter, int limit) {
        LOG.info("Récupération de {} posts de r/{}", limit, subreddit);
        Publisher<RedditListing> redditListingPublisher = redditClient.fetchPosts(subreddit, timeFilter, limit);

        return Single.fromPublisher(redditListingPublisher)
                     .doOnSuccess(listing -> LOG.info("✅ Step 1: listing = {}", listing.kind()))
                     .map(RedditListing::data)
                     .doOnSuccess(data -> LOG.info("✅ Step 2: data = {}", data.after()))
                     .map(ListingData::children)
                     .doOnSuccess(children -> LOG.info("✅ Step 3: children = {}", children.size()))
                     .map(children -> children.stream()
                                          .map(RedditChild::data)
                                          .filter(Objects::nonNull)
                                          .map(this::mapToRedditPost)
                                          .toList())
                     .doOnSuccess(redditPostRepository::saveAll)
                     .doOnSuccess(posts -> LOG.info("✅ Step 4: mapped posts count = {}", posts.size()));

    }

    private RedditPost mapToRedditPost(RedditPostData redditPostData) {
        Set<String> keywords = textProcessingService
                .extractKeywords(redditPostData.title() + " " + redditPostData.selftext());
        Long createdUtc = redditPostData.created_utc() == null ?
                Instant.now().toEpochMilli() : redditPostData.created_utc().longValue();
        return new RedditPost(
                redditPostData.id(),
                redditPostData.subreddit(),
                createdUtc,
                redditPostData.author(),
                redditPostData.title(),
                redditPostData.selftext(),
                redditPostData.permalink(),
                redditPostData.url(),
                redditPostData.score(),
                redditPostData.num_comments(),
                redditPostData.upvote_ratio(),
                redditPostData.over_18(),
                redditPostData.is_original_content(),
                redditPostData.link_flair_text(),
                keywords
        );
    }

    @Scheduled(fixedRate = "1h")
    public void fetchLatestPosts() {
        LOG.info("Starting automated posts fetching...");
        fetchAndSavePosts("socialmedia", "day", 100)
                         .subscribe(posts -> LOG.info("{} fetched posts.", posts.size()),
                                 error -> LOG.error("Error while fetching posts.", error));
    }
}