package no.javazone.cake.redux.comments;

public interface FeedbackBuilder {
    String getId();
    String getTalkid();
    String getAuthor();
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
