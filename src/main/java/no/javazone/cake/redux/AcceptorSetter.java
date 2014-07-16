package no.javazone.cake.redux;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AcceptorSetter {
    private EmsCommunicator emsCommunicator;


    public AcceptorSetter(EmsCommunicator emsCommunicator) {
        this.emsCommunicator = emsCommunicator;
    }

    public String accept(ArrayNode talks) {
        String template = loadTemplate();
        String tagToAdd = "accepted";
        String tagExistsErrormessage = "Talk is already accepted";
        String subjectTemplate = "Javazone 2014 #talkType# accepted";

        return doUpdates(talks, template, subjectTemplate, tagToAdd, tagExistsErrormessage);
    }

    public String massUpdate(ObjectNode jsonObject) {
        JsonNode talks = jsonObject.get("talks");

        String template = null;
        String subjectTemplate = null;
        if (jsonObject.get("doSendMail").asBoolean(false)) {
            template = jsonObject.get("message").asText();
            subjectTemplate = jsonObject.get("subject").asText();
        }

        String tagToAdd = null;
        if (jsonObject.get("doTag").asBoolean(false)) {
            tagToAdd = jsonObject.get("newtag").asText();
        }
        String tagExistsErrormessage = "Tag already exists";

        return doUpdates(talks, template, subjectTemplate, tagToAdd, tagExistsErrormessage);
    }

    private String doUpdates(JsonNode talks, String template, String subjectTemplate, String tagToAdd, String tagExistsErrormessage) {
        ArrayNode statusAllTalks = JsonNodeFactory.instance.arrayNode();
        for (JsonNode talk : talks) {
            ObjectNode accept = JsonNodeFactory.instance.objectNode();
            statusAllTalks.add(accept);
            try {
                String encodedTalkRef = talk.get("ref").asText();
                ObjectNode jsonTalk = emsCommunicator.fetchOneTalkAsObjectNode(encodedTalkRef);
                accept.put("title", jsonTalk.get("title").asText());

                List<String> tags = toCollection(jsonTalk.get("tags"));

                if (tagToAdd != null && tags.contains(tagToAdd)) {
                    accept.put("status","error");
                    accept.put("message", tagExistsErrormessage);
                    continue;
                }

                if (template != null) {
                    generateAndSendMail(template, subjectTemplate, encodedTalkRef, jsonTalk);
                }

                if (tagToAdd != null) {
                    tags.add(tagToAdd);
                    String lastModified = jsonTalk.get("lastModified").asText();
                    emsCommunicator.updateTags(encodedTalkRef, tags, lastModified);
                }
                accept.put("status","ok");
                accept.put("message","ok");

            } catch (EmailException e) {
                accept.put("status","error");
                accept.put("message","Error: " + e.getMessage());
            }
        }
        return statusAllTalks.toString();
    }

    private void generateAndSendMail(
            String template,
            String subjectTemplate,
            String encodedTalkRef,
            ObjectNode jsonTalk) throws EmailException {
        String talkType = talkTypeText(jsonTalk.get("format").asText());
        String submitLink = Configuration.submititLocation() + encodedTalkRef;
        String confirmLocation = Configuration.cakeLocation() + "confirm.html?id=" + encodedTalkRef;
        String title = jsonTalk.get("title").asText();

        SimpleEmail mail = new SimpleEmail();
        String speakerName = addSpeakers(jsonTalk, mail);

        String subject = generateMessage(subjectTemplate, title, talkType, speakerName, submitLink, confirmLocation);
        setupMailHeader(mail,subject);

        String message = generateMessage(template,title, talkType, speakerName, submitLink, confirmLocation);
        mail.setMsg(message);
        mail.send();
    }

    private List<String> toCollection(JsonNode tags) {
        ArrayList<String> result = new ArrayList<>();
        if (tags == null) {
            return result;
        }
        for (JsonNode tag : tags) {
            result.add(tag.asText());
        }
        return result;
    }



    private SimpleEmail setupMailHeader(SimpleEmail mail,String subject) throws EmailException {
        mail.setHostName(Configuration.smtpServer());
        mail.setSmtpPort(Configuration.smtpPort());
        mail.setFrom("program@java.no", "Javazone program commitee");
        mail.addCc("program@java.no");
        mail.setSubject(subject);
        return mail;
    }

    private String addSpeakers(ObjectNode jsonTalk, SimpleEmail mail) throws EmailException {
        JsonNode jsonSpeakers = jsonTalk.get("speakers");
        StringBuilder speakerName=new StringBuilder();
        for (JsonNode speaker : jsonSpeakers) {
            String email=speaker.get("email").asText();
            String name=speaker.get("name").asText();
            if (!speakerName.toString().isEmpty()) {
                speakerName.append(" and ");
            }

            speakerName.append(name);
            mail.addTo(email);
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

    protected String generateMessage(String template, String title, String talkType, String speakerName, String submitLink, String confirmLocation) {
        String message = template;
        message = replaceAll(message,"#title#", title);
        message = replaceAll(message,"#speakername#", speakerName);
        message = replaceAll(message,"#talkType#", talkType);
        message = replaceAll(message,"#submititLink#", submitLink);
        message = replaceAll(message,"#confirmLink#", confirmLocation);
        return message;
    }

    private String loadTemplate() {
        String template;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("acceptanceTemplate.txt")) {
            template = EmsCommunicator.toString(is);

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
