package no.javazone.cake.redux.util;

import no.javazone.cake.redux.mail.EmailManualSender;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FindDrafts {
    private SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Use arg");
            return;
        }
        System.setProperty("cake-redux-config-file",args[0]);
        FindDrafts findDrafts = new FindDrafts();
        Set<String> analyze = findDrafts.analyze();
        findDrafts.sendMails(analyze);
    }

    private void sendMails(Set<String> analyze) throws Exception {
        String subject = "Have you forgotten to submit your JavaZone proposal?";
        String content="<html><body><p>Hello,</p>" +
                "<p>We have started evaluating all submissions. On this occasion, we have noticed that you have a submission that is still registered as a draft. If you did not intend to submit this proposal, please ignore this message. Otherwise, please log into <a href=\"submit.javazone.no\">submit.javazone.no</a> and change your submission status as soon as possible such that we can consider your talk.</p>" +
                "<p>If you have any questions do not hesitate to contact us at program@java.no</p>" +
                "<p>Thank you, the JavaZone program committee</p>";
        for (String email : analyze) {
            EmailManualSender.send(email,content,subject);
        }
    }

    public static boolean isValidEmail(String email) {
        if (Optional.ofNullable(email).orElse("").trim().isEmpty()) {
            return false;
        }
        if (email.equals("zane.moreign@hccug.org")) {
            return false;
        }
        if (!email.contains("@")) {
            return false;
        }
        return email.chars().noneMatch(Character::isWhitespace);
    }

    public Set<String> analyze() {
        String confid2018 = "346cb6bd41ea4812971927ffa33e0333";
        JsonArray talks = sleepingpillCommunicator.allTalkFromConferenceSleepingPillFormat(confid2018);
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
            if (!isValidEmail(email)) {
                continue;
            }
            unsubmitted.add(email);
        }

        System.out.println("email " + unsubmitted.size());

        unsubmitted.forEach(System.out::println);

        return unsubmitted;

    }
}
