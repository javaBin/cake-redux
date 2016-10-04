package no.javazone.cake.redux;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.hamnaberg.funclite.Optional;
import net.hamnaberg.json.*;
import net.hamnaberg.json.parser.CollectionParser;
import org.jsonbuddy.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static no.javazone.cake.redux.CommunicatorHelper.openConnection;
import static no.javazone.cake.redux.CommunicatorHelper.openStream;

public class EmsCommunicator {


    public String updateTags(String encodedTalkUrl,List<String> tags,String givenLastModified,UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        Property newVals = Property.arrayObject("tags", new ArrayList<Object>(tags));
        return update(encodedTalkUrl, givenLastModified, Arrays.asList(newVals));
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

    private String update(String encodedTalkUrl, String givenLastModified, List<Property> newVals) {
        String talkUrl = Base64Util.decode(encodedTalkUrl);
        URLConnection connection = openConnection(talkUrl, true);
        String lastModified = connection.getHeaderField("last-modified");

        if (!lastModified.equals(givenLastModified)) {
            ObjectNode errorJson = JsonNodeFactory.instance.objectNode();
            errorJson.put("error","Talk has been updated at " + lastModified + " not " + givenLastModified);
            return errorJson.toString();
        }

        Data data;
        try (InputStream inputStream = connection.getInputStream()) {
            data = new CollectionParser().parse(inputStream).getFirstItem().get().getData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Property prop : newVals) {
            data = data.replace(prop);
        }
        Template template = Template.create(data.getDataAsMap().values());

        HttpURLConnection putConnection = (HttpURLConnection) openConnection(talkUrl, true);

        putConnection.setDoOutput(true);
        try {
            putConnection.setRequestMethod("PUT");
            putConnection.setRequestProperty("content-type","application/vnd.collection+json");
            putConnection.setRequestProperty("if-unmodified-since",lastModified);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }


        try (OutputStream outputStream = putConnection.getOutputStream()) {
            template.writeTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = putConnection.getInputStream()) {
            CommunicatorHelper.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fetchOneTalk(encodedTalkUrl);
    }

    private String confirmTalkMessage(String status, String message) {
        ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();

        jsonObject.put("status",status);
        jsonObject.put("message",message);
        return jsonObject.toString();
    }

    public String confirmTalk(String encodedTalkUrl, String dinner,UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonTalk = objectMapper.readTree(fetchOneTalk(encodedTalkUrl));

            ArrayNode tagsarr = (ArrayNode) jsonTalk.get("tags");
            List<String> tags = new ArrayList<>();
            for (int i=0;i<tagsarr.size();i++) {
                String atag = tagsarr.get(i).asText();
                tags.add(atag);
            }
            if (tags.contains("confirmed")) {
                return confirmTalkMessage("error","Talk has already been confirmed");
            }
            if (!tags.contains("accepted")) {
                return confirmTalkMessage("error","Talk is not accepted");
            }
            if ("yes".equals(dinner)) {
                tags.add("dinner");
            }
            tags.add("confirmed");
            String lastModified = jsonTalk.get("lastModified").asText();
            updateTags(encodedTalkUrl,tags, lastModified,userAccessType);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return confirmTalkMessage("ok", "ok");
    }

    public String allEvents()  {
        return eventJsonNodes().toJson();
    }

    private JsonArray eventJsonNodes() {
        try {
            URLConnection connection = openConnection(Configuration.emsEventLocation(), false);
            Collection events = new CollectionParser().parse(openStream(connection));
            List<Item> items = events.getItems();

            JsonArray eventArray = JsonFactory.jsonArray();
            for (Item item : items) {
                Data data = item.getData();
                String eventname = data.propertyByName("name").get().getValue().get().asString();
                String slug = data.propertyByName("slug").get().getValue().get().asString();
                String href = item.getHref().get().toString();

                href = Base64Util.encode(href);

                JsonObject jsonObjectFactory = JsonFactory.jsonObject()
                        .put("name", eventname)
                        .put("ref", href)
                        .put("slug", slug);

                eventArray.add(jsonObjectFactory);


            }
            return eventArray;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String allRoomsAndSlots(String encodedEventid) {
        String eventid = Base64Util.decode(encodedEventid);
        ArrayNode roomArray = allRooms(eventid);
        ArrayNode slotArray = allSlots(eventid);
        ObjectNode all = JsonNodeFactory.instance.objectNode();
        all.put("rooms",roomArray);
        all.put("slots",slotArray);
        return all.toString();
    }

    private ArrayNode allRooms(String eventid) {
        String loc = eventid + "/rooms";
        URLConnection connection = openConnection(loc, false);
        try {
            ArrayNode roomArray = JsonNodeFactory.instance.arrayNode();
            Collection events = new CollectionParser().parse(connection.getInputStream());
            List<Item> items = events.getItems();
            for (Item item : items) {
                Data data = item.getData();
                String roomname = data.propertyByName("name").get().getValue().get().asString();
                String href = item.getHref().get().toString();

                href = Base64Util.encode(href);

                ObjectNode event = JsonNodeFactory.instance.objectNode();

                event.put("name",roomname);
                event.put("ref",href);

                roomArray.add(event);
            }
            return roomArray;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayNode allSlots(String eventid) {
        String loc = eventid + "/slots";
        URLConnection connection = openConnection(loc, false);
        try {
            ArrayNode slotArray = JsonNodeFactory.instance.arrayNode();
            Collection events = new CollectionParser().parse(openStream(connection));
            List<Item> items = events.getItems();
            for (Item item : items) {
                Data data = item.getData();
                String start = data.propertyByName("start").get().getValue().get().asString();
                int duration = data.propertyByName("duration").get().getValue().get().asNumber().intValue();
                String href = item.getHref().get().toString();

                SlotTimeFormatter slotTimeFormatter = new SlotTimeFormatter(start,duration);

                href = Base64Util.encode(href);

                ObjectNode slot = JsonNodeFactory.instance.objectNode();

                slot.put("start", slotTimeFormatter.getStart());
                slot.put("end", slotTimeFormatter.getEnd());
                slot.put("length", slotTimeFormatter.getLength());
                slot.put("ref", href);


                slotArray.add(slot);
            }
            return slotArray;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject oneTalkAsJson(String encodedUrl) {
        String url = Base64Util.decode(encodedUrl);
        URLConnection connection = openConnection(url, true);

        try {
            InputStream is = openStream(connection);
            Item talkItem = new CollectionParser().parse(is).getFirstItem().get();
            JsonObject jsonObject = readTalk(url,talkItem, connection);
            String submititLocation = Configuration.submititLocation() + encodedUrl;
            jsonObject.put("submititLoc",submititLocation);
            jsonObject.put("eventId",eventFromTalk(url));
            return jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String fetchOneTalk(String encodedUrl) {
        return oneTalkAsJson(encodedUrl).toJson();
    }

    private String eventFromTalk(String url) {
        int pos = url.indexOf("/sessions");
        String eventUrl = url.substring(0,pos);
        return Base64Util.encode(eventUrl);
    }

    public String assignRoom(String encodedTalk,String encodedRoomRef,String givenLastModified,UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        String talkUrl = Base64Util.decode(encodedTalk);
        String roomRef = Base64Util.decode(encodedRoomRef);
        StringBuilder formData = new StringBuilder();
        try {
            formData.append(URLEncoder.encode("room","UTF-8"));
            formData.append("=");
            formData.append(URLEncoder.encode(roomRef,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection connection = (HttpURLConnection) openConnection(talkUrl, true);

        String lastModified = connection.getHeaderField("last-modified");

        if (!lastModified.equals(givenLastModified)) {
            ObjectNode errorJson = JsonNodeFactory.instance.objectNode();
            errorJson.put("error","Talk has been updated at " + lastModified + " not " + givenLastModified);
            return errorJson.toString();
        }

        HttpURLConnection postConnection = (HttpURLConnection) openConnection(talkUrl, true);

        postConnection.setDoOutput(true);
        try {
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("content-type","application/x-www-form-urlencoded");
            postConnection.setRequestProperty("if-unmodified-since",lastModified);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }



        try {
            DataOutputStream wr = new DataOutputStream(postConnection.getOutputStream());
            wr.writeBytes(formData.toString());
            wr.flush();
            wr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = postConnection.getInputStream()) {
            CommunicatorHelper.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fetchOneTalk(encodedTalk);
    }

    public void addRoomToEvent(String eventid,String roomname,UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        JsonObject roomtemplate = JsonFactory.jsonObject().put("template", JsonFactory.jsonObject().put("data",
                JsonFactory.jsonArray().add(
                        JsonFactory.jsonObject().put("name", "name").put("value", roomname)
                )));

        StringWriter out = new StringWriter();
        roomtemplate.toJson(new PrintWriter(out));
        System.out.println(out);

        String url = eventid + "/rooms";
        HttpURLConnection postConnection = (HttpURLConnection) openConnection(url, true);

        postConnection.setDoOutput(true);
        try {
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("content-type","application/vnd.collection+json");
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(postConnection.getOutputStream()))) {
            roomtemplate.toJson(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = postConnection.getInputStream()) {
            String res = CommunicatorHelper.toString(is);
            System.out.println(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void addSlotToEvent(UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        String url = "http://test.javazone.no/ems/server/events/0e6d98e9-5b06-42e7-b275-6abadb498c81/slots";
        JsonObject roomtemplate = JsonFactory.jsonObject().put("template", JsonFactory.jsonObject().put("data",
                JsonFactory.jsonArray()
                        .add(JsonFactory.jsonObject().put("name", "start").put("value", "2016-09-09T15:00:00Z"))
                        .add(JsonFactory.jsonObject().put("name", "duration").put("value", new JsonNumber(10)))
                ));

        HttpURLConnection postConnection = (HttpURLConnection) openConnection(url, true);

        postConnection.setDoOutput(true);
        try {
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("content-type","application/vnd.collection+json");
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(postConnection.getOutputStream()))) {
            roomtemplate.toJson(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = postConnection.getInputStream()) {
            String res = CommunicatorHelper.toString(is);
            System.out.println(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String assignSlot(String encodedTalk,String encodedSlotRef,String givenLastModified,UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        String talkUrl = Base64Util.decode(encodedTalk);
        String slotRef = Base64Util.decode(encodedSlotRef);
        StringBuilder formData = new StringBuilder();
        try {
            formData.append(URLEncoder.encode("slot","UTF-8"));
            formData.append("=");
            formData.append(URLEncoder.encode(slotRef,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection connection = (HttpURLConnection) openConnection(talkUrl, true);

        String lastModified = connection.getHeaderField("last-modified");

        if (!lastModified.equals(givenLastModified)) {
            ObjectNode errorJson = JsonNodeFactory.instance.objectNode();
            errorJson.put("error","Talk has been updated at " + lastModified + " not " + givenLastModified);
            return errorJson.toString();
        }

        HttpURLConnection postConnection = (HttpURLConnection) openConnection(talkUrl, true);

        postConnection.setDoOutput(true);
        try {
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("content-type","application/x-www-form-urlencoded");

            postConnection.setRequestProperty("if-unmodified-since",lastModified);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }



        try {
            DataOutputStream wr = new DataOutputStream(postConnection.getOutputStream());
            wr.writeBytes(formData.toString());
            wr.flush();
            wr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = postConnection.getInputStream()) {
            CommunicatorHelper.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fetchOneTalk(encodedTalk);
    }

    public String publishTalk(String encodedTalkUrl,String givenLastModified,UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        String talkUrl = Base64Util.decode(encodedTalkUrl);
        HttpURLConnection connection = (HttpURLConnection) openConnection(talkUrl, true);

        String lastModified = connection.getHeaderField("last-modified");
        if (!lastModified.equals(givenLastModified)) {
            ObjectNode errorJson = JsonNodeFactory.instance.objectNode();
            errorJson.put("error","Talk has been updated at " + lastModified + " not " + givenLastModified);
            return errorJson.toString();
        }

        String publishLink;
        try (InputStream inputStream = connection.getInputStream()) {
            Collection parse = new CollectionParser().parse(inputStream);
            Item talkItem = parse.getFirstItem().get();
            Optional<Link> publish = talkItem.linkByRel("publish");
            publishLink = publish.get().getHref().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection postConnection = (HttpURLConnection) openConnection(publishLink, true);

        postConnection.setDoOutput(true);
        try {
            postConnection.setRequestMethod("POST");
            postConnection.setRequestProperty("content-type","text/uri-list");
            postConnection.setRequestProperty("if-unmodified-since",lastModified);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }


        try (OutputStream outputStream = postConnection.getOutputStream()) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream))) {
                writer.println(talkUrl);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = postConnection.getInputStream()) {
           CommunicatorHelper.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fetchOneTalk(encodedTalkUrl);
    }

    public String talkShortVersion(String encodedEvent) {
        List<Item> items = getAllTalksSummary(encodedEvent);
        JsonArray allTalks = JsonFactory.jsonArray();
        for (Item item : items) {
            JsonObject jsonTalk = readItemProperties(item, null);
            addSpeakersToTalkFromLink(allTalks, item, jsonTalk);

            readRoom(item, jsonTalk);
            readSlot(item, jsonTalk);
        }
        allTalks = JsonArray.fromNodeStream(allTalks.nodeStream()
                .sorted((n1,n2) -> n1.requiredString("title").compareTo(n2.requiredString("title"))));

        return allTalks.toJson();
    }

    private void addSpeakersToTalkFromLink(JsonArray allTalk, Item item, JsonObject jsonTalk) {
        List<Link> links = item.getLinks();
        JsonArray speakers = JsonFactory.jsonArray();
        for (Link link : links) {
            if (!"speaker item".equals(link.getRel())) {
                continue;
            }
            JsonObject speaker = JsonFactory.jsonObject();
            speaker.put("name", link.getPrompt().get().toString());
            speakers.add(speaker);
        }
        jsonTalk.put("speakers", speakers);
        allTalk.add(jsonTalk);
    }


    public String talksFullVersion(String encodedEvent) {
        List<Item> items = getAllTalksSummary(encodedEvent);
        // TODO There has to be a better way to do this
        JsonArray talkArray = JsonFactory.jsonArray();
        int num=items.size();
        for (Item item : items) {
            System.out.println(num--);
            String url = item.getHref().get().toString();
            URLConnection talkConn = openConnection(url, true);
            Item talkIktem = null;
            try (InputStream talkInpStr = talkConn.getInputStream()) {
                talkIktem = new CollectionParser().parse(talkInpStr).getFirstItem().get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JsonObject jsonTalk = readTalk(url, talkIktem, talkConn);
            talkArray.add(jsonTalk);
        }

        return talkArray.toJson();
    }

    private List<Item> getAllTalksSummary(String encodedEvent) {
        String url = Base64Util.decode(encodedEvent) + "/sessions";

        URLConnection connection = openConnection(url, true);
        Collection events;
        try {
            events = new CollectionParser().parse(openStream(connection));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return events.getItems();
    }

    private JsonObject readTalk(String talkUrl, Item item, URLConnection connection) {
        JsonObject jsonTalk = readItemProperties(item, connection);

        readRoom(item, jsonTalk);
        readSlot(item, jsonTalk);
        readPermalink(item,jsonTalk);

        String speakerLink = item.linkByRel("speaker collection").get().getHref().toString();

        URLConnection speakerConnection = openConnection(speakerLink, true);

        Collection speakers;
        try (InputStream speakInpStream = speakerConnection.getInputStream()) {
            speakers = new CollectionParser().parse(speakInpStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonArray jsonSpeakers = JsonFactory.jsonArray();
        for (Item speaker : speakers.getItems()) {
            JsonObject jsonSpeaker = readItemProperties(speaker, speakerConnection);
            jsonSpeakers.add(jsonSpeaker);
        }

        jsonTalk.put("speakers", jsonSpeakers);
        java.util.Optional<String> eventSlug = findEventSlug(talkUrl);
        if (eventSlug.isPresent()) {
            jsonTalk.put("eventSlug", eventSlug.get());
        }
        return jsonTalk;
    }

    private java.util.Optional<String> findEventSlug(String talkUrl) {
        JsonArray evnetnodes = eventJsonNodes();
        java.util.Optional<String> eventSlug = evnetnodes.nodeStream()
                .map(jn -> (JsonObject) jn)
                .filter(jo -> talkUrl.startsWith(Base64Util.decode(jo.requiredString("ref"))))
                .findAny()
                .map(jo -> jo.requiredString("slug"));
        return eventSlug;
    }


    private void readRoom(Item item, JsonObject jsonTalk) {
        Optional<Link> roomLinkOpt = item.linkByRel("room item");
        if (roomLinkOpt.isSome()) {
            Link roomLink = roomLinkOpt.get();
            String roomName = roomLink.getPrompt().get();
            String ref = roomLink.getHref().toString();
            JsonObject room = JsonFactory.jsonObject();
            room.put("name", roomName);
            room.put("ref", Base64Util.encode(ref));
            jsonTalk.put("room", room);
        }
    }

    private void readPermalink(Item item,JsonObject jsonTalk) {
        Optional<Link> alternate = item.linkByRel("alternate");
        if (alternate.isSome()) {
            String link = alternate.get().getHref().toString();
            String permalink = link.substring(link.lastIndexOf("/")+1);
            jsonTalk.put("permalink",permalink);
        }
    }

    private void readSlot(Item item, JsonObject jsonTalk) {
        Optional<Link> slotLinkOpt = item.linkByRel("slot item");
        if (slotLinkOpt.isSome()) {
            Link slotLink = slotLinkOpt.get();
            String ref = slotLink.getHref().toString();
            String slotcode = slotLink.getPrompt().get();
            SlotTimeFormatter slotTimeFormatter = new SlotTimeFormatter(slotcode);
            JsonObject slot = JsonFactory.jsonObject()
                    .put("ref", Base64Util.encode(ref))
                    .put("start",slotTimeFormatter.getStart())
                    .put("end",slotTimeFormatter.getEnd());
            jsonTalk.put("slot",slot);
        }
    }


    private JsonObject readItemProperties(Item item, URLConnection connection) {
        JsonObject itemAsJson = JsonFactory.jsonObject();
        Map<String,Property> dataAsMap = item.getData().getDataAsMap();
        for (Map.Entry<String,Property> propentry : dataAsMap.entrySet()) {
            String key = propentry.getKey();
            Property property = propentry.getValue();
            if (property.hasArray()) {
                List<Value> array = property.getArray();
                JsonArray values = JsonFactory.jsonArray();
                for (Value val : array) {
                    values.add(val.asString());
                }
                itemAsJson.put(key, values);

            } else if (property.hasValue()) {
                itemAsJson.put(key, property.getValue().get().asString());
            }
        }
        String href = item.getHref().get().toString();
        itemAsJson.put("ref", Base64Util.encode(href));
        String lastModified = (connection != null) ? connection.getHeaderField("last-modified") : null;
        if (lastModified != null) {
            itemAsJson.put("lastModified", lastModified);
        }
        return itemAsJson;
    }


    public String update(String ref, List<String> taglist, String state, String lastModified,UserAccessType userAccessType) {
        checkWriteAccess(userAccessType);
        Property newTag = Property.arrayObject("tags", new ArrayList<>(taglist));
        Property newState = Property.value("state",state);
        return update(ref, lastModified, Arrays.asList(newTag,newState));
    }
}
