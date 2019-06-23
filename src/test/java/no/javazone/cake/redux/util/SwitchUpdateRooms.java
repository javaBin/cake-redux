package no.javazone.cake.redux.util;

import no.javazone.cake.redux.UserAccessType;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import java.util.List;
import java.util.stream.Collectors;

public class SwitchUpdateRooms {
    private SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Use arg");
            return;
        }
        System.setProperty("cake-redux-config-file", args[0]);
        new SwitchUpdateRooms().doStuff();
    }

    private String newRoom(String oldroom) {
        switch (oldroom) {
            //case "Room 3": return "Room 1";
            case "Room 1": return "Room 3";
            //case "Room 4": return "Room 5";
            //case "Room 5": return "Room 7";
            //case "Room 7": return "Room 4";
        }
        return null;
    }

    private void doStuff() {
        JsonArray allTalks = JsonParser.parseToArray(sleepingpillCommunicator.talkShortVersion("99f71831-fdd3-41e3-962e-f25af5e091b9"));
        List<JsonObject> all = allTalks.objectStream().collect(Collectors.toList());
        int left = all.size();
        for (JsonObject talkobj : all) {
            left--;
            if (left%30 == 0) {
                System.out.println("Left " + left);
            }
            if (!"APPROVED".equals(talkobj.stringValue("state").orElse(""))) {
                continue;
            }
            if (!"presentation".equals(talkobj.stringValue("format").orElse(""))) {
                continue;
            }
            String currentroom = talkobj.objectValue("room").orElse(new JsonObject()).stringValue("name").orElse("");
            String updatedroom = newRoom(currentroom);
            if (updatedroom == null) {
                continue;
            }
            sleepingpillCommunicator.updateRoom(talkobj.requiredString("ref"),updatedroom, UserAccessType.FULL);

        }
        System.out.println("done");


    }

}
