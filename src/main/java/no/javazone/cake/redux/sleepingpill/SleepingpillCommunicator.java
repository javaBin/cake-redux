package no.javazone.cake.redux.sleepingpill;

import no.javazone.cake.redux.*;
import org.jsonbuddy.*;
import org.jsonbuddy.parse.JsonParser;
import org.jsonbuddy.pojo.JsonGenerator;
import org.jsonbuddy.pojo.PojoMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .map(SleepingpillCommunicator::talkObj)
                .sorted(Comparator.comparing(o -> o.requiredString("submittedTime")))
        );


        return jsonHackFix(result.toJson());
    }

    public static String jsonHackFix(String jsonstr) {
        StringBuilder fixed = new StringBuilder(jsonstr);
        for (int i=0;i<fixed.length();i++) {
            if (fixed.charAt(i) == '\u000B') {
                fixed.replace(i,i+1," ");
            }
        }
        return fixed.toString();
    }

    public JsonObject oneTalkAsJson(String talkid) {
        JsonObject talk = oneTalkStripped(talkid);
        JsonArray allConferences = parseJsonFromConnection(openConnection(Configuration.sleepingPillBaseLocation() + "/data/conference")).requiredArray("conferences");

        JsonArray speakersArr = talk.requiredArray("speakers");
        for (JsonNode part : speakersArr) {
            if (!(part instanceof JsonObject)) {
                continue;
            }
            JsonObject speaker = (JsonObject) part;
            String speakerEmail = speaker.requiredString("email");
            try {
                speakerEmail = URLEncoder.encode(speakerEmail,"UTF-8");
            } catch (UnsupportedEncodingException ignored) {}
            String url = Configuration.sleepingPillBaseLocation() + "/data/submitter/" + speakerEmail + "/session";
            JsonObject speakerTalks = parseJsonFromConnection(openConnection(url));
            JsonArray otherTalks = JsonArray.fromNodeStream(
                    speakerTalks.requiredArray("sessions").objectStream()
                            .filter(obj -> !(obj.stringValue("id").equals(Optional.of(talkid)) || obj.stringValue("status").equals(Optional.of("DRAFT"))))
                            .map(obj -> buildSimularTalk(obj,allConferences))
                            .sorted((a,b) -> {
                                int conf = a.stringValue("conference").orElse("").compareTo(b.stringValue("conference").orElse(""));
                                if (conf != 0) {
                                    return conf;
                                }
                                return a.stringValue("title").orElse("").compareTo(b.stringValue("title").orElse(""));
                            })
            );
            speaker.put("spOtherTalks",otherTalks);
        }

        JsonArray uniqueTags = JsonArray.fromStringStream(talk.arrayValue("tags").orElse(JsonFactory.jsonArray())
                .stringStream().collect(Collectors.toSet())
                .stream());
        talk.put("tags",uniqueTags);

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
        String format = obj.requiredObject("data").objectValue("format").orElse(JsonFactory.jsonObject()).stringValue("value").orElse("Unknown");
        JsonArray tags = obj.requiredObject("data").objectValue("tags").orElse(JsonFactory.jsonObject().put("value", JsonFactory.jsonArray())).requiredArray("value");
        String status = obj.requiredString("status");

        return JsonFactory.jsonObject()
                .put("sessionId", obj.requiredString("sessionId"))
                .put("href", href)
                .put("title", title)
                .put("conference",confname)
                .put("tags",tags)
                .put("format",format)
                .put("status",status);

    }

    public JsonObject oneTalkStripped(String talkid) {
        JsonObject jsonObject = oneTalkSleepingPillFormat(talkid);
        return talkObj(jsonObject);
    }

    public JsonObject oneTalkSleepingPillFormat(String talkid) {
        String talkurl = Configuration.sleepingPillBaseLocation() + "/data/session/" + talkid;
        URLConnection urlConnection = openConnection(talkurl);
        JsonObject returnValue = parseJsonFromConnection(urlConnection);
        /*
        List<JsonObject> testtaglist = new ArrayList<>();
        testtaglist.add(new JsonObject().put("tag","dummytag").put("author","Per"));
        testtaglist.add(new JsonObject().put("tag","dummytag").put("author","Ole"));
        returnValue.requiredObject("data").put("tagswithauthor",new JsonObject().put("privateDate",true).put("value",JsonArray.fromNodeList(testtaglist)));*/
        return returnValue;
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
        talkob.put("participation",readValueFromProp(jsonObject,"participation"));
        talkob.put("registerLoc",readValueFromProp(jsonObject,"registerLoc"));


        final JsonArray talkUpdatesArray = jsonObject.requiredArray("talkUpdates");
        final List<JsonObject> talkUpdates = talkUpdatesArray
                .objectStream().collect(Collectors.toList());
        String lastUpdateBy = "";
        if (!talkUpdates.isEmpty()) {
            lastUpdateBy = talkUpdates.get(talkUpdates.size()-1).requiredString("updatedBy");
        }
        talkob.put("lastChangedBy",lastUpdateBy);
        talkob.put("talkUpdateHistory",talkUpdatesArray);

        TagsToDisplay tagsToDisplay = TagsHandler.INSTANCE.readTagsFromDataObject(jsonObject.requiredObject("data"));

        talkob.put("tags",tagsToDisplay.getSummary());
        talkob.put("tagswithauthor",tagsToDisplay.getFull());
        talkob.put("published", Boolean.toString(Arrays.asList("APPROVED", "HISTORIC").contains(jsonObject.requiredString("status"))));
        talkob.put("body",readValueFromProp(jsonObject,"abstract"));
        talkob.put("ref",jsonObject.requiredString("id"));
        talkob.put("ratingsummary",PkomFeedbackCompute.INSTANCE.compute(jsonObject));
        talkob.put("hasUnpublishedValues",jsonObject.requiredObject("sessionUpdates")
                .requiredBoolean("hasUnpublishedChanges")
                ? "Yes" : "No");

        jsonObject.requiredObject("data")
                .objectValue("room")
                .map(ob -> ob.requiredString("value"))
                .ifPresent(roomname -> talkob.put("room",JsonFactory.jsonObject().put("name",roomname)));
        readSlotFromSleepingPill(jsonObject).ifPresent(slotobj -> talkob.put("slot",slotobj));


        talkob.put("sessionUpdates",jsonObject.requiredObject("sessionUpdates"));
        jsonObject.stringValue("lastUpdated").ifPresent(lu -> talkob.put("lastModified",lu));
        Optional.of(readValueFromProp(jsonObject,"emslocation"))
                .filter(s -> !s.stringValue().isEmpty())
                .ifPresent(v -> talkob.put("emslocation",v));

        talkob.put("state",jsonObject.requiredString("status"));
        talkob.put("submittedTime",jsonObject.stringValue("submittedTime").orElse("2017"));

        talkob.put("speakers",JsonArray.fromNodeStream(
            jsonObject.requiredArray("speakers").objectStream()
                .map(ob -> JsonFactory.jsonObject()
                                .put("name",ob.stringValue("name").orElse(""))
                                .put("email",ob.stringValue("email").orElse(""))
                                .put("bio",readValueFromProp(ob,"bio"))
                                .put("zip-code",readValueFromProp(ob,"zip-code"))
                                .put("twitter",readValueFromProp(ob,"twitter"))
                                .put("residence",readValueFromProp(ob,"residence"))
                        )));
        talkob.put("pubcomments",jsonObject.arrayValue("comments").orElse(JsonFactory.jsonArray()));

        talkob.put("postcode",talkob.requiredArray("speakers").objectStream().map(ob -> ob.stringValue("zip-code").orElse("")).filter(pc -> !pc.trim().isEmpty())
                .reduce((a,b) -> a+","+b).orElse(""));

        return talkob;


    }

    private static Optional<JsonObject> readSlotFromSleepingPill(JsonObject talkObj) {
        Optional<String> start = talkObj.requiredObject("data").objectValue("startTime").map(jo -> jo.requiredString("value"));
        Optional<String> end = talkObj.requiredObject("data").objectValue("endTime").map(jo -> jo.requiredString("value"));
        if (!(start.isPresent() && end.isPresent())) {
            return Optional.empty();
        }
        return Optional.of(JsonFactory.jsonObject().put("start",start.get()).put("end",end.get()));
    }

    private static JsonNode readValueFromProp(JsonObject talkObj,String key) {
        return readValueFromProp(talkObj,key,new JsonString(""));
    }

    private static JsonNode readValueFromProp(JsonObject talkObj,String key,JsonNode defaultValue) {
        return talkObj.requiredObject("data")
                .objectValue(key).orElse(JsonFactory.jsonObject().put("value",defaultValue))
                .value("value").orElseThrow(() -> new RuntimeException("Unknown property " + key));

    }


    private JsonArray allSubmittedTalksFromConference(String conferenceid) {
        JsonArray conferences = allTalkFromConferenceSleepingPillFormat(conferenceid);
        JsonArray withoutDraft = JsonArray.fromNodeStream(conferences.objectStream().filter(ob -> !"DRAFT".equals(ob.requiredString("status"))));

        return withoutDraft;
    }

    public JsonArray allTalkFromConferenceSleepingPillFormat(String conferenceid) {
        String url = Configuration.sleepingPillBaseLocation() + "/data/conference/" +conferenceid + "/session";
        URLConnection urlConnection = openConnection(url);
        JsonObject jsonObject;
        try (InputStream inputStream = urlConnection.getInputStream()) {
            jsonObject = JsonParser.parseToObject(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.requiredArray("sessions");
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

    public static void checkWriteAccess(UserAccessType userAccessType) {
        if (Configuration.noAuthMode()) {
            return;
        }
        switch (userAccessType) {
            case FULL:
            case WRITE:
            case OPENSERVLET:
                return;
            default:
                throw new NoUserAceessException();
        }
    }



    public void updateTags(String ref,List<TagWithAuthor> tags,UserWithAccess userWithAccess, String lastModified) {
        checkWriteAccess(userWithAccess.userAccessType);

        JsonObject oneTalkStripped = oneTalkStripped(ref);

        final Optional<JsonObject> updatePayload = computeUpdatedTags(tags, oneTalkStripped);

        if (updatePayload.isPresent()) {
            sendTalkUpdate(ref,updatePayload.get().put("lastUpdated",lastModified));

        }
    }

    public String update(String ref, List<TagWithAuthor> taglist, List<String> keywords,String state, String lastModified, UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        JsonObject oneTalkStripped = oneTalkStripped(ref);
        String currentstate = oneTalkStripped.requiredString("state");
        JsonArray currentKeywords = oneTalkStripped.requiredArray("keywords");

        JsonArray newkeywords = JsonArray.fromStringList(keywords);

        JsonObject payload = computeUpdatedTags(taglist, oneTalkStripped).orElse(new JsonObject());

        if (!newkeywords.equals(currentKeywords)) {
            JsonObject input = JsonFactory.jsonObject();
            input.put("keywords", jsonObject().put("value", newkeywords).put("privateData", true));
            payload.put("data", input);
        }

        if ((!currentstate.equals(state)) && (Configuration.noAuthMode() || userAccessType == UserAccessType.FULL)) {
            payload.put("status", state);
        }

        if (payload.isEmpty()) {
            return fetchOneTalk(ref);

        }
        //payload.put("lastUpdated",lastModified);
        sendTalkUpdate(ref, payload);
        return fetchOneTalk(ref);
    }

    private Optional<JsonObject> computeUpdatedTags(List<TagWithAuthor> taglist, JsonObject oneTalkStripped) {
        JsonArray updatedTags = TagsHandler.INSTANCE.computeTagChanges(taglist, oneTalkStripped.requiredArray("tagswithauthor"));
        if (updatedTags == null) {
            return Optional.empty();
        }
        JsonObject payload = new JsonObject();
        JsonObject input = JsonFactory.jsonObject();
        input.put("tagswithauthor", jsonObject().put("value", updatedTags).put("privateData", true));
        input.put("tags", jsonObject().put("value", new JsonArray()).put("privateData", true));
        payload.put("data", input);
        return Optional.of(payload);
    }

    public JsonObject sendTalkUpdate(String ref, JsonObject payload) {
        String url = Configuration.sleepingPillBaseLocation() + "/data/session/" + ref;


        HttpURLConnection conn = openConnection(url);

        try {
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"))) {
                payload.toJson(printWriter);
            }
            try (InputStream is = conn.getInputStream()) {
                return JsonParser.parseToObject(is);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void approveTalk(String ref, UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        JsonObject payload = jsonObject().put("status", "APPROVED");
        sendTalkUpdate(ref,payload);
    }

    private String confirmTalkMessage(String status, String message) {
        JsonObject jsonObject = JsonFactory.jsonObject();

        jsonObject.put("status",status);
        jsonObject.put("message",message);
        return jsonObject.toString();
    }

    public String confirmTalk(String ref, String dinner,UserWithAccess userWithAccess) {
        checkWriteAccess(userWithAccess.userAccessType);
        JsonObject jsonTalk = oneTalkStripped(ref);

        List<TagWithAuthor> origtags = new ArrayList<>(jsonTalk.arrayValue("tagswithauthor").orElse(new JsonArray()).objectStream().map(a -> PojoMapper.map(a, TagWithAuthor.class)).collect(Collectors.toSet()));

        if (origtags.stream().anyMatch(a -> "confirmed".equalsIgnoreCase(a.getTag()))) {
            return confirmTalkMessage("error","Talk has already been confirmed");
        }
        if (origtags.stream().noneMatch(a -> "accepted".equalsIgnoreCase(a.getTag()))) {
            return confirmTalkMessage("error","Talk is not accepted");
        }
        if ("yes".equals(dinner)) {
            origtags.add(new TagWithAuthor("dinner",userWithAccess.username));
        }
        origtags.add(new TagWithAuthor("confirmed",userWithAccess.username));

        updateTags(ref,origtags,userWithAccess,jsonTalk.requiredString("lastModified"));

        return confirmTalkMessage("ok", "ok");
    }


    public JsonObject addPublicComment(String ref, String comment, String lastModified) {
        JsonObject payload = JsonFactory.jsonObject();
        payload.put("lastUpdated",lastModified);

        JsonObject commentobj = JsonFactory.jsonObject().put("email", "program@java.no").put("from", "JavaZone program commitee").put("comment", comment);

        payload.put("comments",JsonFactory.jsonArray().add(commentobj));

        JsonObject jsonObject = sendTalkUpdate(ref, payload);
        return jsonObject;
    }

    public void pubishChanges(String talkref, UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        String url = Configuration.sleepingPillBaseLocation() + "/data/session/" + talkref + "/publish";


        HttpURLConnection conn = openConnection(url);

        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"))) {
                JsonFactory.jsonObject().toJson(printWriter);
            }
            try (InputStream is = conn.getInputStream()) {
                 JsonParser.parseToObject(is);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSlotTime(String talkref, String startTimeStr, UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(startTimeStr);
        LocalDateTime startTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime();

        updateSlotTime(talkref, startTime,userAccessType);
    }

    public void updateSlotTime(String talkref, LocalDateTime startTime,UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        JsonObject jsonObject = oneTalkSleepingPillFormat(talkref);
        int length = Integer.parseInt(readValueFromProp(jsonObject, "length").stringValue());
        LocalDateTime endTime = startTime.plusMinutes(length);

        JsonObject payload = JsonFactory.jsonObject()
                .put("startTime", JsonFactory.jsonObject().put("value", startTime.toString()).put("privateData", false))
                .put("endTime", JsonFactory.jsonObject().put("value", endTime.toString()).put("privateData", false));
        sendTalkUpdate(talkref,JsonFactory.jsonObject().put("data",payload));
    }

    public void updateRoom(String talkref, String room, UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        JsonObject payload = JsonFactory.jsonObject()
                .put("room", JsonFactory.jsonObject().put("value", room).put("privateData", false));
        sendTalkUpdate(talkref,JsonFactory.jsonObject().put("data",payload));
    }

    public void updateRegisterLoc(String talkref, String registerLoc, UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        JsonObject payload = JsonFactory.jsonObject()
                .put("registerLoc", JsonFactory.jsonObject().put("value", registerLoc).put("privateData", false));
        sendTalkUpdate(talkref,JsonFactory.jsonObject().put("data",payload));
    }

    public void updateVideo(String talkref,String video) {
        JsonObject payload = JsonFactory.jsonObject()
                .put("video", JsonFactory.jsonObject().put("value", video).put("privateData", false));
        sendTalkUpdate(talkref,JsonFactory.jsonObject().put("data",payload));
    }
}
