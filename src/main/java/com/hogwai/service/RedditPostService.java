package com.hogwai.service;

import com.hogwai.client.RedditClient;
import com.hogwai.enums.Timeframe;
import com.hogwai.model.RedditPost;
import com.hogwai.model.raw.ListingData;
import com.hogwai.model.raw.RedditChild;
import com.hogwai.model.raw.RedditListing;
import com.hogwai.model.raw.RedditPostData;
import com.hogwai.repository.RedditPostRepository;
import com.hogwai.util.TextProcessingUtil;
import io.micronaut.context.annotation.Value;
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
    public Single<List<RedditPost>> fetchAndSavePosts(String subreddit, String temporality, int limit) {
        LOG.info("Getting {} posts of r/{}", limit, subreddit);
        Publisher<RedditListing> redditListingPublisher = redditClient.fetchPosts(subreddit, temporality, limit);

        return Single.fromPublisher(redditListingPublisher)
                     .map(RedditListing::data)
                     .map(ListingData::children)
                     .map(children -> children.stream()
                                              .map(RedditChild::data)
                                              .filter(Objects::nonNull)
                                              .map(this::mapToRedditPost)
                                              .toList())
                     .doOnSuccess(redditPostRepository::saveAll)
                     .doOnSuccess(posts -> LOG.info("Saved successfully {} posts", posts.size()))
                     .doOnError(err -> LOG.error("Error while fetching posts from r/{}", subreddit));

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

    @Scheduled(fixedRate = "1h")
    public void fetchLatestPosts() {
        LOG.info("Starting automated posts fetching...");
        fetchAndSavePosts(defaultSubreddit, Timeframe.DAY.getLabel(), 100);
    }
}