package no.javazone.cake.redux.comments;

public class Comment extends Feedback {
    public final String talkComment;

    @Override
    public FeedbackType feedbackType() {
        return FeedbackType.COMMENT;
    }

    public static class Builder extends FeedbackBuilder {
        private String talkComment;


        public Builder setTalkComment(String talkComment) {
            this.talkComment = talkComment;
            return this;
        }

        @Override
        public void setInfo(String info) {
            setTalkComment(info);
        }

        @Override
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
