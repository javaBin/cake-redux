package no.javazone.cake.redux;

import no.javazone.cake.redux.mail.MailSenderImplementation;
import no.javazone.cake.redux.mail.MailSenderService;
import no.javazone.cake.redux.mail.MailToSend;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.PojoMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AcceptorSetter {
    //private EmsCommunicator emsCommunicator;
    private SleepingpillCommunicator sleepingpillCommunicator;

    public AcceptorSetter(SleepingpillCommunicator sleepingpillCommunicator) {
        this.sleepingpillCommunicator = sleepingpillCommunicator;
        //this.emsCommunicator = emsCommunicator;
    }

    public String accept(JsonArray talks,UserWithAccess userWithAccess) {
        String template = loadTemplate();
        String tagToAdd = "accepted";
        String tagExistsErrormessage = "Talk is already accepted";
        String subjectTemplate = "JavaZone 2022 #talkType# accepted";

        return doUpdates(talks, template, subjectTemplate, tagToAdd, tagExistsErrormessage,userWithAccess,false);
    }

    public String massUpdate(JsonObject jsonObject,UserWithAccess userWithAccess) {
        JsonArray talks = jsonObject.requiredArray("talks");

        String template = null;
        String subjectTemplate = null;
        if ("true".equals(jsonObject.requiredString("doSendMail"))) {
            template = jsonObject.requiredString("message");
            subjectTemplate = jsonObject.requiredString("subject");
        };

        String tagToAdd = null;
        if ("true".equals(jsonObject.requiredString("doTag"))) {
            tagToAdd = jsonObject.requiredString("newtag");
        }
        String tagExistsErrormessage = "Tag already exsists";

        boolean publishUpdates = "true".equals(jsonObject.stringValue("publishUpdates").orElse("false"));

        return doUpdates(talks, template, subjectTemplate, tagToAdd, tagExistsErrormessage,userWithAccess,publishUpdates);
    }

    private String doUpdates(JsonArray talks, String template, String subjectTemplate, String tagToAdd, String tagExistsErrormessage,UserWithAccess userWithAccess,boolean publishUpdates) {
        JsonArray statusAllTalks = JsonFactory.jsonArray();
        for (int i=0;i<talks.size();i++) {
            JsonObject accept = JsonFactory.jsonObject();
            statusAllTalks.add(accept);
            try {
                String encodedTalkRef = talks.get(i,JsonObject.class).requiredString("ref");
                JsonObject jsonTalk = sleepingpillCommunicator.oneTalkStripped(encodedTalkRef);

                accept.put("title",jsonTalk.requiredString("title"));

                List<TagWithAuthor> origtags = new ArrayList<>(jsonTalk.arrayValue("tagswithauthor").orElse(new JsonArray()).objectStream().map(a -> PojoMapper.map(a, TagWithAuthor.class)).collect(Collectors.toSet()));

                if (tagToAdd != null) {
                    Optional<TagWithAuthor> exisisting = origtags.stream().filter(a -> tagToAdd.equals(a.getTag())).findAny();
                    if (exisisting.isPresent()) {
                        accept.put("status","error");
                        accept.put("message", tagExistsErrormessage);
                        continue;
                    }
                }


                if (template != null) {
                    Optional<String> sendResult = generateAndSendMail(template, subjectTemplate, encodedTalkRef, jsonTalk);
                    if (sendResult.isPresent()) {
                        accept.put("status","error");
                        accept.put("message", sendResult.get());
                        continue;
                    }
                }

                if (tagToAdd != null) {
                    origtags.add(new TagWithAuthor(tagToAdd,userWithAccess.username));
                    String lastModified = jsonTalk.requiredString("lastModified");
                    sleepingpillCommunicator.updateTags(encodedTalkRef, origtags, userWithAccess,lastModified);
                }
                if (publishUpdates) {
                    sleepingpillCommunicator.pubishChanges(encodedTalkRef,userWithAccess.userAccessType);
                }
                accept.put("status","ok");
                accept.put("message","ok");

            } catch (Exception e) {
                    accept.put("status","error");
                    accept.put("message","Error: " + e.getMessage());
            }
        }
        return statusAllTalks.toJson();
    }

    private Optional<String> generateAndSendMail(
            String template,
            String subjectTemplate,
            String encodedTalkRef,
            JsonObject jsonTalk) {
        String talkType = talkTypeText(jsonTalk.requiredString("format"));
        String submitLink = Configuration.submititLocation() + encodedTalkRef;
        String confirmLocation = Configuration.cakeLocation() + "confirm.html?id=" + encodedTalkRef;
        String title = jsonTalk.requiredString("title");

        List<String> sendTo = new ArrayList<>();
        String speakerName = addSpeakers(jsonTalk, sendTo);

        for (String email : sendTo) {
            if (email == null || email.isEmpty()) {
                return Optional.of("Cannot send to empty email");
            }
            int ind = email.indexOf("@");
            if (ind < 1 || ind >= email.length()-1) {
                return Optional.of("Not a valid email: " + email);
            }
        }

        String subject = generateMessage(subjectTemplate, title, talkType, speakerName, submitLink, confirmLocation,jsonTalk, encodedTalkRef);

        String message = generateMessage(template,title, talkType, speakerName, submitLink, confirmLocation,jsonTalk,encodedTalkRef);

        MailToSend mailToSend = new MailToSend(sendTo, subject, message);
        MailSenderService.get().sendMail(MailSenderImplementation.create(mailToSend));
        return Optional.empty();
    }





    private String addSpeakers(JsonObject jsonTalk, List<String> sendTo) {
        JsonArray jsonSpeakers = jsonTalk.requiredArray("speakers");
        StringBuilder speakerName=new StringBuilder();
        for (int j=0;j<jsonSpeakers.size();j++) {
            JsonObject speaker = jsonSpeakers.get(j,JsonObject.class);
            String email=speaker.requiredString("email");
            String name=speaker.requiredString("name");
            if (!speakerName.toString().isEmpty()) {
                speakerName.append(" and ");
            }

            speakerName.append(name);
            sendTo.add(email);
        }
        return speakerName.toString();
    }

    private String replaceAll(String s, String code,String replacement) {
        StringBuilder builder = new StringBuilder(s);
        for (int ind = builder.indexOf(code);ind!=-1;ind = builder.indexOf(code)) {
            builder.replace(ind,ind+code.length(),replacement);
        }
        return builder.toString();
    }

    protected String generateMessage(String template, String title, String talkType, String speakerName, String submitLink, String confirmLocation, JsonObject jsonTalk, String encodedTalkRef) {
        String message = template;
        message = replaceAll(message,"#title#", title);
        message = replaceAll(message,"#speakername#", speakerName);
        message = replaceAll(message,"#talkType#", talkType);
        message = replaceAll(message,"#submititLink#", submitLink);
        message = replaceAll(message,"#confirmLink#", confirmLocation);
        message = replaceAll(message,"#talkid#", encodedTalkRef);


        for (int pos=message.indexOf("#");pos!=-1;pos=message.indexOf("#",pos+1)) {
            if (pos == message.length()-1) {
                break;
            }
            int endpos = message.indexOf("#",pos+1);
            if (endpos == -1) {
                break;
            }
            String key = message.substring(pos+1,endpos);
            Optional<String> stringValue;
            switch (key) {
                case "slot":
                    stringValue = readSlot(jsonTalk);
                    break;
                case "room":
                    stringValue = readRoom(jsonTalk);
                    break;
                default:
                    stringValue = jsonTalk.stringValue(key);
            }
            if (!stringValue.isPresent()) {
                continue;
            }
            String before = message.substring(0,pos);
            String after = (endpos == message.length()-1) ? "" : message.substring(endpos+1);
            message =  before + stringValue.get() + after;
        }
        return message;
    }

    private Optional<String> readRoom(JsonObject jsonTalk) {
        String roomval = jsonTalk.objectValue("room")
                .map(ob -> ob.stringValue("name"))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse("No room allocated");
        return Optional.of(roomval);
    }

    private Optional<String> readSlot(JsonObject jsonTalk) {
        Optional<String> startVal = jsonTalk.objectValue("slot")
                .map(ob -> ob.stringValue("start"))
                .filter(Optional::isPresent)
                .map(Optional::get);
        if (!startVal.isPresent()) {
            return Optional.of("No slot allocated");
        }
        LocalDateTime parse = LocalDateTime.parse(startVal.get());
        String val = parse.format(DateTimeFormatter.ofPattern("MMMM d 'at' HH:mm"));
        return Optional.of(val);
    }

    private String loadTemplate() {
        String template;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("acceptanceTemplate.html")) {
            template = CommunicatorHelper.toString(is);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return template;
    }

    private String talkTypeText(String format) {
        if ("presentation".equals(format)) {
            return "presentation";
        }
        if ("lightning-talk".equals(format)) {
            return "lightning talk";
        }
        if ("workshop".equals(format)) {
            return "workshop";
        }
        return "Unknown";
    }

}
