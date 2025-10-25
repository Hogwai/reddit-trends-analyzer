package com.hogwai.enums;

public enum Listing {
    CONTROVERSIAL("controversial"),
    BEST("best"),
    HOT("hot"),
    NEW("new"),
    RANDOM("random"),
    RISING("rising"),
    TOP("top");

    private final String label;

    Listing(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
