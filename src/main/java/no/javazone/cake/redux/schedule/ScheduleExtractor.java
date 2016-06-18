package no.javazone.cake.redux.schedule;

import no.javazone.cake.redux.EmsCommunicator;
import no.javazone.cake.redux.comments.Comment;
import no.javazone.cake.redux.comments.Feedback;
import no.javazone.cake.redux.comments.FeedbackDao;
import no.javazone.cake.redux.comments.FeedbackDaoFileImpl;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
                .filter(co -> co.talkComment.toLowerCase().startsWith("rom:"))
                .forEach(comment -> {
                    Optional<TalkSchedule> exsisting = talkSceduleDao.getSchedule(comment.talkid);
                    Optional<String> talkComment = Optional.of(comment.talkComment);
                    TalkSchedule ts = exsisting
                            .map(exs -> new TalkSchedule(exs.talkid, exs.talkSlot, talkComment))
                            .orElse(new TalkSchedule(comment.talkid, Optional.empty(), talkComment));
                    talkSceduleDao.updateSchedule(ts);
                });
        feedbacks.stream()
                .filter(fb -> fb instanceof Comment)
                .map(fb -> (Comment) fb)
                .filter(co -> co.talkComment.toLowerCase().startsWith("tid:"))
                .forEach(comment -> {
                    TalkSlot talkSlot = computeSlot(comment);
                    if (talkSlot == null) {
                        return;
                    }
                    Optional<TalkSchedule> exsisting = talkSceduleDao.getSchedule(comment.talkid);
                    TalkSchedule ts = exsisting
                            .map(exs -> new TalkSchedule(exs.talkid, Optional.of(talkSlot), exs.room))
                            .orElse(new TalkSchedule(comment.talkid, Optional.of(talkSlot), Optional.empty()));
                    talkSceduleDao.updateSchedule(ts);
                });

    }

    private TalkSlot computeSlot(Comment comment) {
        LocalDateTime wednesday = LocalDateTime.of(2016,9,7,1,1);
        LocalDateTime thursday = LocalDateTime.of(2016,9,8,1,1);
        LocalDateTime start;
        if (comment.talkComment.toLowerCase().startsWith("tid: ons_")) {
            start = wednesday;
        } else if (comment.talkComment.toLowerCase().startsWith("tid: tor_")) {
            start = thursday;
        } else {
            return null;
        }
        int hour;
        int min;
        try {
            hour = Integer.parseInt(comment.talkComment.substring(9, 11));
            min = Integer.parseInt(comment.talkComment.substring(11));
        } catch (Exception e) {
            return null;
        }
        start = start.withHour(hour);
        start = start.withMinute(min);
        JsonObject jsonObject = new EmsCommunicator().oneTalkAsJson(comment.talkid);
        JsonArray tags = jsonObject.requiredArray("tags");
        Integer duration = tags.stringStream()
                .filter(s -> s.startsWith("len") && s.length() > 3)
                .filter(s -> {
                    try {
                        Integer.parseInt(s.substring(3));
                        return true;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                })
                .map(s -> Integer.parseInt(s.substring(3)))
                .findAny()
                .orElse(60);
        return new TalkSlot(start,duration);
    }

    public static void main(String[] args) {
        System.setProperty("cake-redux-config-file",args[0]);
        TalkSceduleDao talkSceduleDao = new TalkScheduleDaoMemImpl();
        FeedbackDao feedbackDao = FeedbackDaoFileImpl.get();

        ScheduleExtractor scheduleExtractor = new ScheduleExtractor(talkSceduleDao, feedbackDao);
        scheduleExtractor.extractScheduleFromComments();

        List<TalkSchedule> talkSchedules = talkSceduleDao.allScedules();
        System.out.println(talkSchedules);
    }
}
