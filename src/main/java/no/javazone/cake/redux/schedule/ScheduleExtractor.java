package no.javazone.cake.redux.schedule;

import no.javazone.cake.redux.comments.Comment;
import no.javazone.cake.redux.comments.Feedback;
import no.javazone.cake.redux.comments.FeedbackDao;

import java.time.LocalDateTime;
import java.util.*;

public class ScheduleExtractor {
    private final TalkSceduleDao talkSceduleDao;
    private final FeedbackDao feedbackDao;

    public ScheduleExtractor(TalkSceduleDao talkSceduleDao, FeedbackDao feedbackDao) {
        this.talkSceduleDao = talkSceduleDao;
        this.feedbackDao = feedbackDao;
    }

    public void extractScheduleFromComments() {
        List<Feedback> feedbacks = feedbackDao.allFeedbacks();
        feedbacks.stream()
                .filter(fb -> fb instanceof Comment)
                .map(fb -> (Comment) fb)
                .filter(ScheduleExtractor::isRoomComment)
                .forEach(comment -> {
                    Optional<TalkSchedule> exsisting = talkSceduleDao.getSchedule(comment.talkid);
                    Optional<String> roomVal = Optional.of(computeRom(comment.talkComment));
                    TalkSchedule ts = exsisting
                            .map(exs -> new TalkSchedule(exs.talkid, exs.talkSlot, roomVal))
                            .orElse(new TalkSchedule(comment.talkid, Optional.empty(), roomVal));
                    talkSceduleDao.updateSchedule(ts);
                });
        feedbacks.stream()
                .filter(fb -> fb instanceof Comment)
                .map(fb -> (Comment) fb)
                .filter(ScheduleExtractor::isTimeComment)
                .forEach(comment -> {
                    TalkSlot talkSlot = computeSlot(comment);
                    if (talkSlot == null) {
                        return;
                    }
                    Optional<TalkSchedule> exsisting = talkSceduleDao.getSchedule(comment.talkid);
                    if (exsisting.filter(ex -> ex.talkSlot.isPresent()).isPresent()) {
                        return;
                    }
                    TalkSchedule ts = exsisting
                            .map(exs -> new TalkSchedule(exs.talkid, Optional.of(talkSlot), exs.room))
                            .orElse(new TalkSchedule(comment.talkid, Optional.of(talkSlot), Optional.empty()));
                    talkSceduleDao.updateSchedule(ts);
                });

    }

    private static class RoomStart {
        private final String room;
        private final LocalDateTime start;

        public RoomStart(String room, LocalDateTime start) {
            this.room = room;
            this.start = start;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RoomStart)) return false;
            RoomStart roomStart = (RoomStart) o;
            return Objects.equals(room, roomStart.room) &&
                    Objects.equals(start, roomStart.start);
        }

        @Override
        public int hashCode() {
            return Objects.hash(room, start);
        }

        @Override
        public String toString() {
            return "RoomStart{" +
                    "room='" + room + '\'' +
                    ", start=" + start +
                    '}';
        }
    }

    private void fixLightning() {
        List<Feedback> feedbacks = feedbackDao.allFeedbacks();
        Map<String,String> rooms = new HashMap<>();
        feedbacks.stream()
                .filter(fb -> fb instanceof Comment)
                .map(fb -> (Comment) fb)
                .filter(ScheduleExtractor::isRoomComment)
                .forEach(comment -> {
                    rooms.put(comment.talkid,computeRom(comment.talkComment));
                });
        Map<RoomStart,List<TalkSchedule>> schedule = new HashMap<>();
        feedbacks.stream()
                .filter(fb -> fb instanceof Comment)
                .map(fb -> (Comment) fb)
                .filter(ScheduleExtractor::isTimeComment)
                .forEach(comment -> {
                    String room = rooms.get(comment.talkid);
                    if (room == null) {
                        return;
                    }
                    TalkSlot talkSlot = computeSlot(comment);
                    if (talkSlot == null)  {
                        return;
                    }
                    if (talkSlot.duration > 20) {
                        return;
                    }
                    TalkSchedule talkSchedule = new TalkSchedule(comment.talkid, Optional.of(talkSlot), Optional.of(room));
                    RoomStart roomStart = new RoomStart(room, talkSlot.time);

                    List<TalkSchedule> oneSlot = schedule.get(roomStart);
                    if (oneSlot == null) {
                        oneSlot = new ArrayList<>();
                        schedule.put(roomStart,oneSlot);
                    }
                    oneSlot.add(talkSchedule);
                });

        int numup = 0;
        for (List<TalkSchedule> entry : schedule.values()) {
            LocalDateTime time = entry.get(0).talkSlot.get().time;
            for (TalkSchedule talkSchedule : entry) {
                int talkDur = talkSchedule.talkSlot.get().duration;
                TalkSlot ts = new TalkSlot(time, talkDur);
                TalkSchedule updated = new TalkSchedule(talkSchedule.talkid, Optional.of(ts), talkSchedule.room);
                numup++;
                talkSceduleDao.updateSchedule(updated);
                time = time.plusMinutes(talkDur);
            }
        }

        System.out.println("done upd " + numup);

    }

    private static boolean isTimeComment(Comment co) {
        String talkComment = co.talkComment;
        if (talkComment.toLowerCase().contains("tid:")) {
            return true;
        }
        for (String startTest : Arrays.asList("ons_","tors_","tor_")) {
            if (talkComment.toLowerCase().startsWith(startTest)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRoomComment(Comment co) {
        String talkComment = co.talkComment;
        return talkComment.toLowerCase().contains("rom:") || talkComment.equalsIgnoreCase("rom7") || talkComment.equalsIgnoreCase("rom8");
    }

    private static String computeRom(String comment) {
        if (comment.equalsIgnoreCase("rom7")) {
            return "Room 7";
        }
        if (comment.equalsIgnoreCase("rom8")) {
            return "Room 8";
        }
        int stind = comment.toLowerCase().indexOf("rom:");
        int endind = comment.toLowerCase().indexOf("tid:");
        if (endind == -1) {
            endind = comment.length();
        }
        return "Room " + comment.substring(stind + "rom:".length(),endind).trim();
    }


    private static TalkSlot computeSlot(Comment comment) {
        return TalkSlot.computeSlot(comment.talkComment,comment.talkid);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Use config");
            return;
        }
        System.setProperty("cake-redux-config-file",args[0]);

        FeedbackDao feedbackDao = FeedbackDao.impl();

        TalkSceduleDao talkSceduleDao = TalkSceduleDao.getImpl();


        List<TalkSchedule> talkSchedules;
        /*
        talkSchedules = talkSceduleDao.allScedules();
        if (!talkSchedules.isEmpty()) {
            System.out.println("Dao already has schedules");
            return;
        }*/

        ScheduleExtractor scheduleExtractor = new ScheduleExtractor(talkSceduleDao, feedbackDao);
        //scheduleExtractor.extractScheduleFromComments();
        scheduleExtractor.fixLightning();



        talkSchedules = talkSceduleDao.allScedules();
        System.out.println("Done read " + talkSchedules.size());
        //System.out.println(talkSchedules);
        /*
        List<String> allRefs = talkSchedules.stream()
                .map(ts -> ts.talkid)
                .distinct()
                .collect(Collectors.toList());

        TalkScheduleGrid talkScheduleGrid = TalkScheduleService.get().makeGrid(allRefs, Collections.emptyList(), Collections.emptyList());
        talkScheduleGrid.asHtmlTable(System.out);
        */

    }


}
