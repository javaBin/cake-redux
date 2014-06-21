package no.javazone.cake.redux;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AcceptorSetter {
    private EmsCommunicator emsCommunicator;


    public AcceptorSetter(EmsCommunicator emsCommunicator) {
        this.emsCommunicator = emsCommunicator;
    }

    public String accept(JSONArray talks) {
        String template = loadTemplate();
        String tagToAdd = "accepted";
        String tagExistsErrormessage = "Talk is already accepted";
        String subjectTemplate = "Javazone 2014 #talkType# accepted";

        return doUpdates(talks, template, subjectTemplate, tagToAdd, tagExistsErrormessage);
    }

    public String massUpdate(JSONObject jsonObject) throws JSONException {
        JSONArray talks = jsonObject.getJSONArray("talks");

        String template = null;
        String subjectTemplate = null;
        if ("true".equals(jsonObject.getString("doSendMail"))) {
            template = jsonObject.getString("message");
            subjectTemplate = jsonObject.getString("subject");
        };

        String tagToAdd = null;
        if ("true".equals(jsonObject.getString("doTag"))) {
            tagToAdd = jsonObject.getString("newtag");
        }
        String tagExistsErrormessage = "Tag already exsists";

        return doUpdates(talks, template, subjectTemplate, tagToAdd, tagExistsErrormessage);
    }

    private String doUpdates(JSONArray talks, String template, String subjectTemplate, String tagToAdd, String tagExistsErrormessage) {
        List<JSONObject> statusAllTalks = new ArrayList<>();
        for (int i=0;i<talks.length();i++) {
            JSONObject accept = new JSONObject();
            statusAllTalks.add(accept);
            try {
                String encodedTalkRef = talks.getJSONObject(i).getString("ref");
                JSONObject jsonTalk = new JSONObject(emsCommunicator.fetchOneTalk(encodedTalkRef));
                accept.put("title",jsonTalk.getString("title"));

                List<String> tags = toCollection(jsonTalk.getJSONArray("tags"));

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
                    String lastModified = jsonTalk.getString("lastModified");
                    emsCommunicator.updateTags(encodedTalkRef, tags, lastModified);
                }
                accept.put("status","ok");
                accept.put("message","ok");

            } catch (JSONException | EmailException e) {
                try {
                    accept.put("status","error");
                    accept.put("message","Error: " + e.getMessage());
                } catch (JSONException je) {
                    throw new RuntimeException(je);
                }
            }
        }
        return new JSONArray(statusAllTalks).toString();
    }

    private void generateAndSendMail(
            String template,
            String subjectTemplate,
            String encodedTalkRef,
            JSONObject jsonTalk) throws JSONException, EmailException {
        String talkType = talkTypeText(jsonTalk.getString("format"));
        String submitLink = Configuration.submititLocation() + encodedTalkRef;
        String confirmLocation = Configuration.cakeLocation() + "confirm.html?id=" + encodedTalkRef;
        String title = jsonTalk.getString("title");

        SimpleEmail mail = new SimpleEmail();
        String speakerName = addSpeakers(jsonTalk, mail);

        String subject = generateMessage(subjectTemplate, title, talkType, speakerName, submitLink, confirmLocation);
        setupMailHeader(mail,subject);

        String message = generateMessage(template,title, talkType, speakerName, submitLink, confirmLocation);
        mail.setMsg(message);
        mail.send();
    }

    private List<String> toCollection(JSONArray tags) throws JSONException {
        ArrayList<String> result = new ArrayList<>();
        if (tags == null) {
            return result;
        }
        for (int i=0;i<tags.length();i++) {
            result.add(tags.getString(i));
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

    private String addSpeakers(JSONObject jsonTalk, SimpleEmail mail) throws JSONException, EmailException {
        JSONArray jsonSpeakers = jsonTalk.getJSONArray("speakers");
        StringBuilder speakerName=new StringBuilder();
        for (int j=0;j<jsonSpeakers.length();j++) {
            JSONObject speaker = jsonSpeakers.getJSONObject(j);
            String email=speaker.getString("email");
            String name=speaker.getString("name");
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
