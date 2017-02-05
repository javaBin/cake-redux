package no.javazone.cake.redux.sleepingpill;

import no.javazone.cake.redux.Base64Util;
import no.javazone.cake.redux.Configuration;
import org.jsonbuddy.*;
import org.jsonbuddy.parse.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

public class SleepingpillCommunicator {

    public String allEvents() {
        JsonArray allCpnf = JsonArray.fromNodeStream(allConferences().objectStream()
                .map(obj -> JsonFactory.jsonObject()
                        .put("name", obj.requiredString("name"))
                        .put("ref", obj.requiredString("id"))
                        .put("slug", obj.requiredString("slug"))));
        return allCpnf.toJson();
    }

    private JsonArray allConferences()  {
        String conferenceUrls = Configuration.sleepingPillBaseLocation() + "/data/conference";
        URLConnection urlConnection = openConnection(conferenceUrls);
        try (InputStream inputStream = urlConnection.getInputStream()) {
            JsonObject jsonObject = JsonParser.parseToObject(inputStream);
            JsonArray conferences = jsonObject.requiredArray("conferences");
            return conferences;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String talkShortVersion(String conferenceid) {
        JsonArray result = JsonArray.fromNodeStream(allTalksFromConference(conferenceid)
                .objectStream()
                .map(SleepingpillCommunicator::talkObj));
        return result.toJson();
    }

    public JsonObject oneTalkAsJson(String talkid) {
        String talkurl = Configuration.sleepingPillBaseLocation() + "/data/session/" + talkid;
        URLConnection urlConnection = openConnection(talkurl);
        try (InputStream inputStream = urlConnection.getInputStream()) {
            JsonObject jsonObject = JsonParser.parseToObject(inputStream);
            return talkObj(jsonObject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String fetchOneTalk(String talkid) {
        return oneTalkAsJson(talkid).toJson();
    }

    private static JsonObject talkObj(JsonObject jsonObject) {
        JsonObject talkob = JsonFactory.jsonObject();
        talkob.put("title",readValueFromProp(jsonObject,"title"));
        talkob.put("format",readValueFromProp(jsonObject,"format"));
        talkob.put("lang",readValueFromProp(jsonObject,"language"));
        talkob.put("keywords",readValueFromProp(jsonObject,"keywords",JsonFactory.jsonArray()));
        talkob.put("audience",readValueFromProp(jsonObject,"intendedAudience"));
        talkob.put("equipment",readValueFromProp(jsonObject,"equipment"));
        talkob.put("outline",readValueFromProp(jsonObject,"outline"));
        talkob.put("summary","");
        talkob.put("level","beginner");
        talkob.put("tags",readValueFromProp(jsonObject,"tags",JsonFactory.jsonArray()));
        talkob.put("published",new Boolean(Arrays.asList("APPROVED","HISTORIC").contains(jsonObject.requiredString("status"))).toString());
        talkob.put("body",readValueFromProp(jsonObject,"abstract"));
        talkob.put("ref",jsonObject.requiredString("id"));
        talkob.put("state",jsonObject.requiredString("status"));

        talkob.put("speakers",JsonArray.fromNodeStream(
            jsonObject.requiredArray("speakers").objectStream()
                .map(ob -> JsonFactory.jsonObject()
                                .put("name",ob.stringValue("name").orElse(""))
                                .put("email",ob.stringValue("email").orElse(""))
                                .put("bio",readValueFromProp(ob,"bio"))
                                .put("zip-code",readValueFromProp(ob,"zip-code"))
                        )));

        return talkob;


    }

    private static JsonNode readValueFromProp(JsonObject talkObj,String key) {
        return readValueFromProp(talkObj,key,new JsonString(""));
    }

    private static JsonNode readValueFromProp(JsonObject talkObj,String key,JsonNode defaultValue) {
        return talkObj.requiredObject("data")
                .objectValue(key).orElse(JsonFactory.jsonObject().put("value",defaultValue))
                .value("value").orElseThrow(() -> new RuntimeException("Ubknown property " + key));

    }


    private JsonArray allTalksFromConference(String conferenceid) {
        String url = Configuration.sleepingPillBaseLocation() + "/data/conference/" +conferenceid + "/session";
        URLConnection urlConnection = openConnection(url);
        try (InputStream inputStream = urlConnection.getInputStream()) {
            JsonObject jsonObject = JsonParser.parseToObject(inputStream);
            JsonArray conferences = jsonObject.requiredArray("sessions");
            return conferences;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private URLConnection openConnection(String urlpath) {
        try {
            URL url = new URL(urlpath);
            URLConnection urlConnection = url.openConnection();
            String sleepingpillUser = Configuration.sleepingpillUser();
            if (sleepingpillUser != null) {
                String authString = sleepingpillUser + ":" + Configuration.sleepingpillPassword();
                String authStringEnc = Base64Util.encode(authString);
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            }
            return urlConnection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
