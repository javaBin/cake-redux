package no.javazone.cake.redux.comments;

public class TalkRating extends Feedback {
    public final Rating rating;

    @Override
    public FeedbackType feedbackType() {
        return FeedbackType.TALK_RATING;
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
        public void setInfo(String info) {
            setRating(Rating.fromText(info));
        }

        @Override
        public TalkRating create() {
            return new TalkRating(this);
        }
    }

    private TalkRating(Builder builder) {
        super(builder);
        this.rating = builder.rating;
    }

}
