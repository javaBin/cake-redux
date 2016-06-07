package no.javazone.cake.redux.comments;

public enum FeedbackType {
    COMMENT,TALK_RATING,CONTACT;

    Feedback.FeedbackBuilder builder() {
        switch (this) {
            case COMMENT:
                return Comment.builder();
            case TALK_RATING:
                return TalkRating.builder();
            case CONTACT:
                return Contact.builder();
            default:
                throw new RuntimeException("Unknown feedback type " + this);
        }
    }
}
