package no.javazone.cake.redux.schedule;

import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;

import java.util.Optional;

public class TalkScheduleDaoFileImpl implements TalkSceduleDao {
    private static transient TalkScheduleDaoFileImpl instance = null;
    private static final JsonObject store = JsonFactory.jsonObject();

    public static synchronized TalkScheduleDaoFileImpl get() {
        if (instance == null) {
            instance = new TalkScheduleDaoFileImpl();
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
}
