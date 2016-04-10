package no.javazone.cake.redux.comments;

import org.jsonbuddy.JsonObject;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class FeedbackTest {
    @Test
    public void shouldSerializeCorrectly() throws Exception {
        Feedback feedback = Comment.builder()
                .setTalkid("talx")
                .setAuthor("authorname")
                .setInfo("my comment")
                .setCreated(LocalDateTime.now().withNano(0))
                .create();

        JsonObject jsonObject = feedback.asStoreJson();

        Feedback copy = Feedback.fromStoreJson(jsonObject);

        assertThat(copy).isEqualToComparingFieldByField(feedback);

    }
}