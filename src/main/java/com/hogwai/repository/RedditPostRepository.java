package com.hogwai.repository;

import com.hogwai.model.RedditPost;
import com.hogwai.model.SummarizedPost;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.*;

@Singleton
public class RedditPostRepository {
    private static final Logger LOG = LoggerFactory.getLogger(RedditPostRepository.class);
    public static final Set<String> SUMMARIZED_POST_ATTRIBUTES = Set.of(
            "id",
            "createdUtc",
            "author",
            "title",
            "url",
            "score",
            "numComments",
            "upvoteRatio",
            "isOriginalContent",
            "linkFlairText");
    public static final Set<String> KEYWORDS_ATTRIBUTE = Set.of("keywords");
    public static final Set<String> LINK_FLAIR_TEXT_ATTRIBUTE = Set.of("linkFlairText");

    private final DynamoDbTable<RedditPost> postTable;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;


    public RedditPostRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.postTable = dynamoDbEnhancedClient.table("reddit-posts", TableSchema.fromBean(RedditPost.class));
    }

    public List<RedditPost> getPostsKeywordsBySubredditAndDate(String subreddit, Instant startDate, Instant endDate) {
        Map<String, AttributeValue> expressionValues = buildAttributeValues(subreddit, startDate, endDate);
        Expression expression = buildExpression(expressionValues);
        ScanEnhancedRequest request = buildRequestWithAttributes(expression, KEYWORDS_ATTRIBUTE);
        return postTable.scan(request)
                        .items()
                        .stream()
                        .toList();
    }

    public List<RedditPost> getPostsFlairsBySubreddit(String subreddit) {
        Map<String, AttributeValue> expressionValues =
                Map.of(":subreddit", AttributeValue.builder()
                                                   .s(subreddit)
                                                   .build());
        Expression expression = Expression.builder()
                                          .expression("subreddit = :subreddit")
                                          .expressionValues(expressionValues)
                                          .build();
        ScanEnhancedRequest request = buildRequestWithAttributes(expression, LINK_FLAIR_TEXT_ATTRIBUTE);

        return postTable.scan(request)
                        .items()
                        .stream()
                        .toList();
    }

    public List<SummarizedPost> getTopPostsBySubreddit(String subreddit, Instant startDate, Instant endDate, int limit) {
        Map<String, AttributeValue> expressionValues = buildAttributeValues(subreddit, startDate, endDate);
        Expression expression = buildExpression(expressionValues);
        ScanEnhancedRequest request = buildRequestWithAttributes(expression, SUMMARIZED_POST_ATTRIBUTES);
        return postTable.scan(request)
                        .items()
                        .stream()
                        .sorted(Comparator.comparingInt(RedditPost::getScore)
                                          .reversed())
                        .map(this::mapToSummarizedPost)
                        .limit(limit)
                        .toList();
    }


    public void saveAll(List<RedditPost> posts) {
        if (posts == null || posts.isEmpty()) {
            LOG.warn("No post to save");
            return;
        }

        for (int i = 0; i < posts.size(); i += 25) {
            List<RedditPost> batch = posts.subList(i, Math.min(i + 25, posts.size()));
            WriteBatch.Builder<RedditPost> writeBatchBuilder = WriteBatch.builder(RedditPost.class)
                                                                         .mappedTableResource(this.postTable);
            batch.forEach(writeBatchBuilder::addPutItem);
            BatchWriteItemEnhancedRequest batchRequest =
                    BatchWriteItemEnhancedRequest.builder()
                                                 .addWriteBatch(writeBatchBuilder.build())
                                                 .build();

            try {
                this.dynamoDbEnhancedClient.batchWriteItem(batchRequest);
                LOG.info("Batch {}–{} inserted ({} elements)", i + 1, i + batch.size(), batch.size());
            } catch (Exception e) {
                LOG.error("Error while inserting batch {}–{} : {}", i + 1, i + batch.size(), e.getMessage(), e);
            }
        }
        LOG.info("Saved {} posts.", posts.size());
    }


    private Expression buildExpression(Map<String, AttributeValue> attributes) {
        return Expression.builder()
                         .expression("subreddit = :subreddit AND createdUtc BETWEEN :startDate AND :endDate")
                         .expressionValues(attributes)
                         .build();
    }

    private ScanEnhancedRequest buildRequestWithAttributes(Expression expression,
                                                           Set<String> attributesToProject) {
        return ScanEnhancedRequest.builder()
                                  .filterExpression(expression)
                                  .attributesToProject(new ArrayList<>(attributesToProject))
                                  .build();
    }

    private Map<String, AttributeValue> buildAttributeValues(String subreddit,
                                                             Instant startDate,
                                                             Instant endDate) {
        return Map.of(
                ":subreddit", AttributeValue.builder()
                                            .s(subreddit)
                                            .build(),
                ":startDate", AttributeValue.builder()
                                            .n(String.valueOf(startDate.getEpochSecond()))
                                            .build(),
                ":endDate", AttributeValue.builder()
                                          .n(String.valueOf(endDate.getEpochSecond()))
                                          .build());
    }

    private SummarizedPost mapToSummarizedPost(RedditPost rp) {
        return new SummarizedPost(rp.getId(), rp.getCreatedUtc(), rp.getAuthor(),
                rp.getTitle(), rp.getUrl(), rp.getScore(), rp.getNumComments(),
                rp.getUpvoteRatio(), rp.getIsOriginalContent(), rp.getLinkFlairText());
    }
}