package com.hogwai.service;

import com.hogwai.client.RedditClient;
import com.hogwai.enums.Timeframe;
import com.hogwai.model.RedditPost;
import com.hogwai.model.raw.RedditChild;
import com.hogwai.model.raw.RedditListing;
import com.hogwai.model.raw.RedditPostData;
import com.hogwai.repository.RedditPostRepository;
import com.hogwai.util.TextProcessingUtil;
import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Singleton
public class RedditPostService {
    private static final Logger LOG = LoggerFactory.getLogger(RedditPostService.class);
    @Value("${default-subreddit}")
    private String defaultSubreddit;
    private final RedditClient redditClient;
    private final RedditPostRepository redditPostRepository;
    private final TextProcessingUtil textProcessingUtil;

    public RedditPostService(RedditClient redditClient,
                             RedditPostRepository redditPostRepository,
                             TextProcessingUtil textProcessingUtil) {
        this.redditClient = redditClient;
        this.redditPostRepository = redditPostRepository;
        this.textProcessingUtil = textProcessingUtil;
    }

    /**
     * Fetch posts from a subreddit and save them in the db
     *
     * @param subreddit   subreddit
     * @param temporality temporality
     * @param limit       max number of posts
     * @return posts fetched
     */
    public List<RedditPost> fetchAndSavePosts(String subreddit, String temporality, int limit) {
        LOG.info("Getting {} posts of r/{}", limit, subreddit);

        RedditListing listing = redditClient.fetchListing(subreddit, temporality, limit);

        List<RedditPost> posts = listing.data()
                                        .children()
                                        .stream()
                                        .map(RedditChild::data)
                                        .filter(Objects::nonNull)
                                        .map(this::mapToRedditPost)
                                        .toList();

        redditPostRepository.saveAll(posts);
        LOG.info("Saved successfully {} posts", posts.size());

        return posts;
    }

    /**
     * Get all posts
     *
     * @return posts
     */
    public List<RedditPost> getAllPosts() {
        return redditPostRepository.getAllPosts();
    }

    /**
     * Map raw reddit post data to {@link RedditPost}
     *
     * @param redditPostData raw data
     * @return {@link RedditPost}
     */
    private RedditPost mapToRedditPost(RedditPostData redditPostData) {
        Set<String> keywords = textProcessingUtil
                .extractKeywords(redditPostData.title() + " " + redditPostData.selftext());
        Long createdUtc = redditPostData.created_utc() == null ?
                Instant.now()
                       .toEpochMilli() :
                redditPostData.created_utc()
                              .longValue();
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

    @Scheduled(fixedRate = "1h", condition = "${posts-scheduling.enabled}")
    @ExecuteOn(TaskExecutors.BLOCKING)
    public void fetchLatestPosts() {
        LOG.info("Starting automated posts fetching...");
        fetchAndSavePosts(defaultSubreddit, Timeframe.DAY.getLabel(), 100);
    }
}