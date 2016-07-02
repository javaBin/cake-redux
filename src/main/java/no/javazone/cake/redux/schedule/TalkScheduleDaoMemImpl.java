package no.javazone.cake.redux.schedule;

import no.javazone.cake.redux.comments.FeedbackDao;
import no.javazone.cake.redux.comments.FeedbackDaoFileImpl;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TalkScheduleDaoMemImpl implements TalkSceduleDao {
    private static transient TalkScheduleDaoMemImpl instance = null;
    private static final JsonObject store = JsonFactory.jsonObject();

    public static synchronized TalkScheduleDaoMemImpl get() {
        if (instance == null) {
            instance = new TalkScheduleDaoMemImpl();
            FeedbackDao feedbackDao = FeedbackDaoFileImpl.get();

            ScheduleExtractor scheduleExtractor = new ScheduleExtractor(instance, feedbackDao);
            scheduleExtractor.extractScheduleFromComments();


        }
        return instance;
    }

    @Override
    public void updateSchedule(TalkSchedule talkSchedule) {
        JsonNode jsonNode = talkSchedule.jsonValue();
        synchronized (store) {
            store.put(talkSchedule.talkid,jsonNode);
        }
    }

    @Override
    public Optional<TalkSchedule> getSchedule(String talkid) {
        synchronized (store) {
            return store.objectValue(talkid).map(jo -> new TalkSchedule.Mapper().build(jo));
        }
    }

    @Override
    public List<TalkSchedule> allScedules() {
        synchronized (store) {
            return store.keys().stream()
                    .map(key -> new TalkSchedule.Mapper().build(store.requiredObject(key)))
                    .collect(Collectors.toList());
        }


    }
}
