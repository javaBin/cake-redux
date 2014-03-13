package no.javazone.cake.redux;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class AcceptorSetter {
    private EmsCommunicator emsCommunicator;


    public AcceptorSetter(EmsCommunicator emsCommunicator) {
        this.emsCommunicator = emsCommunicator;
    }

    public String accept(JSONArray talks) {
        for (int i=0;i<talks.length();i++) {
            try {
                String encodedTalkRef = talks.getJSONObject(i).getString("ref");
                JSONObject jsonTalk = new JSONObject(emsCommunicator.fetchOneTalk(encodedTalkRef));
                String title = jsonTalk.getString("title");
                SimpleEmail mail = new SimpleEmail();
                mail.setFrom("program@java.no", "Javazone program commitee");
                mail.addCc("program@java.no");
                mail.setSubject("Talk accepted");
                mail.setHostName(Configuration.smtpServer());
                mail.setSmtpPort(Configuration.smtpPort());

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
                    System.out.println(String.format("Sending mail to %s (%s) acception '%s'", name, email, title));
                }

                String message;

                try (InputStream is = getClass().getClassLoader().getResourceAsStream("acceptanceTemplate.txt")) {
                    String template = EmsCommunicator.toString(is);
                    message = template
                            .replaceAll("#title#",title)
                            .replaceAll("#speakername#",speakerName.toString())
                            ;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                mail.setMsg(message);
                mail.send();

            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (EmailException e) {
                throw new RuntimeException(e);
            }
        }
        return "{}";
    }

}
