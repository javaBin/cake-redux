package no.javazone.cake.redux.comments;

import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;

import java.util.stream.Stream;

import static org.jsonbuddy.JsonFactory.jsonObject;

public class FeedbackInSleepingpill implements FeedbackDao {
    private static transient FeedbackDao instance;

    public static synchronized FeedbackDao get() {
        if (instance == null) {
            instance = new FeedbackInSleepingpill();
        }
        return instance;
    }

    private SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    @Override
    public void addFeedback(Feedback feedback,String talkLastModified) {
        String talkid = feedback.talkid;
        JsonArray talkFeedbacks = fetchCurrentFeedbacks(talkid);

        talkFeedbacks.add(feedback.asStoreJson());

        sendUpdatedFeedback(talkid, talkFeedbacks,talkLastModified);
    }

    private JsonObject sendUpdatedFeedback(String talkid, JsonArray talkFeedbacks,String talkLastModified) {
        JsonObject input = JsonFactory.jsonObject()
                .put("pkomfeedbacks", jsonObject().put("value", talkFeedbacks).put("privateData", true));
        return sleepingpillCommunicator.sendTalkUpdate(talkid,jsonObject().put("data",input).put("lastUpdated",talkLastModified));
    }

    private JsonArray fetchCurrentFeedbacks(String talkid) {
        JsonObject talkJson = sleepingpillCommunicator.oneTalkSleepingPillFormat(talkid);
        return talkJson.requiredObject("data")
                .objectValue("pkomfeedbacks").orElse(JsonFactory.jsonObject().put("value", JsonFactory.jsonArray()))
                .requiredArray("value");
    }

    @Override
    public String deleteFeedback(String talkRef, String id,String talkLastModified) {
        JsonArray currentFeedbacks = fetchCurrentFeedbacks(talkRef);
        JsonArray updated = JsonArray.fromNodeStream(currentFeedbacks.objectStream()
                .filter(feedback -> !Feedback.fromStoreJson(feedback).equals(id)));
        JsonObject jsonObject = sendUpdatedFeedback(talkRef, updated, talkLastModified);
        return jsonObject.requiredString("lastUpdated");

    }


    @Override
    public Stream<Feedback> feedbacksForTalk(String talkid) {
        JsonArray currentFeedbacks = fetchCurrentFeedbacks(talkid);
        return currentFeedbacks.objectStream()
                .map(Feedback::fromStoreJson);
    }
}
