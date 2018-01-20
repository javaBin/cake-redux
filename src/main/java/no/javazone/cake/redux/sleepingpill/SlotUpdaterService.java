package no.javazone.cake.redux.sleepingpill;

import no.javazone.cake.redux.UserAccessType;
import org.apache.log4j.lf5.util.StreamUtils;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

public class SlotUpdaterService {
    private static final LocalDate DAY_ONE = LocalDate.of(2017,9,13);
    private final static SlotUpdaterService instance = new SlotUpdaterService();
    public static SlotUpdaterService get() {
        return instance;
    }


    private static class PossibleSlot implements Comparable<PossibleSlot> {
        public final String slotDescription;
        public final LocalDateTime start;

        public PossibleSlot(String slotDescription, LocalDateTime start) {
            this.slotDescription = slotDescription;
            this.start = start;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PossibleSlot)) return false;
            PossibleSlot that = (PossibleSlot) o;
            return Objects.equals(slotDescription, that.slotDescription);
        }

        @Override
        public int hashCode() {
            return Objects.hash(slotDescription);
        }

        @Override
        public int compareTo(PossibleSlot that) {
            if (this.slotDescription.equals(that.slotDescription)) {
                return 0;
            }
            if ("No slot".equals(this.slotDescription)) {
                return -1;
            }
            if ("No slot".equals(that.slotDescription)) {
                return 1;
            }
            String myDay = this.slotDescription.substring(0,3);
            if (!myDay.equals(that.slotDescription.substring(0,3))) {
                return  ("wed".equalsIgnoreCase(myDay)) ? -1 : 1;
            }
            return this.slotDescription.compareTo(that.slotDescription);
        }
    }

    private final SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    public JsonObject readDataOnTalks(JsonObject talkRefObj,UserAccessType userAccessType) {
        SleepingpillCommunicator.checkWriteAccess(userAccessType);
        List<String> possibleRooms = possibleRooms();
        List<PossibleSlot> possibleSlots = possibleSlots();
        JsonArray talksRef = talkRefObj.requiredArray("talks");
        JsonArray talks = JsonArray.fromNodeStream(talksRef.stringStream()
                .map(this::readTalkForSlotUpdate));

        JsonArray talkGrid = JsonFactory.jsonArray();

        for (PossibleSlot slot : possibleSlots) {
            JsonArray oneRow = JsonFactory.jsonArray();

            for (String room : possibleRooms) {
                JsonArray talkList = JsonArray.fromNodeStream(talks.objectStream()
                        .filter(talkob ->
                                room.equals(talkob.requiredString("room")) &&
                                        slot.slotDescription.equals(talkob.requiredString("slot")))
                        .sorted(Comparator.comparing(a -> a.requiredString("display"))));

                JsonObject cell = JsonFactory.jsonObject()
                        .put("room",room)
                        .put("slot",slot.slotDescription)
                        .put("talks",talkList);
                oneRow.add(cell);

            }

            talkGrid.add(JsonFactory.jsonObject().put("slot",slot.slotDescription).put("row",oneRow));
        }


        JsonObject result = JsonFactory.jsonObject();
        result.put("talkGrid",talkGrid);
        result.put("rooms", JsonArray.fromStringList(possibleRooms));
        return result;
    }

    private static List<PossibleSlot> possibleSlots() {
        List<PossibleSlot> result = new ArrayList<>();
        result.add(new PossibleSlot("No slot",null));
        for (int day=0;day<2;day++) {
            int numSlots = day==0 ? 8 :7;
            LocalDateTime start = DAY_ONE.plusDays(day).atTime(9,0);
            for (int i=0;i<numSlots;i++) {
                result.add(new PossibleSlot(formatSlot(start),start));
                start = start.plusMinutes(80);
            }
        }
        return result;
    }

    private static List<String> possibleRooms() {
        List<String> result = new ArrayList<>();
        result.add("No room");
        for (int i=1;i<8;i++) {
            result.add("Room " + i);
        }
        return result;
    }

    private JsonObject readTalkForSlotUpdate(String ref) {
        JsonObject spTalk = sleepingpillCommunicator.oneTalkSleepingPillFormat(ref);
        JsonObject talk = JsonFactory.jsonObject();
        talk.put("id", ref);
        JsonObject dataObj = spTalk.requiredObject("data");
        String title = readVal(dataObj, "title").orElseThrow(() -> new RuntimeException("No title"));
        String length = readVal(dataObj, "length").orElseThrow(() -> new RuntimeException("No length"));
        talk.put("display", String.format("%s (%s)", title, length));
        talk.put("slot", readVal(dataObj, "startTime").map(SlotUpdaterService::formatSlot).orElse("No slot"));
        talk.put("room", readVal(dataObj, "room").orElse("No room"));
        return talk;
    }

    private static String formatSlot(String spFormatTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(spFormatTime);
        return formatSlot(localDateTime);

    }

    private static String formatSlot(LocalDateTime localDateTime) {
        String res = String.format("%s %s:%s",
                localDateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                toToDigitDisplay(localDateTime.getHour()),
                toToDigitDisplay(localDateTime.getMinute())
        );
        return res;
    }

    private static String toToDigitDisplay(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return "" + i;
    }

    private static Optional<String> readVal(JsonObject dataObj, String key) {
        return dataObj.objectValue(key).map(ob -> ob.requiredString("value"));
    }

    public void updateRoomSlot(JsonObject update,UserAccessType userAccessType) {
        SleepingpillCommunicator.checkWriteAccess(userAccessType);
        String talkid = update.requiredString("id");
        JsonObject spTalk = sleepingpillCommunicator.oneTalkSleepingPillFormat(talkid);
        String newRoom = update.requiredString("room");
        String slotStr = update.requiredString("slot");

        List<PossibleSlot> possibleSlots = possibleSlots();

        PossibleSlot possibleSlot = possibleSlots.get(possibleSlots.indexOf(new PossibleSlot(slotStr, null)));
        int length = Integer.parseInt(spTalk.requiredObject("data").requiredObject("length").requiredString("value"));

        String startTime = possibleSlot.start.toString();
        String endTime = possibleSlot.start.plusMinutes(length).toString();

        JsonObject updatePayload = JsonFactory.jsonObject().put("data",
                JsonFactory.jsonObject()
                        .put("room", JsonFactory.jsonObject().put("privateData", false).put("value", newRoom))
                        .put("startTime", JsonFactory.jsonObject().put("privateData", false).put("value", startTime))
                        .put("endTime", JsonFactory.jsonObject().put("privateData", false).put("value", endTime))
        );
        sleepingpillCommunicator.sendTalkUpdate(talkid,updatePayload);


    }
}
