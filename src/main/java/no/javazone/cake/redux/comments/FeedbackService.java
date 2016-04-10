package no.javazone.cake.redux.comments;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        FeedbackDao feedbackDao = FeedbackDao.instance();
        feedbackDao.addFeedback(feedback);
        return rereadFeedbacks(talkref, feedbackDao, FeedbackType.COMMENT);
    }

    private JsonArray rereadFeedbacks(String talkref, FeedbackDao feedbackDao, FeedbackType feedbackType) {
        List<Feedback> feedbacks = feedbackDao.feedbacksForTalk(talkref)
                .stream()
                .filter(fb -> fb.feedbackType() == feedbackType)
                .collect(Collectors.toList());
        return JsonArray.fromNodeStream(feedbacks.stream().map(Feedback::asDisplayJson));
    }

    private static String cleanUserInput(String input) {
        return input
                .replaceAll("\n"," ")
                .replaceAll("&"," ")
                .replaceAll("<","")
                .replaceAll(">","");
    }

    public JsonArray giveRating(JsonObject payload,String username) {
        String talkref = payload.requiredString("talkref");
        FeedbackDao feedbackDao = FeedbackDao.instance();
        Optional<Feedback> oldFeedback = feedbackDao.feedbacksForTalk(talkref).stream()
                .filter(fb -> fb.feedbackType() == FeedbackType.TALK_RATING && fb.author.equals(username))
                .findAny();
        if (oldFeedback.isPresent()) {
            feedbackDao.deleteFeedback(oldFeedback.get().id);
        }

        Feedback feedback = TalkRating.builder()
                .setRating(Rating.fromText(payload.requiredString("rating")))
                .setTalkid(talkref)
                .setAuthor(username)
                .create();
        feedbackDao.addFeedback(feedback);
        return rereadFeedbacks(talkref, feedbackDao, FeedbackType.TALK_RATING);
    }



    public JsonArray commentsForTalk(String talkRef) {
        List<Feedback> feedbacks = FeedbackDao.instance().feedbacksForTalk(talkRef);
        return JsonArray.fromNodeStream(feedbacks.stream().map(Feedback::asDisplayJson));
    }
}
