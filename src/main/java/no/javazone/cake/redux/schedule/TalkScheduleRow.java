package no.javazone.cake.redux.schedule;

import java.util.List;

public class TalkScheduleRow {
    public final String displaySlot;
    public final List<TalkScheduleCell> cells;

    public TalkScheduleRow(String displaySlot, List<TalkScheduleCell> cells) {
        this.displaySlot = displaySlot;
        this.cells = cells;
    }

    @Override
    public String toString() {
        return "TalkScheduleRow{" +
                "displaySlot='" + displaySlot + '\'' +
                ", cells=" + cells +
                '}';
    }
}
