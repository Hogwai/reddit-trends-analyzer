package com.hogwai.ai;

import com.hogwai.model.RedditPost;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@Singleton
public class AIAnalyzer {
    private static final Logger LOG = LoggerFactory.getLogger(AIAnalyzer.class);
    private static final String SUMMARIZE_TEXT = """
            Summarize the text (100 words)
            Example:
            "It treats this subject.."
            
            Text: %s
            """;
    private static final String GIVE_SENTIMENT = """
            Give the text sentiment in 1 word (POSITIVE, NEUTRAL, NEGATIVE), nothing else.
            Text: %s
            """;

    private final ChatModel model;

    public AIAnalyzer(@Value("${ollama.host:http://localhost:11434}") String ollamaHost,
                      @Value("${ollama.model:gemma3:1b}") String modelName) {
        this.model = OllamaChatModel.builder()
                                    .baseUrl(ollamaHost)
                                    .modelName(modelName)
                                    .temperature(0.0)
                                    .build();
    }

    public RedditPost enrich(RedditPost post) {
        String text = post.getTitle() + "\n" + post.getSelfText();

        LOG.info("Start enriching post {}", post.getId());
        String summary = model.chat(SUMMARIZE_TEXT.formatted(text))
                              .trim();
        String sentiment = model.chat(GIVE_SENTIMENT.formatted(text))
                                .trim()
                                .toLowerCase();

        post.setSummary(summary);
        post.setSentiment(sentiment);
        post.setProcessedAt(Instant.now());

        LOG.info("Enriched post {}", post.getId());
        return post;
    }
}