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
            String encodedTalkRef;
            try {
                encodedTalkRef = talks.getString(i);
                JSONObject jsonObject = new JSONObject(emsCommunicator.fetchOneTalk(encodedTalkRef));
                System.out.println(jsonObject.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return "{}";
    }
}
