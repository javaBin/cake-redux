package no.javazone.cake.redux.util;

import no.javazone.cake.redux.Configuration;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

public class SpeakerAddr {
    private SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("configfile");
            return;
        }
        Configuration.setConfigFile(args);
        SpeakerAddr speakerAddr = new SpeakerAddr();
        speakerAddr.doit();
    }

    private void doit() {
        JsonArray jsonArray = sleepingpillCommunicator.allTalkFromConferenceSleepingPillFormat(Configuration.videoAdminConference());
        jsonArray.objectStream()
                .filter(this::isApprovedPres)
                .flatMap(ob -> ob.requiredArray("speakers").objectStream())
                .map(this::toExcelRow)
                .sorted()
                .forEach(System.out::println);
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
