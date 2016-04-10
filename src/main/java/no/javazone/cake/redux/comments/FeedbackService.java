package no.javazone.cake.redux.comments;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

import java.util.List;

public class FeedbackService {
    public static FeedbackService get() {
        return new FeedbackService();
    }

    public JsonArray addComment(JsonObject payload,String username) {
        String talkref = payload.requiredString("talkref");
        Feedback feedback = Comment.builder()
                .setTalkComment(cleanUserInput(payload.requiredString("comment")))
                .setTalkid(talkref)
                .setAuthor(username)
                .create();
        FeedbackDao instance = FeedbackDao.instance();
        instance.addFeedback(feedback);
        List<Feedback> feedbacks = FeedbackDao.instance().feedbacksForTalk(talkref);
        return JsonArray.fromNodeStream(feedbacks.stream().map(Feedback::asDisplayJson));
    }

    private static String cleanUserInput(String input) {
        return input
                .replaceAll("\n"," ")
                .replaceAll("&"," ")
                .replaceAll("<","")
                .replaceAll(">","");
    }

    public JsonArray commentsForTalk(String talkRef) {
        List<Feedback> feedbacks = FeedbackDao.instance().feedbacksForTalk(talkRef);
        return JsonArray.fromNodeStream(feedbacks.stream().map(Feedback::asDisplayJson));
    }
}
