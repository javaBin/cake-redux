package no.javazone.cake.redux.schedule;

import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNumber;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.JsonPojoBuilder;
import org.jsonbuddy.pojo.OverrideMapper;
import org.jsonbuddy.pojo.OverridesJsonGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@OverrideMapper(using = TalkSchedule.Mapper.class)
public class TalkSchedule implements OverridesJsonGenerator {
    public final String talkid;
    public final Optional<TalkSlot> talkSlot;
    public final Optional<String> room;

    public TalkSchedule(String talkid, Optional<TalkSlot> talkSlot, Optional<String> room) {
        this.talkid = talkid;
        this.talkSlot = talkSlot;
        this.room = room;
    }

    public static class Mapper implements JsonPojoBuilder<TalkSchedule> {

        @Override
        public TalkSchedule build(JsonNode node) {
            JsonObject jsonObject = (JsonObject) node;
            String talkid = jsonObject.requiredString("talkid");

            Optional<JsonObject> tslotobj = jsonObject.objectValue("talkSlot");
            Optional<TalkSlot> talkSlot = Optional.empty();
            if (tslotobj.isPresent()) {
                DateTimeFormatter pattern = DateTimeFormatter.ofPattern("YYYYMMddHHmm");
                LocalDateTime time = LocalDateTime.parse(tslotobj.get().requiredString("time"), pattern);
                int duration = (int) tslotobj.get().requiredLong("duration");
                talkSlot = Optional.of(new TalkSlot(time,duration));
            }

            Optional<String> room = jsonObject.stringValue("room");

            return new TalkSchedule(talkid, talkSlot, room);
        }
    }

    @Override
    public JsonNode jsonValue() {
        JsonObject result = JsonFactory.jsonObject()
                .put("talkid", this.talkid);
        if (talkSlot.isPresent()) {
            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("YYYYMMddHHmm");
            String time = talkSlot.get().time.format(pattern);
            result.put("talkSlot",
                JsonFactory.jsonObject().put("time",time).put("duration",new JsonNumber(talkSlot.get().duration)));
        }
        if (room.isPresent()) {
            result.put("room",room.get());
        }
        return result;
    }
}
