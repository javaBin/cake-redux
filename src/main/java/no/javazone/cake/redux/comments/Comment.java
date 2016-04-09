package no.javazone.cake.redux.comments;

public class Comment extends Feedback {
    public final String talkComment;

    public static class Builder implements FeedbackBuilder {
        private String id;
        private String talkid;
        private String author;
        private String talkComment;

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

        public String getId() {
            return id;
        }

        public String getTalkid() {
            return talkid;
        }

        public String getAuthor() {
            return author;
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
