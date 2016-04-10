package no.javazone.cake.redux.comments;

import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public abstract class Feedback  {
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
        private String id = UUID.randomUUID().toString();
        private String talkid;
        private String author;
        private LocalDateTime created = LocalDateTime.now();

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

        public abstract FeedbackBuilder setInfo(String info);


        public abstract Feedback create();
    }

    public abstract FeedbackType feedbackType();

    public abstract String getInfo();

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

    public JsonObject asDisplayJson() {
        return JsonFactory.jsonObject()
                .put("id",id)
                .put("author",author)
                .put("info",getInfo());
    }

    public JsonObject asStoreJson() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return JsonFactory.jsonObject()
                .put("id",id)
                .put("feedbacktype",feedbackType().toString())
                .put("talkid",talkid)
                .put("author",author)
                .put("created",created.format(formatter))
                .put("info",getInfo());
    }

    public static Feedback fromStoreJson(JsonObject jsonObject) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String feedbacktype = jsonObject.requiredString("feedbacktype");
        Feedback feedback = FeedbackType.valueOf(feedbacktype)
                .builder()
                .setId(jsonObject.requiredString("id"))
                .setAuthor(jsonObject.requiredString("author"))
                .setTalkid(jsonObject.requiredString("talkid"))
                .setAuthor(jsonObject.requiredString("author"))
                .setInfo(jsonObject.requiredString("info"))
                .setCreated(LocalDateTime.parse(jsonObject.requiredString("created"), formatter))
                .create();
        return feedback;
    }
}
