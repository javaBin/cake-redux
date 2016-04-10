package no.javazone.cake.redux.comments;

public class TalkRating extends Feedback {
    public final Rating rating;

    @Override
    public FeedbackType feedbackType() {
        return FeedbackType.TALK_RATING;
    }

    @Override
    public String getInfo() {
        return rating.asText();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends FeedbackBuilder {
        private Rating rating;

        public Builder setRating(Rating rating) {
            this.rating = rating;
            return this;
        }

        @Override
        public Builder setInfo(String info) {
            return setRating(Rating.fromText(info));
        }

        @Override
        public TalkRating create() {
            return new TalkRating(this);
        }
    }

    private TalkRating(Builder builder) {
        super(builder);
        if (builder.rating == null) {
            throw new NullPointerException("Rating required");
        }
        this.rating = builder.rating;
    }

}
