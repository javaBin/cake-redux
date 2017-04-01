package no.javazone.cake.redux;

import no.javazone.cake.redux.mail.MailSenderImplementation;
import no.javazone.cake.redux.mail.MailSenderService;
import no.javazone.cake.redux.mail.SmtpMailSender;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SpeakerMailSender {
    private final String msg;
    private SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    public SpeakerMailSender() throws IOException {
        msg = CommunicatorHelper.toString(getClass().getClassLoader().getResourceAsStream("speakerInvMail.txt"));
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usege SpeakerMailSender <configfile>");
            return;
        }
        System.setProperty("cake-redux-config-file",args[0]);
        SpeakerMailSender speakerMailSender = new SpeakerMailSender();

        SortedSet<String> allAddresses = readFromFile(args[1]);
        //Set<String> allAddresses = speakerMailSender.readSpeakerList();
        System.out.println(allAddresses);
        System.out.println("****");
        speakerMailSender.sendMailToAll(allAddresses);
    }

    private static SortedSet<String> readFromFile(String filename) throws Exception {
        String all = CommunicatorHelper.toString(new FileInputStream(filename));

        SortedSet<String> result = new TreeSet<>();
        for (String line : all.split("\n")) {
            result.add(line);
        }
        return result;
    }

    private void sendMailToAll(SortedSet<String> allAddresses) throws Exception {
        System.out.println("Sending...");
        for (String address : allAddresses) {
            System.out.println(address);
            sendMail(address);
            Thread.sleep(10000);
        }
    }

    public Set<String> allMailsFrom(String event) {
        String allTalksStr = sleepingpillCommunicator.talkShortVersion(event);
        JsonArray allTalks = JsonParser.parseToArray(allTalksStr);
        Set<String> res = allTalks.nodeStream().flatMap(jn -> {
            JsonObject obj = (JsonObject) jn;
            return obj.arrayValue("speakers").orElse(JsonFactory.jsonArray()).nodeStream()
                    .map(speakerNode -> {
                        JsonObject speakerObject = (JsonObject) speakerNode;
                        return speakerObject.requiredString("email").toLowerCase();
                    });
        }).collect(Collectors.toSet());
        return res;
    }

    private SimpleEmail setupMailHeader(SimpleEmail mail, String subject) throws EmailException {
        mail.setHostName(Configuration.smtpServer());
        mail.setFrom("program@java.no", "Javazone program commitee");
        mail.addBcc("program-auto@java.no");
        mail.setSubject(subject);


        if (Configuration.useMailSSL()) {
            mail.setSSLOnConnect(true);
            mail.setSslSmtpPort("" + Configuration.smtpPort());
        } else {
            mail.setSmtpPort(Configuration.smtpPort());

        }
        String mailUser = Configuration.mailUser();
        if (mailUser != null) {
            mail.setAuthentication(mailUser, Configuration.mailPassword());
        }

        return mail;
    }

    private void sendMail(String email) throws EmailException {
        SimpleEmail mail = new SimpleEmail();
        mail = setupMailHeader(mail,"JavaZone 2016 Call for Speaker");
        mail.addTo(email);
        mail.setMsg(msg);
        MailSenderService.get().sendMail(MailSenderImplementation.create(mail));
    }

    private Set<String> readSpeakerList() {
        JsonArray jsonArray = JsonParser.parseToArray(sleepingpillCommunicator.allEvents());
        Map<String, String> eventmap = jsonArray.nodeStream()
                .collect(Collectors.toMap(jn -> ((JsonObject) jn).requiredString("slug"), jn -> ((JsonObject) jn).requiredString("ref")));
        System.out.println(eventmap.keySet());
        Set<String> speakers2016 = allMailsFrom(eventmap.get("javazone_2016"));
        Set<String> speakers2015 = allMailsFrom(eventmap.get("javazone_2015"));
        Set<String> speakers2014 = allMailsFrom(eventmap.get("javazone_2014"));
        Set<String> speakers2013 = allMailsFrom(eventmap.get("javazone_2013"));
        System.out.println("2016: " + speakers2016.size());
        System.out.println("2015: " + speakers2015.size());
        System.out.println("2014: " + speakers2014.size());
        System.out.println("2013: " + speakers2013.size());

        SortedSet all = new TreeSet<>();
        all.addAll(speakers2013);
        all.addAll(speakers2014);
        all.addAll(speakers2015);
        all.removeAll(speakers2016);
        System.out.println("All:" + all.size());

        all.stream().forEach(System.out::println);

        return all;
    }
}
