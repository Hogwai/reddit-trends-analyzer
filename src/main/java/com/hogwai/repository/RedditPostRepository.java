package com.hogwai.repository;

import com.hogwai.model.RedditPost;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class RedditPostRepository {
    private static final Logger LOG = LoggerFactory.getLogger(RedditPostRepository.class);

    private final DynamoDbTable<RedditPost> postTable;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;


    public RedditPostRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.postTable = dynamoDbEnhancedClient.table("reddit-posts", TableSchema.fromBean(RedditPost.class));
    }

    public RedditPost save(RedditPost post) {
        postTable.putItem(post);
        return post;
    }

    public List<RedditPost> findBySubreddit(String subreddit) {
        return postTable.query(r -> r.queryConditional(
                                QueryConditional.keyEqualTo(k -> k.partitionValue(subreddit))))
                        .items()
                        .stream()
                        .toList();
    }

    public List<RedditPost> scanBySubredditAndDate(String subreddit, Instant startDate, Instant endDate) {
        ScanEnhancedRequest scanRequest = buildScanRequest(subreddit, startDate, endDate);
        return postTable.scan(scanRequest)
                        .items()
                        .stream()
                        .toList();
    }

    public List<RedditPost> getTopPostsBySubreddit(String subreddit, Instant startDate, Instant endDate, int limit) {
        ScanEnhancedRequest scanRequest = buildScanRequest(subreddit, startDate, endDate);
        return postTable.scan(scanRequest)
                        .items()
                        .stream()
                        .sorted(Comparator.comparingInt(RedditPost::getScore).reversed())
                        .limit(limit)
                        .toList();
    }

    private ScanEnhancedRequest buildScanRequest(String subreddit, Instant startDate, Instant endDate) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":subreddit", AttributeValue.builder().s(subreddit).build());
        expressionValues.put(":startDate", AttributeValue.builder().n(String.valueOf(startDate.getEpochSecond())).build());
        expressionValues.put(":endDate", AttributeValue.builder().n(String.valueOf(endDate.getEpochSecond())).build());

        Expression filterExpression = Expression.builder()
                                                .expression("subreddit = :subreddit AND createdUtc BETWEEN :startDate AND :endDate")
                                                .expressionValues(expressionValues)
                                                .build();

        return ScanEnhancedRequest.builder()
                                  .filterExpression(filterExpression)
                                  .build();
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
}