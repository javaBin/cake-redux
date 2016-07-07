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


    private static class ComutedSchedule {
        private final List<OptTalkSlot> usedSlots;
        private final List<OptRoom> usedRooms;
        private final Map<OptRoom,Set<TalkSchedule>> assignedRooms;
        private final Map<OptTalkSlot, Set<TalkSchedule>> assignedSlots;

        public ComutedSchedule(List<OptTalkSlot> usedSlots, List<OptRoom> usedRooms, Map<OptRoom, Set<TalkSchedule>> assignedRooms, Map<OptTalkSlot, Set<TalkSchedule>> assignedSlots) {
            this.usedSlots = usedSlots;
            this.usedRooms = usedRooms;
            this.assignedRooms = assignedRooms;
            this.assignedSlots = assignedSlots;
        }
    }

    private static class OptTalkSlot implements Comparable<OptTalkSlot> {
        private final Optional<TalkSlot> talkSlot;

        public OptTalkSlot(Optional<TalkSlot> talkSlot) {
            this.talkSlot = talkSlot;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OptTalkSlot)) return false;
            OptTalkSlot that = (OptTalkSlot) o;
            return Objects.equals(talkSlot, that.talkSlot);
        }

        @Override
        public int hashCode() {
            return Objects.hash(talkSlot);
        }


        @Override
        public int compareTo(OptTalkSlot o) {
            if (!(talkSlot.isPresent() || o.talkSlot.isPresent())) {
                return 0;
            }
            if (talkSlot.isPresent() && o.talkSlot.isPresent()) {
                return talkSlot.get().compareTo(o.talkSlot.get());
            }
            if (!talkSlot.isPresent()) {
                return -1;
            }
            return 1;
        }

        public String display() {
            return talkSlot.map(TalkSlot::getDisplay).orElse("No slot");
        }
    }

    private static class OptRoom implements Comparable<OptRoom> {
        private final Optional<String> room;

        public OptRoom(Optional<String> room) {
            this.room = room;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OptRoom)) return false;
            OptRoom optRoom = (OptRoom) o;
            return Objects.equals(room, optRoom.room);
        }

        @Override
        public int hashCode() {
            return Objects.hash(room);
        }


        @Override
        public int compareTo(OptRoom o) {
            if (!(room.isPresent() || o.room.isPresent())) {
                return 0;
            }
            if (room.isPresent() && o.room.isPresent()) {
                return room.get().compareTo(o.room.get());
            }
            if (!room.isPresent()) {
                return -1;
            }
            return 1;
        }

        public String display() {
            return room.orElse("No room");
        }
    }

    private ComutedSchedule comutedSchedule(List<String> talkReferences,List<String> wantedRooms,List<TalkSlot> wantedSlots) {
        TalkSceduleDao talkSceduleDao = TalkSceduleDao.getImpl();
        Map<OptRoom,Set<TalkSchedule>> assignedRooms = new HashMap<>();
        Map<OptTalkSlot,Set<TalkSchedule>> assignedSlots = new HashMap<>();
        for (String ref : talkReferences) {
            Optional<TalkSchedule> scheduleOpt = talkSceduleDao.getSchedule(ref);
            TalkSchedule talkSchedule = scheduleOpt.orElse(new TalkSchedule(ref, Optional.empty(), Optional.empty()));

            OptRoom roomKey = new OptRoom(talkSchedule.room);
            putVal(assignedRooms,roomKey,talkSchedule);
            putVal(assignedSlots,new OptTalkSlot(talkSchedule.talkSlot),talkSchedule);

        }

        Set<OptRoom> usedRoomsSet = new HashSet<>();
        usedRoomsSet.addAll(assignedRooms.keySet());
        usedRoomsSet.addAll(wantedRooms.stream().map(ro -> new OptRoom(Optional.of(ro))).collect(Collectors.toList()));

        List<OptRoom> usedRooms = new ArrayList<>(usedRoomsSet);
        usedRooms.sort(OptRoom::compareTo);

        Set<OptTalkSlot> usedSlotsSet = new HashSet<>();
        usedSlotsSet.addAll(assignedSlots.keySet());
        usedSlotsSet.addAll(wantedSlots.stream().map(r-> new OptTalkSlot(Optional.of(r))).collect(Collectors.toList()));
        List<OptTalkSlot> usedSlots = new ArrayList<>(usedSlotsSet);
        usedSlots.sort(OptTalkSlot::compareTo);
        return new ComutedSchedule(usedSlots,usedRooms,assignedRooms,assignedSlots);
    }

    public TalkScheduleGrid makeGrid(List<String> talkReferences,List<String> wantedRooms,List<TalkSlot> wantedSlots) {
        ComutedSchedule comutedSchedule = comutedSchedule(talkReferences,wantedRooms,wantedSlots);
        List<TalkScheduleRow> rows = new ArrayList<>();
        for (OptTalkSlot slot : comutedSchedule.usedSlots) {
            List<TalkScheduleCell> schedules = new ArrayList<>();
            Set<TalkSchedule> rowSlots = Optional.ofNullable(comutedSchedule.assignedSlots.get(slot)).orElse(Collections.emptySet());
            for (OptRoom room : comutedSchedule.usedRooms) {
                Set<TalkSchedule> colSlots = Optional.ofNullable(comutedSchedule.assignedRooms.get(room)).orElse(Collections.emptySet());

                Set<TalkSchedule> cellSlots = new HashSet<>(colSlots);
                cellSlots.retainAll(rowSlots);
                List<TalkScheduleCellDisplay> displays = cellSlots.stream()
                        .map(ref -> new TalkScheduleCellDisplay(readTitle(ref.talkid), ref.talkid)).collect(Collectors.toList());
                schedules.add(new TalkScheduleCell(displays));
            }
            rows.add(new TalkScheduleRow(slot.display(),schedules));
        }
        return new TalkScheduleGrid(comutedSchedule.usedRooms.stream().map(OptRoom::display).collect(Collectors.toList()),rows);
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

    private <T> void putVal(Map<T, Set<TalkSchedule>> map, T key,TalkSchedule talkSchedule) {
        Set<TalkSchedule> vals = map.get(key);
        if (vals == null) {
            vals = new HashSet<>();
            map.put(key,vals);
        }
        vals.add(talkSchedule);
    }

    public TalkScheduleGrid move(List<String> talkReferences,List<String> wantedRooms,List<TalkSlot> wantedSlots,String ref,MoveDirection moveDirection) {
        ComutedSchedule comutedSchedule = comutedSchedule(talkReferences,wantedRooms,wantedSlots);
        int roomInd;
        for (roomInd = 0;roomInd<comutedSchedule.usedRooms.size();roomInd++) {
            if (comutedSchedule.assignedRooms.get(comutedSchedule.usedRooms.get(roomInd)).stream().filter(ts -> ts.talkid.equals(ref)).findAny().isPresent()) {
                break;
            }
        }
        int slotInd;
        for (slotInd = 0; slotInd<comutedSchedule.usedSlots.size();slotInd++) {
            if (comutedSchedule.assignedSlots.get(comutedSchedule.usedSlots.get(slotInd)).stream().filter(ts -> ts.talkid.equals(ref)).findAny().isPresent()) {
                break;
            }
        }
        switch (moveDirection) {
            case UP:
                slotInd--;
                break;
            case DOWN:
                slotInd++;
                break;
            case LEFT:
                roomInd--;
                break;
            case RIGHT:
                roomInd++;
                break;
            default:
                throw new RuntimeException("Unknown moveDirection " + moveDirection);
        }
        Optional<String> newRoom = comutedSchedule.usedRooms.get(roomInd).room;
        Optional<TalkSlot> newSlot = comutedSchedule.usedSlots.get(slotInd).talkSlot;

        TalkSchedule newTalkSchedule = new TalkSchedule(ref,newSlot,newRoom);
        TalkSceduleDao.getImpl().updateSchedule(newTalkSchedule);
        return makeGrid(talkReferences,wantedRooms,wantedSlots);

    }
}
