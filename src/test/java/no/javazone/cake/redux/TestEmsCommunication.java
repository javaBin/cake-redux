package no.javazone.cake.redux;

import java.util.Arrays;
import java.util.List;

public class TestEmsCommunication {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        if (args == null || args.length < 1) {
            System.out.println("need configfile");
            return;
        }
        System.setProperty("cake-redux-config-file",args[0]);
        //updateTagsOnEvent();
        //publishTalk();
        //testAllTaksLight();
        //testAllRooms();
        //testPublishRoom();
        //readOneTalk();
        //new EmsCommunicator().addRoomToEvent("http://test.javazone.no/ems/server/events/aad84b5a-b527-45d4-b532-c5be1f25c1d0","Room 1");
        new EmsCommunicator().addSlotToEvent(UserAccessType.FULL);
        long duration = System.currentTimeMillis() -start;
        System.out.println("Took " + duration);
    }

    private static void testPublishRoom() {
        String encodedTalk = "aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy9jZWUzN2NjMS01Mzk5LTQ3ZWYtOTQxOC0yMWY5YjY0NDRiZmEvc2Vzc2lvbnMvZWQ2Y2RiNzctZTVlMy00MGQ2LWE0NjktODRkYzhkMTA5ZGM1";
        String room = "http://test.2014.javazone.no/ems/server/events/cee37cc1-5399-47ef-9418-21f9b6444bfa/rooms/893b1b52-d158-4125-9725-34039abaf6a4";
        //String room = "http://test.2014.javazone.no/ems/server/events/cee37cc1-5399-47ef-9418-21f9b6444bfa/slots/c92511fc-5063-4054-b55d-6d5cd78b5e18";
        new EmsCommunicator().assignRoom(encodedTalk,room,"xxx",UserAccessType.FULL);
    }

    private static void testAllRooms() {
        String res=new EmsCommunicator().allRoomsAndSlots("aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWY");
        System.out.println(res);
    }

    private static void updateTagsOnEvent() {
        String talkEvent="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWYvc2Vzc2lvbnMvOWQzMWVmZGYtN2MzMi00ZDg1LWEyYjUtYjM2YmVlZjMyYzQ0";
        List<String> tags = Arrays.asList("testTag");
        System.out.println(new EmsCommunicator().updateTags(talkEvent,tags,"",UserAccessType.FULL));
    }

    private static void testAllTaks() {
        String event2014Text = "aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWY=";
        String event2013Text ="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy9jZWUzN2NjMS01Mzk5LTQ3ZWYtOTQxOC0yMWY5YjY0NDRiZmE=";
        System.out.println(new EmsCommunicator().talksFullVersion(event2013Text));
    }

    private static void testAllTaksLight() {
        String event2014Text = "aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWY=";
        String event2013Text ="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy9jZWUzN2NjMS01Mzk5LTQ3ZWYtOTQxOC0yMWY5YjY0NDRiZmE=";
        System.out.println(new EmsCommunicator().talkShortVersion(event2013Text));
    }

    private static void readOneTalk() {
        String talkEvent="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy9jZWUzN2NjMS01Mzk5LTQ3ZWYtOTQxOC0yMWY5YjY0NDRiZmEvc2Vzc2lvbnMvNzFlOTYzNTQtNmE0OS00ZDEzLThhNjctNjFkN2QzMTk4ODlm";
        System.out.println(new EmsCommunicator().fetchOneTalk(talkEvent));
    }

    private static void publishTalk() {
        String talkEvent="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWYvc2Vzc2lvbnMvOWQzMWVmZGYtN2MzMi00ZDg1LWEyYjUtYjM2YmVlZjMyYzQ0";
        String publishTalk = new EmsCommunicator().publishTalk(talkEvent,"",UserAccessType.FULL);
        System.out.println("result: " + publishTalk);
    }

    private static void readAllEvents() {
        String ev="http://test.java.no/ems-redux/server/events/cee37cc1-5399-47ef-9418-21f9b6444bfa/sessions/35efacf4-e9be-4980-9fb1-5baa83bb050f";
        System.out.println(new EmsCommunicator().allEvents());
    }

}
