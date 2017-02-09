package no.javazone.cake.redux.sleepingpill;

import no.javazone.cake.redux.Base64Util;
import no.javazone.cake.redux.Configuration;
import no.javazone.cake.redux.NoUserAceessException;
import no.javazone.cake.redux.UserAccessType;
import org.jsonbuddy.*;
import org.jsonbuddy.parse.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import static org.jsonbuddy.JsonFactory.jsonObject;

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
        JsonArray result = JsonArray.fromNodeStream(allSubmittedTalksFromConference(conferenceid)
                .objectStream()
                .map(SleepingpillCommunicator::talkObj));
        return result.toJson();
    }

    public JsonObject oneTalkAsJson(String talkid) {
        JsonObject talk = oneTalkStripped(talkid);
        JsonArray allConferences = parseJsonFromConnection(openConnection(Configuration.sleepingPillBaseLocation() + "/data/conference")).requiredArray("conferences");
        talk.requiredArray("speakers").objectStream().forEach(speaker -> {
            String url = Configuration.sleepingPillBaseLocation() + "/data/submitter/" + speaker.requiredString("email") + "/session";
            JsonObject speakerTalks = parseJsonFromConnection(openConnection(url));
            JsonArray otherTalks = JsonArray.fromNodeStream(
                    speakerTalks.requiredArray("sessions").objectStream().map(obj -> buildSimularTalk(obj,allConferences))
            );
            speaker.put("spOtherTalks",otherTalks);
        });

        return talk;
    }

    private static JsonObject buildSimularTalk(JsonObject obj,JsonArray allConferences) {
        String href = Configuration.cakeLocation() + "secured/#/showTalk/" + obj.requiredString("sessionId");
        String confid = obj.requiredString("conferenceId");
        String confname = allConferences.objectStream()
                .filter(ob -> ob.requiredString("id").equals(confid))
                .findAny()
                .map(ob -> ob.requiredString("name"))
                .orElse("UNKNOWN");
        String title = obj.requiredObject("data").objectValue("title").orElse(JsonFactory.jsonObject().put("value", "xxx")).requiredString("value");
        JsonArray tags = obj.requiredObject("data").objectValue("tags").orElse(JsonFactory.jsonObject().put("value", JsonFactory.jsonArray())).requiredArray("value");
        String status = obj.requiredString("status");

        return JsonFactory.jsonObject()
                .put("sessionId", obj.requiredString("sessionId"))
                .put("href", href)
                .put("title", title)
                .put("conference",confname)
                .put("tags",tags)
                .put("status",status);

    }

    public JsonObject oneTalkStripped(String talkid) {
        String talkurl = Configuration.sleepingPillBaseLocation() + "/data/session/" + talkid;
        URLConnection urlConnection = openConnection(talkurl);
        return talkObj(parseJsonFromConnection(urlConnection));
    }

    private JsonObject parseJsonFromConnection(URLConnection urlConnection) {
        JsonObject jsonObject;
        try (InputStream inputStream = urlConnection.getInputStream()) {
            jsonObject = JsonParser.parseToObject(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonObject;
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
        talkob.put("suggestedKeywords",readValueFromProp(jsonObject,"suggestedKeywords"));
        talkob.put("audience",readValueFromProp(jsonObject,"intendedAudience"));
        talkob.put("equipment",readValueFromProp(jsonObject,"equipment"));
        talkob.put("outline",readValueFromProp(jsonObject,"outline"));
        talkob.put("infoToProgramCommittee",readValueFromProp(jsonObject,"infoToProgramCommittee"));
        talkob.put("length",readValueFromProp(jsonObject,"length"));
        talkob.put("summary","");
        talkob.put("level",readValueFromProp(jsonObject,"level"));
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
                                .put("twitter",readValueFromProp(ob,"twitter"))
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


    private JsonArray allSubmittedTalksFromConference(String conferenceid) {
        String url = Configuration.sleepingPillBaseLocation() + "/data/conference/" +conferenceid + "/session";
        URLConnection urlConnection = openConnection(url);
        JsonObject jsonObject;
        try (InputStream inputStream = urlConnection.getInputStream()) {
            jsonObject = JsonParser.parseToObject(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JsonArray conferences = jsonObject.requiredArray("sessions");
        JsonArray withoutDraft = JsonArray.fromNodeStream(conferences.objectStream().filter(ob -> !"DRAFT".equals(ob.requiredString("status"))));

        return withoutDraft;
    }

    private HttpURLConnection openConnection(String urlpath) {
        try {
            URL url = new URL(urlpath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
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

    private void checkWriteAccess(UserAccessType userAccessType) {
        if (Configuration.noAuthMode()) {
            return;
        }
        switch (userAccessType) {
            case FULL:
            case OPENSERVLET:
                return;
            default:
                throw new NoUserAceessException();
        }
    }

    public void updateTags(String ref,List<String> tags,UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        JsonObject input = JsonFactory.jsonObject()
                .put("tags", jsonObject().put("value", JsonArray.fromStringList(tags)).put("privateData", true));
        sendTalkUpdate(ref,input);
    }

    public String update(String ref, List<String> taglist, String state, String lastModified, UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        JsonObject jsonObject = oneTalkStripped(ref);
        JsonArray currenttags = jsonObject.requiredArray("tags");
        String currentstate = jsonObject.requiredString("state");

        JsonObject input = JsonFactory.jsonObject();
        JsonArray newtags = JsonArray.fromStringList(taglist);

        if (!newtags.equals(currenttags)) {
            input.put("tags", jsonObject().put("value", newtags).put("privateData", true));
        }
        if (!currentstate.equals(state)) {
            input.put("status", jsonObject().put("value", state).put("privateData", false));
        }

        if (input.isEmpty()) {
            return fetchOneTalk(ref);

        }

        sendTalkUpdate(ref, input);
        return fetchOneTalk(ref);
    }

    private void sendTalkUpdate(String ref, JsonObject input) {
        String url = Configuration.sleepingPillBaseLocation() + "/data/session/" + ref;
        JsonObject payload = jsonObject().put("data", input);

        HttpURLConnection conn = openConnection(url);

        try {
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"))) {
                payload.toJson(printWriter);
            }
            try (InputStream is = conn.getInputStream()) {
                JsonParser.parseToObject(is);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
