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
                String correcttime = tslotobj.get().requiredString("time");
                LocalDateTime time = LocalDateTime.of(2016, 9, 9, 10, 10, 0, 0)
                        .withYear(Integer.parseInt(correcttime.substring(0,4)))
                        .withMonth(Integer.parseInt(correcttime.substring(4,6)))
                        .withDayOfMonth(Integer.parseInt(correcttime.substring(6,8)))
                        .withHour(Integer.parseInt(correcttime.substring(8,10)))
                        .withMinute(Integer.parseInt(correcttime.substring(10,12)));

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
                JsonFactory.jsonObject()
                        .put("time",time)
                        .put("duration",new JsonNumber(talkSlot.get().duration))
                        .put("display",talkSlot.get().getDisplay())
            );
        }
        if (room.isPresent()) {
            result.put("room",room.get());
        }
        return result;
    }

    @Override
    public String toString() {
        return "TalkSchedule{" +
                "talkid='" + talkid + '\'' +
                ", talkSlot=" + talkSlot +
                ", room=" + room +
                '}';
    }
}
