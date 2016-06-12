package no.javazone.cake.redux.schedule;

import java.time.LocalDateTime;

public class TalkSlot {
    public final LocalDateTime time;
    public final int duration;

    public TalkSlot(LocalDateTime time, int duration) {
        this.time = time;
        this.duration = duration;
    }
}
