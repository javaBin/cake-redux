package no.javazone.cake.redux.comments;

public class Contact extends Feedback {
    public final String contactPhone;

    @Override
    public FeedbackType feedbackType() {
        return FeedbackType.CONTACT;
    }

    @Override
    public String getInfo() {
        return contactPhone;
    }

    public static class Builder extends FeedbackBuilder {
        private String contactPhone;


        public Builder setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
            return this;
        }

        @Override
        public Builder setInfo(String info) {
            return setContactPhone(info);
        }

        @Override
        public Contact create() {
            return new Contact(this);
        }

    }

    private Contact(Builder builder) {
        super(builder);
        this.contactPhone = builder.contactPhone;
    }

    public static Builder builder() {
        return new Builder();
    }

}
