package no.javazone.cake.redux.comments;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Feedback {
    public final String id;
    public final String talkid;
    public final String author;
    public final LocalDateTime created;

    public Feedback(FeedbackBuilder feedbackBuilder) {
        this.id = feedbackBuilder.getId();
        this.talkid = feedbackBuilder.getTalkid();
        this.author = feedbackBuilder.getAuthor();
        this.created = feedbackBuilder.getCreated();
    }

    public abstract FeedbackType feedbackType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Feedback)) return false;
        Feedback feedback = (Feedback) o;
        return Objects.equals(id, feedback.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
