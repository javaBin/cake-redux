package no.javazone.cake.redux;

import java.util.Arrays;
import java.util.List;

public class TestEmsCommunication {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        System.setProperty("cake-redux-config-file",args[0]);
        //updateTagsOnEvent();
        publishTalk();
        //testAllTaksLight();
        long duration = System.currentTimeMillis() -start;
        System.out.println("Took " + duration);
    }

    private static void updateTagsOnEvent() {
        String talkEvent="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWYvc2Vzc2lvbnMvOWQzMWVmZGYtN2MzMi00ZDg1LWEyYjUtYjM2YmVlZjMyYzQ0";
        List<String> tags = Arrays.asList("testTag");
        System.out.println(new EmsCommunicator().updateTags(talkEvent,tags,""));
    }

    private static void testAllTaks() {
        String event2014Text = "aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWY=";
        String event2013Text ="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy9jZWUzN2NjMS01Mzk5LTQ3ZWYtOTQxOC0yMWY5YjY0NDRiZmE=";
        System.out.println(new EmsCommunicator().talksFullVersion(event2013Text));
    }

    private static void testAllTaksLight() {
        String event2014Text = "aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWY=";
        String event2013Text ="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy9jZWUzN2NjMS01Mzk5LTQ3ZWYtOTQxOC0yMWY5YjY0NDRiZmE=";
        System.out.println(new EmsCommunicator().talkShortVersion(event2014Text));
    }

    private static void readOneTalk() {
        String talkEvent="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWYvc2Vzc2lvbnMvOWQzMWVmZGYtN2MzMi00ZDg1LWEyYjUtYjM2YmVlZjMyYzQ0";
        System.out.println(new EmsCommunicator().fetchOneTalk(talkEvent));
    }

    private static void publishTalk() {
        String talkEvent="aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWYvc2Vzc2lvbnMvOWQzMWVmZGYtN2MzMi00ZDg1LWEyYjUtYjM2YmVlZjMyYzQ0";
        String publishTalk = new EmsCommunicator().publishTalk(talkEvent);
        System.out.println("result: " + publishTalk);
    }

    private static void readAllEvents() {
        String ev="http://test.java.no/ems-redux/server/events/cee37cc1-5399-47ef-9418-21f9b6444bfa/sessions/35efacf4-e9be-4980-9fb1-5baa83bb050f";
        System.out.println(new EmsCommunicator().allEvents());
    }

}
