package no.javazone.cake.redux.comments;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        feedbackDao.addFeedback(feedback,payload.requiredString("lastModified"));
        return commentsForTalk(talkref);
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
        String lastModified = payload.requiredString("lastModified");
        FeedbackDao feedbackDao = FeedbackDao.instance();
        Optional<Feedback> oldFeedback = feedbackDao.feedbacksForTalk(talkref)
                .filter(fb -> fb.feedbackType() == FeedbackType.TALK_RATING && fb.author.equals(username))
                .findAny();
        if (oldFeedback.isPresent()) {
            lastModified = feedbackDao.deleteFeedback(oldFeedback.get().talkid,oldFeedback.get().id,lastModified);
        }



        Feedback feedback = TalkRating.builder()
                .setRating(Rating.fromText(payload.requiredString("rating")))
                .setTalkid(talkref)
                .setAuthor(username)
                .create();
        feedbackDao.addFeedback(feedback, lastModified);
        return ratingsForTalk(talkref);
    }



    public JsonArray commentsForTalk(String talkRef) {
        return JsonArray.fromNodeStream(FeedbackDao.instance().feedbacksForTalk(talkRef)
                .filter(fb -> fb.feedbackType() == FeedbackType.COMMENT)
                .sorted((o1,o2)->o1.created.compareTo(o2.created))
                .map(Feedback::asDisplayJson)
        );
    }

    public JsonArray ratingsForTalk(String talkRef) {
        return JsonArray.fromNodeStream(FeedbackDao.instance().feedbacksForTalk(talkRef)
                .filter(fb -> fb.feedbackType() == FeedbackType.TALK_RATING)
                .map(fb -> (TalkRating) fb)
                .sorted((o1,o2)-> {
                    int val = o1.rating.compareTo(o2.rating);
                    if (val != 0) {
                        return val;
                    }
                    return o1.author.compareTo(o2.author);
                })
                .map(Feedback::asDisplayJson)
        );
    }

    public Optional<String> contactForTalk(String talkRef) {
        return FeedbackDao.instance().feedbacksForTalk(talkRef)
                .filter(fb -> fb.feedbackType() == FeedbackType.CONTACT)
                .map(Feedback::getInfo)
                .findAny();
    }
}
