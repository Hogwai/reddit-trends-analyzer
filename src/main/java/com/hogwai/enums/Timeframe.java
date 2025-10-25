package com.hogwai.enums;

public enum Timeframe {
    HOUR("hour"),
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year"),
    ALL("all");

    private final String label;

    Timeframe(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

