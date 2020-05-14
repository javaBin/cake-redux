package no.javazone.cake.redux.util;

import no.javazone.cake.redux.CommunicatorHelper;
import no.javazone.cake.redux.Configuration;
import no.javazone.cake.redux.mail.EmailManualSender;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MailToAllSpeakers {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage <configfile> <mailfile> <subject>");
            return;
        }
        String mailfile = args[1];
        String subject = args[2];
        Configuration.setConfigFile(args);
        Set<SpeakerAddr.SpeakerObj> allSpeakers = new SpeakerAddr().allSpeakersGivenConferences(new HashSet<>(Arrays.asList("javazone_2018","javazone_2019")));

        String content = CommunicatorHelper.toString(new FileInputStream(mailfile));
        System.out.println("Sending emails to " + allSpeakers.size());
        for (SpeakerAddr.SpeakerObj allSpeaker : allSpeakers) {
            try {
                EmailManualSender.send(allSpeaker.email, content, subject);
            } catch (Exception e) {
                System.out.println("Could not send to '" + allSpeaker.email + "' " + e.getMessage() + "["  + e.getClass() + "]");
            }
        }

    }
}
