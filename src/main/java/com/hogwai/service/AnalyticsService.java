package com.hogwai.service;

import com.hogwai.model.RedditPost;
import com.hogwai.repository.RedditPostRepository;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class AnalyticsService {

    public static final String REDDIT_POSTS = "reddit-posts";
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private final DynamoDbTable<RedditPost> postTable;
    private final DynamoDbEnhancedClient client;
    private final RedditPostRepository redditPostRepository;

    public AnalyticsService(DynamoDbEnhancedClient client,
                            RedditPostRepository redditPostRepository) {
        this.client = client;
        this.redditPostRepository = redditPostRepository;
        this.postTable = this.client.table(REDDIT_POSTS, TableSchema.fromBean(RedditPost.class));
    }


    public List<RedditPost> getTopPostsBySubreddit(String subreddit,
                                                   Instant startDate,
                                                   Instant endDate,
                                                   int limit) {
        return redditPostRepository.getTopPostsBySubreddit(subreddit, startDate, endDate, limit);
    }


    public Map<String, Long> getFlairDistribution(String subreddit) {
        QueryConditional queryCondition = QueryConditional.keyEqualTo(
                Key.builder()
                   .partitionValue(subreddit)
                   .build()
        );

        return postTable.query(r -> r.queryConditional(queryCondition))
                        .items()
                        .stream()
                        .filter(post -> !StringUtils.isEmpty(post.getLinkFlairText()))
                        .collect(Collectors.groupingBy(RedditPost::getLinkFlairText, Collectors.counting()));
    }

    public List<Map.Entry<String, Long>> getTopKeywords(String subreddit,
                                                        Instant startDate,
                                                        Instant endDate,
                                                        int limit) {
        List<RedditPost> posts =
                redditPostRepository.scanBySubredditAndDate(subreddit, startDate, endDate);
        Map<String, Long> keywordFrequencies = new ConcurrentHashMap<>();
        posts.forEach(post -> {
            if (post.getKeywords() != null) {
                post.getKeywords().forEach(keyword -> keywordFrequencies.merge(keyword, 1L, Long::sum));
            }
        });
        return keywordFrequencies.entrySet().stream()
                                 .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                 .limit(limit)
                                 .toList();
    }

    public Map<String, Long> compareTerms(String subreddit,
                                          Instant startDate,
                                          Instant endDate,
                                          String terms) {
        Set<String> termSet = COMMA_PATTERN.splitAsStream(terms)
                                           .map(String::trim)
                                           .map(String::toLowerCase)
                                           .collect(Collectors.toSet());
        List<RedditPost> posts =
                redditPostRepository.scanBySubredditAndDate(subreddit, startDate, endDate);
        Map<String, Long> termCounts = termSet.stream().collect(Collectors.toMap(Function.identity(), k -> 0L));
        posts.forEach(post -> {
            if (post.getKeywords() != null) {
                post.getKeywords().forEach(keyword -> {
                    if (termCounts.containsKey(keyword)) {
                        termCounts.put(keyword, termCounts.get(keyword) + 1);
                    }
                });
            }
        });
        return termCounts;
    }
}
