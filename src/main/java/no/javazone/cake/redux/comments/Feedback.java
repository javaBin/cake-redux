package no.javazone.cake.redux.comments;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Feedback {
    public final String id;
    public final String talkid;
    public final String author;
    public final LocalDateTime created;

    public Feedback(FeedbackBuilder feedbackBuilder) {
        this.id = feedbackBuilder.id;
        this.talkid = feedbackBuilder.talkid;
        this.author = feedbackBuilder.author;
        this.created = feedbackBuilder.created;
    }

    public abstract static class FeedbackBuilder {
        private String id;
        private String talkid;
        private String author;
        private LocalDateTime created;

        public FeedbackBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public FeedbackBuilder setTalkid(String talkid) {
            this.talkid = talkid;
            return this;
        }

        public FeedbackBuilder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public FeedbackBuilder setCreated(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public abstract void setInfo(String info);


        public abstract Feedback create();
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
