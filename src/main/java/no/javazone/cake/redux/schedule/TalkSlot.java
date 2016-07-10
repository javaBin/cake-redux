package no.javazone.cake.redux.schedule;

import no.javazone.cake.redux.EmsCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

public class TalkSlot implements Comparable<TalkSlot> {
    public final LocalDateTime time;
    public final int duration;

    public TalkSlot(LocalDateTime time, int duration) {
        this.time = time;
        this.duration = duration;
    }



    @Override
    public String toString() {
        return "TalkSlot{" +
                "time=" + time +
                ", duration=" + duration +
                '}';
    }

    public String getDisplay() {
        StringBuilder res = new StringBuilder();
        res.append(time.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        res.append(" ");
        res.append(padZeros(time.getHour(),2));
        res.append(padZeros(time.getMinute(),2));
        res.append(" ");
        res.append(duration);
        res.append(" min");
        return res.toString();

    }

    private static String padZeros(int val,int len) {
        String res = "" + val;
        while (res.length() < len) {
            res = "0" + res;
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TalkSlot)) return false;
        TalkSlot talkSlot = (TalkSlot) o;
        return duration == talkSlot.duration &&
                Objects.equals(time, talkSlot.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, duration);
    }

    @Override
    public int compareTo(TalkSlot o) {
        int comp = time.compareTo(o.time);
        if (comp != 0) {
            return comp;
        }
        return new Integer(duration).compareTo(new Integer(o.duration));
    }

    public static TalkSlot computeSlot(String comment, String ref) {
        String talkComment = comment;
        talkComment = talkComment.substring(talkComment.toLowerCase().indexOf("tid:")).toLowerCase();

        LocalDateTime tuesday = LocalDateTime.of(2016,9,6,1,1);
        LocalDateTime wednesday = LocalDateTime.of(2016,9,7,1,1);
        LocalDateTime thursday = LocalDateTime.of(2016,9,8,1,1);
        LocalDateTime start;
        int matchStart;
        if (talkComment.toLowerCase().startsWith("tid: tir_")) {
            matchStart = 9;
            start = tuesday;
        } else if (talkComment.toLowerCase().startsWith("tid: tirsdag_")) {
            matchStart = 12;
            start = tuesday;
        } else if (talkComment.toLowerCase().startsWith("tid: ons_")) {
            start = wednesday;
            matchStart = 9;
        } else if (talkComment.toLowerCase().startsWith("tid: onsdag_")) {
            matchStart = 12;
            start = thursday;
        } else if (talkComment.toLowerCase().startsWith("tid: tor_")) {
            matchStart = 9;
            start = thursday;
        } else if (talkComment.toLowerCase().startsWith("tid: tors_")) {
            matchStart = 10;
            start = thursday;
        } else if (talkComment.toLowerCase().startsWith("tid: torsdag_")) {
            matchStart = 12;
            start = thursday;
        } else {
            System.out.println("Could not compute slot of " + comment + " ref " + ref);
            return null;
        }
        int hour;
        int min;
        try {
            hour = Integer.parseInt(talkComment.substring(matchStart, matchStart+2));
            min = Integer.parseInt(talkComment.substring(matchStart+2));
        } catch (Exception e) {
            return null;
        }
        start = start.withHour(hour);
        start = start.withMinute(min);
        JsonObject jsonObject = new EmsCommunicator().oneTalkAsJson(ref);
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
}
