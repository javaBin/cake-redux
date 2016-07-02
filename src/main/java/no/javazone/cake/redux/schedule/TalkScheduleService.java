package no.javazone.cake.redux.schedule;

import no.javazone.cake.redux.EmsCommunicator;
import org.jsonbuddy.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

public class TalkScheduleService {
    public static TalkScheduleService get() {
        return new TalkScheduleService();
    }

    private static final Map<String,String> talkTitlesCache = new HashMap<>();

    private final EmsCommunicator emsCommunicator = new EmsCommunicator();

    public TalkScheduleGrid makeGrid(List<String> talkReferences,List<String> wantedRooms,List<String> wantedSlots) {
        TalkSceduleDao talkSceduleDao = TalkSceduleDao.getImpl();
        Map<String,TalkSchedule> assignments = new HashMap<>();
        Map<String,Set<String>> assignedRooms = new HashMap<>();
        Map<String,Set<String>> assignedSlots = new HashMap<>();
        for (String ref : talkReferences) {
            Optional<TalkSchedule> scheduleOpt = talkSceduleDao.getSchedule(ref);
            TalkSchedule talkSchedule = scheduleOpt.orElse(new TalkSchedule(ref, Optional.empty(), Optional.empty()));
            assignments.put(ref, talkSchedule);

            String roomKey = talkSchedule.room.orElse("No room");
            putVal(assignedRooms,roomKey,ref);
            String slotKey = talkSchedule.talkSlot.map(TalkSlot::getDisplay).orElse("No slot");
            putVal(assignedSlots,slotKey,ref);

        }

        Set<String> usedRoomsSet = new HashSet<>();
        usedRoomsSet.addAll(assignedRooms.keySet());
        usedRoomsSet.addAll(wantedRooms);

        List<String> usedRooms = new ArrayList<>(usedRoomsSet);
        usedRooms.sort(String::compareTo);

        Set<String> usedSlotsSet = new HashSet<>();
        usedSlotsSet.addAll(assignedSlots.keySet());
        usedSlotsSet.addAll(wantedSlots);
        List<String> usedSlots = new ArrayList<>(usedSlotsSet);
        usedSlots.sort(String::compareTo);

        List<TalkScheduleRow> rows = new ArrayList<>();
        for (String slot : usedSlots) {
            List<TalkScheduleCell> schedules = new ArrayList<>();
            Set<String> rowSlots = Optional.ofNullable(assignedSlots.get(slot)).orElse(Collections.emptySet());
            for (String room : usedRooms) {
                Set<String> colSlots = Optional.ofNullable(assignedRooms.get(room)).orElse(Collections.emptySet());

                Set<String> cellSlots = new HashSet<>(colSlots);
                cellSlots.retainAll(rowSlots);
                List<TalkScheduleCellDisplay> displays = cellSlots.stream()
                        .map(ref -> new TalkScheduleCellDisplay(readTitle(ref), ref)).collect(Collectors.toList());
                schedules.add(new TalkScheduleCell(displays));
            }
            rows.add(new TalkScheduleRow(slot,schedules));
        }
        return new TalkScheduleGrid(usedRooms,rows);

    }

    private String readTitle(String ref) {
        String title;
        synchronized (talkTitlesCache) {
            title = talkTitlesCache.get(ref);
        }
        if (title != null) {
            return title;
        }
        JsonObject jsonObject = emsCommunicator.oneTalkAsJson(ref);
        title = jsonObject.requiredString("title");
        synchronized (talkTitlesCache) {
            talkTitlesCache.put(ref,title);
        }
        return title;
    }

    private void putVal(Map<String, Set<String>> map, String key,String ref) {
        Set<String> vals = map.get(key);
        if (vals == null) {
            vals = new HashSet<>();
            map.put(key,vals);
        }
        vals.add(ref);
    }
}
