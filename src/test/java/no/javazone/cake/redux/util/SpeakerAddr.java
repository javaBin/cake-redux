package no.javazone.cake.redux.util;

import no.javazone.cake.redux.Configuration;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SpeakerAddr {
    private SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("configfile");
            return;
        }
        Configuration.setConfigFile(args);
        SpeakerAddr speakerAddr = new SpeakerAddr();
        //speakerAddr.doApproved();
        speakerAddr.doAll();
    }

    private void doAll() {
        Set<SpeakerObj> allSpeakers = allSpeakersAllconferences();
        for (SpeakerObj so : allSpeakers) {
            System.out.println(so.email + ";" + so.name);
        }
    }

    public Set<SpeakerObj> allSpeakersAllconferences() {
        JsonArray allEvents = JsonParser.parseToArray(sleepingpillCommunicator.allEvents());
        Set<SpeakerObj> allSpeakers = new HashSet<>();
        List<String> allConf = allEvents.objectStream().map(ob -> ob.requiredString("ref")).collect(Collectors.toList());
        for (String confid : allConf) {
            JsonArray jsonArray = sleepingpillCommunicator.allTalkFromConferenceSleepingPillFormat(confid);
            jsonArray.objectStream()
                    .flatMap(ob -> ob.requiredArray("speakers").objectStream())
                    .map(this::toSpeker)
                    .forEach(allSpeakers::add);
        }
        return allSpeakers;
    }

    private void doApproved() {
        JsonArray jsonArray = sleepingpillCommunicator.allTalkFromConferenceSleepingPillFormat(Configuration.videoAdminConference());
        jsonArray.objectStream()
                .filter(this::isApprovedPres)
                .flatMap(ob -> ob.requiredArray("speakers").objectStream())
                .map(this::toExcelRow)
                .sorted()
                .forEach(System.out::println);
    }

    public static class SpeakerObj implements Comparable<SpeakerObj> {
        public final String email;
        public final String name;


        public SpeakerObj(String email, String name) {
            this.email = email;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SpeakerObj)) return false;
            SpeakerObj that = (SpeakerObj) o;
            return Objects.equals(email, that.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email);
        }

        @Override
        public int compareTo(SpeakerObj o) {
            return this.email.compareTo(o.email);
        }
    }

    private SpeakerObj toSpeker(JsonObject jsonObject) {
        return new SpeakerObj(jsonObject.requiredString("email"),jsonObject.requiredString("name"));
    }

    private String toExcelRow(JsonObject jsonObject) {
        return String.format("%s;%s",jsonObject.requiredString("name"),jsonObject.requiredString("email"));
    }

    private boolean isApprovedPres(JsonObject jsonObject) {
        if (!"APPROVED".equals(jsonObject.requiredString("status"))) {
            return false;
        }
        return true;
        //String format = jsonObject.requiredObject("data").requiredObject("format").requiredString("value");
        //return "workshop".equals(format);
    }
}
