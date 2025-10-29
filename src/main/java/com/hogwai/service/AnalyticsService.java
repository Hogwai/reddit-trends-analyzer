package com.hogwai.service;

import com.hogwai.enums.Timeframe;
import com.hogwai.model.*;
import com.hogwai.repository.RedditPostRepository;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class AnalyticsService {

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    public static final DateTimeFormatter YYYY_MM_DD_HH_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
    public static final DateTimeFormatter YYYY_MM_DD_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YYYY_MM_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter YYYY_PATTERN = DateTimeFormatter.ofPattern("yyyy");
    public static final ZoneOffset UTC = ZoneOffset.UTC;
    public static final WeekFields ISO = WeekFields.ISO;
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


    /**
     * Get flairs with their distribution
     *
     * @param subreddit subreddit
     * @return flairs with their distribution
     */
    public List<Flair> getFlairDistribution(String subreddit) {
        List<RedditPost> posts =
                redditPostRepository.getPostsFlairsBySubreddit(subreddit);

        Map<String, Long> flairFrequencies = posts.stream()
                                                  .filter(Objects::nonNull)
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

    /**
     * Give the weekly frequency evolution of a keyword
     *
     * @param subreddit subreddit
     * @param startDate start date
     * @param endDate   end date
     * @param keyword   keyword
     * @return the evolution of keyword occurrences over time
     */
    public KeywordTrend getKeywordTrends(String subreddit,
                                         Instant startDate,
                                         Instant endDate,
                                         String keyword,
                                         String timeframeStr) {

        if (StringUtils.isEmpty(keyword)) {
            throw new IllegalArgumentException("No keyword provided");
        }
        if (StringUtils.isEmpty(timeframeStr)) {
            throw new IllegalArgumentException("No timeframe provided");
        }

        String normalizedKeyword = keyword.trim()
                                          .toLowerCase();
        Timeframe timeframe = Timeframe.valueOf(timeframeStr.toUpperCase());

        List<RedditPost> posts = redditPostRepository
                .getKeywordWithDataBySubredditAndDate(subreddit, startDate, endDate);

        Map<String, Long> counts = posts.stream()
                                        .filter(Objects::nonNull)
                                        .filter(post -> CollectionUtils.isNotEmpty(post.getKeywords()))
                                        .filter(post -> post.getKeywords()
                                                            .stream()
                                                            .filter(Objects::nonNull)
                                                            .map(String::trim)
                                                            .map(String::toLowerCase)
                                                            .anyMatch(k -> k.equals(normalizedKeyword)))
                                        .map(post -> {
                                            Instant created = Instant.ofEpochSecond(post.getCreatedUtc());
                                            LocalDateTime dt = LocalDateTime.ofInstant(created, UTC);

                                            return switch ( timeframe ) {
                                                case HOUR -> dt.format(YYYY_MM_DD_HH_PATTERN);
                                                case DAY -> dt.format(YYYY_MM_DD_PATTERN);
                                                case WEEK -> {
                                                    int year = dt.toLocalDate()
                                                                 .get(ISO.weekBasedYear());
                                                    int week = dt.toLocalDate()
                                                                 .get(ISO.weekOfWeekBasedYear());
                                                    yield year + "-W" + week;
                                                }
                                                case MONTH -> dt.format(YYYY_MM_PATTERN);
                                                case YEAR -> dt.format(YYYY_PATTERN);
                                                case ALL -> "ALL";
                                            };
                                        })
                                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<KeywordTrendPoint> points = counts.entrySet()
                                               .stream()
                                               .map(e -> new KeywordTrendPoint(e.getValue(), e.getKey()))
                                               .sorted(Comparator.comparing(KeywordTrendPoint::label))
                                               .toList();

        return new KeywordTrend(normalizedKeyword, timeframe.getLabel(), points);
    }


}
