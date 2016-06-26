package no.javazone.cake.redux.schedule;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class TalkSlot {
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
}
