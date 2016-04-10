package no.javazone.cake.redux.comments;

import java.time.LocalDateTime;

public interface FeedbackBuilder {
    String getId();
    String getTalkid();
    String getAuthor();
    LocalDateTime getCreated();
    void setInfo(String info);
    Feedback create();

    static FeedbackBuilder builder(FeedbackType feedbackType) {
        switch (feedbackType) {
            case COMMENT:
                return Comment.builder();
            case TALK_RATING:
                return TalkRating.builder();
            default:
                throw new RuntimeException("Unknown feedback type " + feedbackType);
        }
    }

}
