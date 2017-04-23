package no.javazone.cake.redux.util;

import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class FindDrafts {
    private SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Use arg");
            return;
        }
        System.setProperty("cake-redux-config-file",args[0]);
        new FindDrafts().analyze();
    }

    private void analyze() {
        String confid2017 = "30d5c2f1cb214fc8b0649a44fdf3b4bf";
        JsonArray talks = sleepingpillCommunicator.allTalkFromConferenceSleepingPillFormat(confid2017);
        System.out.println(talks.size());
        Set<String> drafts = new HashSet<>();
        Set<String> others = new HashSet<>();

        talks.objectStream().forEach(tobj -> {
            Set<String> curr = "DRAFT".equals(tobj.requiredString("status")) ? drafts : others;
            tobj.requiredArray("speakers").objectStream()
                    .map(ob -> ob.requiredString("email"))
                    .forEach(curr::add);

        });

        System.out.println("Dr " + drafts.size());
        System.out.println("Ot " + others.size());

        Set<String> unsubmitted = new HashSet<>();

        for (String email : drafts) {
            if (others.contains(email)) {
                continue;
            }
            unsubmitted.add(email);
        }

        System.out.println("email " + unsubmitted.size());

        unsubmitted.forEach(System.out::println);

    }
}
