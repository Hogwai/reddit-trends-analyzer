package com.hogwai.util;

import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class TextProcessingUtil {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
            "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being",
            "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't",
            "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during",
            "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't",
            "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here",
            "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i",
            "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's",
            "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no",
            "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our",
            "ours", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd",
            "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that",
            "that's", "the", "their", "theirs", "them", "themselves", "then", "there",
            "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this",
            "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't",
            "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's",
            "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom",
            "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll",
            "you're", "you've", "your", "yours", "yourself", "yourselves", "also", "just"
    );

    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[^a-zA-Z\\s']");
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s+");

    public Set<String> extractKeywords(String text) {
        if (StringUtils.isEmpty(text)) {
            return Collections.emptySet();
        }

        String sanitizedText = PUNCTUATION_PATTERN.matcher(text.toLowerCase())
                                                  .replaceAll(" ");

        return Arrays.stream(SPLIT_PATTERN.split(sanitizedText))
                     .filter(word -> word.length() > 2 && !STOP_WORDS.contains(word))
                     .collect(Collectors.toSet());
    }
}