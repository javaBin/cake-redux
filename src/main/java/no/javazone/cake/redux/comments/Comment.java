package no.javazone.cake.redux.comments;

import java.time.LocalDateTime;

public class Comment extends Feedback {
    public final String talkComment;

    @Override
    public FeedbackType feedbackType() {
        return FeedbackType.COMMENT;
    }

    public static class Builder implements FeedbackBuilder {
        private String id;
        private String talkid;
        private String author;
        private String talkComment;
        private LocalDateTime created;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTalkid(String talkid) {
            this.talkid = talkid;
            return this;
        }

        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public Builder setTalkComment(String talkComment) {
            this.talkComment = talkComment;
            return this;
        }

        @Override
        public LocalDateTime getCreated() {
            return created;
        }

        public Builder setCreated(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public String getId() {
            return id;
        }

        public String getTalkid() {
            return talkid;
        }

        public String getAuthor() {
            return author;
        }

        @Override
        public void setInfo(String info) {
            setTalkComment(info);
        }

        public Comment create() {
            return new Comment(this);
        }
    }

    private Comment(Builder builder) {
        super(builder);
        this.talkComment = builder.talkComment;
    }

    public static Builder builder() {
        return new Builder();
    }

}
