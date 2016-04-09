package no.javazone.cake.redux.comments;

public class TalkRating extends Feedback {
    public final Rating rating;

    public static class Builder implements FeedbackBuilder {
        private String id;
        private String talkid;
        private String author;
        private Rating rating;

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

        public Builder setRating(Rating rating) {
            this.rating = rating;
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

        public TalkRating create() {
            return new TalkRating(this);
        }
    }

    private TalkRating(Builder builder) {
        super(builder);
        this.rating = builder.rating;
    }

}
