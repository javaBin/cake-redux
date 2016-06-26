package no.javazone.cake.redux.schedule;

import java.util.List;

public class TalkScheduleCell {
    public final List<TalkScheduleCellDisplay> contents;

    public TalkScheduleCell(List<TalkScheduleCellDisplay> contents) {
        this.contents = contents;
    }
}
