package no.javazone.cake.redux.schedule;

import no.javazone.cake.redux.Configuration;

import java.util.List;
import java.util.Optional;

public interface TalkSceduleDao {
    void updateSchedule(TalkSchedule talkSchedule);
    Optional<TalkSchedule> getSchedule(String talkid);
    List<TalkSchedule> allScedules();

    static TalkSceduleDao getImpl() {
        if (Configuration.scheduleDBFileName() != null) {
            return TalkScheduleDaoFileDbImpl.get();
        }
        return TalkScheduleDaoMemImpl.get();
    }
}
