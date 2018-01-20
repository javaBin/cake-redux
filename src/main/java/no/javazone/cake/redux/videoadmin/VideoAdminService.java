package no.javazone.cake.redux.videoadmin;

import no.javazone.cake.redux.Configuration;
import no.javazone.cake.redux.UserAccessType;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;

public class VideoAdminService {
    private static VideoAdminService instance = new VideoAdminService();

    public static VideoAdminService get() {
        return instance;
    }

    private final SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    public JsonArray all() {
        JsonArray jsonArray = sleepingpillCommunicator.allTalkFromConferenceSleepingPillFormat(Configuration.videoAdminConference());
        JsonArray result = JsonArray.fromNodeStream(jsonArray.objectStream()
                .filter(session -> "APPROVED".equals(session.requiredString("status")))
                .map(this::toVideoFormat)
                .sorted(this::sortSessions)
        );
        return result;
    }

    public JsonObject one(String id) {
        JsonObject talkSleepingPillFormat = sleepingpillCommunicator.oneTalkSleepingPillFormat(id);
        return toVideoFormat(talkSleepingPillFormat);
    }

    private int sortSessions(JsonObject a, JsonObject b) {
        return a.requiredString("title").compareTo(b.requiredString("title"));
    }

    private JsonObject toVideoFormat(JsonObject session) {
        JsonObject dataObject = session.requiredObject("data");
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("id", session.requiredString("id"))
                .put("title", dataObject.requiredObject("title").requiredString("value"))
                .put("video",dataObject.objectValue("video").map(ob -> ob.requiredString("value")).orElse(""))
                ;

        return jsonObject;
    }

    public void update(String id, String video) {
        sleepingpillCommunicator.updateVideo(id,video);
        sleepingpillCommunicator.pubishChanges(id, UserAccessType.WRITE);
    }
}
