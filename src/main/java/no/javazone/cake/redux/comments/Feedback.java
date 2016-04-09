package no.javazone.cake.redux.comments;

public abstract class Feedback {
    public final String id;
    public final String talkid;
    public final String author;

    public Feedback(FeedbackBuilder feedbackBuilder) {
        this.id = feedbackBuilder.getId();
        this.talkid = feedbackBuilder.getTalkid();
        this.author = feedbackBuilder.getAuthor();
    }

    public abstract FeedbackType feedbackType();
}
