package no.javazone.cake.redux.comments;

import java.util.Arrays;

public enum Rating implements Comparable<Rating> {
    VERY_POSITIVE("++",5000),
    POSITIVE("+",4000),
    NEUTRAL("0",3000),
    NEGATIVE("-",2000),
    VERY_NEGATIVE("--",1000);

    private final String descrition;
    public final int ratingValue;

    public static Rating fromText(String descrition) {
        return Arrays.asList(values()).stream()
                .filter(ra -> ra.descrition.equals(descrition))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Unknown description " + descrition));
    }

    Rating(String description,int ratingValue) {
        this.descrition = description;
        this.ratingValue = ratingValue;
    }

    public String asText() {
        return descrition;
    }
}
