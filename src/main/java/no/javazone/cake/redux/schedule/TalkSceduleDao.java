package no.javazone.cake.redux.schedule;

import java.util.List;
import java.util.Optional;

public interface TalkSceduleDao {
    void updateSchedule(TalkSchedule talkSchedule);
    Optional<TalkSchedule> getSchedule(String talkid);
    List<TalkSchedule> allScedules();
}
