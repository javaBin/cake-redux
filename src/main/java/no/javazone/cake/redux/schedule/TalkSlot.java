package no.javazone.cake.redux.schedule;

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
}
