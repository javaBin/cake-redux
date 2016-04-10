package no.javazone.cake.redux.comments;

import java.util.Arrays;

public enum Rating implements Comparable<Rating> {
    VERY_POSITIVE("++"),
    POSITIVE("+"),
    NEUTRAL("0"),
    NEGATIVE("-"),
    VERY_NEGATIVE("--");

    private String descrition;

    public static Rating fromText(String descrition) {
        return Arrays.asList(values()).stream()
                .filter(ra -> ra.descrition.equals(descrition))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Unknown description " + descrition));
    }

    Rating(String description) {
        this.descrition = description;
    }

    public String asText() {
        return descrition;
    }
}
