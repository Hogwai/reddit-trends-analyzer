package com.hogwai.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.util.Set;

@Introspected
@Serdeable
@DynamoDbBean
public class RedditPost {

    private String id;
    private String subreddit;
    private Long createdUtc;
    private String author;
    private String title;
    private String selfText;
    private String permalink;
    private String url;
    private Integer score;
    private Integer numComments;
    private Double upvoteRatio;
    private Boolean over18;
    private Boolean isOriginalContent;
    private String linkFlairText;
    private Set<String> keywords;

    private String summary;
    private String sentiment;
    private Instant processedAt;

    /**
     * Needed with @DynamoDbBean
     */
    public RedditPost() { }

    public RedditPost(String id, String subreddit, Long createdUtc, String author,
                      String title, String selfText, String permalink,
                      String url, Integer score, Integer numComments, Double upvoteRatio,
                      Boolean over18, Boolean isOriginalContent, String linkFlairText,
                      Set<String> keywords) {
        this.id = id;
        this.subreddit = subreddit;
        this.createdUtc = createdUtc;
        this.author = author;
        this.title = title;
        this.selfText = selfText;
        this.permalink = permalink;
        this.url = url;
        this.score = score;
        this.numComments = numComments;
        this.upvoteRatio = upvoteRatio;
        this.over18 = over18;
        this.isOriginalContent = isOriginalContent;
        this.linkFlairText = linkFlairText;
        this.keywords = keywords;
    }

    @DynamoDbPartitionKey
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubreddit() { return subreddit; }
    public void setSubreddit(String subreddit) { this.subreddit = subreddit; }

    @DynamoDbSortKey
    public Long getCreatedUtc() { return createdUtc; }
    public void setCreatedUtc(Long createdUtc) { this.createdUtc = createdUtc; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSelfText() { return selfText; }
    public void setSelfText(String selfText) { this.selfText = selfText; }

    public String getPermalink() { return permalink; }
    public void setPermalink(String permalink) { this.permalink = permalink; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getNumComments() { return numComments; }
    public void setNumComments(Integer numComments) { this.numComments = numComments; }

    public Double getUpvoteRatio() { return upvoteRatio; }
    public void setUpvoteRatio(Double upvoteRatio) { this.upvoteRatio = upvoteRatio; }

    public Boolean getOver18() { return over18; }
    public void setOver18(Boolean over18) { this.over18 = over18; }

    public Boolean getIsOriginalContent() { return isOriginalContent; }
    public void setIsOriginalContent(Boolean isOriginalContent) { this.isOriginalContent = isOriginalContent; }

    public String getLinkFlairText() { return linkFlairText; }
    public void setLinkFlairText(String linkFlairText) { this.linkFlairText = linkFlairText; }

    public Set<String> getKeywords() { return keywords; }
    public void setKeywords(Set<String> keywords) { this.keywords = keywords; }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
