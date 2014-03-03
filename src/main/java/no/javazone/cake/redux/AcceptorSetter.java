package no.javazone.cake.redux;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
                JSONArray jsonSpeakers = jsonTalk.getJSONArray("speakers");
                for (int j=0;j<jsonSpeakers.length();j++) {
                    JSONObject speaker = jsonSpeakers.getJSONObject(j);
                    String email=speaker.getString("email");
                    String name=speaker.getString("name");
                    System.out.println(String.format("Sending mail to %s (%s) acception '%s'",name,email,title));
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return "{}";
    }
}
