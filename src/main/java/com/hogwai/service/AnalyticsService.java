package com.hogwai.service;

import com.hogwai.model.Flair;
import com.hogwai.model.Keyword;
import com.hogwai.model.RedditPost;
import com.hogwai.model.SummarizedPost;
import com.hogwai.repository.RedditPostRepository;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class AnalyticsService {

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private final RedditPostRepository redditPostRepository;

    public AnalyticsService(RedditPostRepository redditPostRepository) {
        this.redditPostRepository = redditPostRepository;
    }


    /**
     * Get the posts with the highest score
     *
     * @param subreddit subreddit
     * @param startDate startDate
     * @param endDate   endDate
     * @param limit     limit
     * @return top posts
     */
    public List<SummarizedPost> getTopPostsBySubreddit(String subreddit,
                                                       Instant startDate,
                                                       Instant endDate,
                                                       int limit) {
        return redditPostRepository.getTopPostsBySubreddit(subreddit, startDate, endDate, limit);
    }


    public List<Flair> getFlairDistribution(String subreddit) {
        List<RedditPost> posts =
                redditPostRepository.getPostsFlairsBySubreddit(subreddit);

        Map<String, Long> flairFrequencies = posts.stream()
                                                  .map(RedditPost::getLinkFlairText)
                                                  .filter(Objects::nonNull)
                                                  .collect(Collectors.groupingBy(
                                                          flair -> flair,
                                                          Collectors.counting()
                                                  ));

        return flairFrequencies.entrySet()
                               .stream()
                               .map(entry -> new Flair(entry.getKey(), entry.getValue()))
                               .sorted(Comparator.comparingLong(Flair::frequency)
                                                 .reversed())
                               .toList();
    }

    /**
     * Get keywords with the highest frequency
     *
     * @param subreddit subreddit
     * @param startDate startDate
     * @param endDate   endDate
     * @param limit     limit
     * @return top keyword
     */
    public List<Keyword> getTopKeywords(String subreddit,
                                        Instant startDate,
                                        Instant endDate,
                                        int limit) {
        List<RedditPost> posts =
                redditPostRepository.getPostsKeywordsBySubredditAndDate(subreddit, startDate, endDate);

        Map<String, Long> keywordFrequencies = posts.stream()
                                                    .filter(post -> post.getKeywords() != null)
                                                    .flatMap(post -> post.getKeywords()
                                                                         .stream())
                                                    .collect(Collectors.groupingBy(
                                                            keyword -> keyword,
                                                            Collectors.counting()
                                                    ));

        return keywordFrequencies.entrySet()
                                 .stream()
                                 .map(entry -> new Keyword(entry.getKey(), entry.getValue()))
                                 .sorted(Comparator.comparingLong(Keyword::frequency)
                                                   .reversed())
                                 .limit(limit)
                                 .toList();
    }

    /**
     * Compare the frequency between terms in the posts of a subreddit
     *
     * @param subreddit      subreddit
     * @param startDate      start date
     * @param endDate        end date
     * @param keywordsAsText keywords to compare
     * @return terms with their frequency
     */
    public List<Keyword> compareKeywords(String subreddit,
                                         Instant startDate,
                                         Instant endDate,
                                         String keywordsAsText) {
        Set<String> splitTerms = COMMA_PATTERN.splitAsStream(keywordsAsText)
                                              .map(String::trim)
                                              .map(String::toLowerCase)
                                              .collect(Collectors.toSet());
        if (splitTerms.isEmpty()) {
            throw new IllegalArgumentException("No terms provided");
        }

        List<RedditPost> posts =
                redditPostRepository.getPostsKeywordsBySubredditAndDate(subreddit, startDate, endDate);

        Map<String, Long> keywordCounts = splitTerms.stream()
                                                    .collect(Collectors.toMap(
                                                            Function.identity(),
                                                            k -> posts.stream()
                                                                      .filter(post -> post.getKeywords() != null)
                                                                      .flatMap(post -> post.getKeywords()
                                                                                           .stream())
                                                                      .filter(keyword -> keyword.equals(k))
                                                                      .count()
                                                    ));

        return keywordCounts.entrySet()
                            .stream()
                            .map(entry -> new Keyword(entry.getKey(), entry.getValue()))
                            .sorted(Comparator.comparingLong(Keyword::frequency)
                                              .reversed())
                            .toList();
    }
}
