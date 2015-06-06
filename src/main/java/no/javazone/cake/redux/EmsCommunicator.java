package no.javazone.cake.redux;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.hamnaberg.funclite.Optional;
import net.hamnaberg.json.*;
import net.hamnaberg.json.parser.CollectionParser;
import org.jsonbuddy.JsonArrayFactory;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObjectFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EmsCommunicator {


    public String updateTags(String encodedTalkUrl,List<String> tags,String givenLastModified) {
        Property newVals = Property.arrayObject("tags", new ArrayList<Object>(tags));

        return update(encodedTalkUrl, givenLastModified, Arrays.asList(newVals));
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
            toString(is);
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

    public String confirmTalk(String encodedTalkUrl, String dinner) {
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
            updateTags(encodedTalkUrl,tags, lastModified);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return confirmTalkMessage("ok", "ok");
    }

    public String allEvents()  {
        try {
            URLConnection connection = openConnection(Configuration.emsEventLocation(), false);
            Collection events = new CollectionParser().parse(openStream(connection));
            List<Item> items = events.getItems();

            JsonArrayFactory eventArrayFactor = JsonFactory.jsonArray();
            for (Item item : items) {
                Data data = item.getData();
                String eventname = data.propertyByName("name").get().getValue().get().asString();
                String slug = data.propertyByName("slug").get().getValue().get().asString();
                String href = item.getHref().get().toString();

                href = Base64Util.encode(href);

                JsonObjectFactory jsonObjectFactory = JsonFactory.jsonObject()
                        .withValue("name", eventname)
                        .withValue("ref", href)
                        .withValue("slug", slug);

                eventArrayFactor.add(jsonObjectFactory);


            }

            return eventArrayFactor.create().toJson();
        } catch (IOException  e) {
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


    public String fetchOneTalk(String encodedUrl) {
        String url = Base64Util.decode(encodedUrl);
        URLConnection connection = openConnection(url, true);

        try {
            InputStream is = openStream(connection);
            Item talkItem = new CollectionParser().parse(is).getFirstItem().get();
            JsonObjectFactory jsonObject = readTalk(talkItem, connection);
            String submititLocation = Configuration.submititLocation() + encodedUrl;
            jsonObject.withValue("submititLoc",submititLocation);
            jsonObject.withValue("eventId",eventFromTalk(url));
            return jsonObject.create().toJson();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String eventFromTalk(String url) {
        int pos = url.indexOf("/sessions");
        String eventUrl = url.substring(0,pos);
        return Base64Util.encode(eventUrl);
    }

    private InputStream openStream(URLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        if (true) { // flip for debug :)
            return inputStream;
        }
        String stream = toString(inputStream);
        System.out.println("***STRAN***");
        System.out.println(stream);
        return new ByteArrayInputStream(stream.getBytes());
    }

    public String assignRoom(String encodedTalk,String encodedRoomRef,String givenLastModified) {
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
            toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fetchOneTalk(encodedTalk);
    }

    public String assignSlot(String encodedTalk,String encodedSlotRef,String givenLastModified) {
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
            toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fetchOneTalk(encodedTalk);
    }

    public String publishTalk(String encodedTalkUrl,String givenLastModified) {
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
           toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fetchOneTalk(encodedTalkUrl);
    }

    public String talkShortVersion(String encodedEvent) {
        List<Item> items = getAllTalksSummary(encodedEvent);
        JsonArrayFactory allTalks = JsonFactory.jsonArray();
        for (Item item : items) {
            JsonObjectFactory jsonTalk = readItemProperties(item, null);
            addSpeakersToTalkFromLink(allTalks, item, jsonTalk);

            readRoom(item, jsonTalk);
            readSlot(item, jsonTalk);
        }
        return allTalks.create().toJson();
    }

    private void addSpeakersToTalkFromLink(JsonArrayFactory allTalk, Item item, JsonObjectFactory jsonTalk) {
        List<Link> links = item.getLinks();
        JsonArrayFactory speakers = JsonFactory.jsonArray();
        for (Link link : links) {
            if (!"speaker item".equals(link.getRel())) {
                continue;
            }
            JsonObjectFactory speaker = JsonFactory.jsonObject();
            speaker.withValue("name", link.getPrompt().get().toString());
            speakers.add(speaker);
        }
        jsonTalk.withValue("speakers", speakers);
        allTalk.add(jsonTalk);
    }


    public String talksFullVersion(String encodedEvent) {
        List<Item> items = getAllTalksSummary(encodedEvent);
        // TODO There has to be a better way to do this
        JsonArrayFactory talkArray = JsonFactory.jsonArray();
        int num=items.size();
        for (Item item : items) {
            System.out.println(num--);
            URLConnection talkConn = openConnection(item.getHref().get().toString(), true);
            Item talkIktem = null;
            try (InputStream talkInpStr = talkConn.getInputStream()) {
                talkIktem = new CollectionParser().parse(talkInpStr).getFirstItem().get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JsonObjectFactory jsonTalk = readTalk(talkIktem, talkConn);
            talkArray.add(jsonTalk);
        }

        return talkArray.create().toJson();
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

    private JsonObjectFactory readTalk(Item item, URLConnection connection) {
        JsonObjectFactory jsonTalk = readItemProperties(item, connection);

        readRoom(item, jsonTalk);
        readSlot(item, jsonTalk);

        String speakerLink = item.linkByRel("speaker collection").get().getHref().toString();

        URLConnection speakerConnection = openConnection(speakerLink, true);

        Collection speakers;
        try (InputStream speakInpStream = speakerConnection.getInputStream()) {
            speakers = new CollectionParser().parse(speakInpStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonArrayFactory jsonSpeakers = JsonFactory.jsonArray();
        for (Item speaker : speakers.getItems()) {
            JsonObjectFactory jsonSpeaker = readItemProperties(speaker, speakerConnection);
            jsonSpeakers.add(jsonSpeaker);
        }

        jsonTalk.withValue("speakers", jsonSpeakers);
        return jsonTalk;
    }



    private void readRoom(Item item, JsonObjectFactory jsonTalk) {
        Optional<Link> roomLinkOpt = item.linkByRel("room item");
        if (roomLinkOpt.isSome()) {
            Link roomLink = roomLinkOpt.get();
            String roomName = roomLink.getPrompt().get();
            String ref = roomLink.getHref().toString();
            JsonObjectFactory room = JsonFactory.jsonObject();
            room.withValue("name", roomName);
            room.withValue("ref", Base64Util.encode(ref));
            jsonTalk.withValue("room", room);
        }
    }

    private void readSlot(Item item, JsonObjectFactory jsonTalk) {
        Optional<Link> slotLinkOpt = item.linkByRel("slot item");
        if (slotLinkOpt.isSome()) {
            Link slotLink = slotLinkOpt.get();
            String ref = slotLink.getHref().toString();
            String slotcode = slotLink.getPrompt().get();
            SlotTimeFormatter slotTimeFormatter = new SlotTimeFormatter(slotcode);
            JsonObjectFactory slot = JsonObjectFactory.jsonObject()
                    .withValue("ref", Base64Util.encode(ref))
                    .withValue("start",slotTimeFormatter.getStart())
                    .withValue("end",slotTimeFormatter.getEnd());
            jsonTalk.withValue("slot",slot);
        }
    }


    private JsonObjectFactory readItemProperties(Item item, URLConnection connection) {
        JsonObjectFactory itemAsJson = JsonFactory.jsonObject();
        Map<String,Property> dataAsMap = item.getData().getDataAsMap();
        for (Map.Entry<String,Property> propentry : dataAsMap.entrySet()) {
            String key = propentry.getKey();
            Property property = propentry.getValue();
            if (property.hasArray()) {
                List<Value> array = property.getArray();
                JsonArrayFactory values = JsonFactory.jsonArray();
                for (Value val : array) {
                    values.add(val.asString());
                }
                itemAsJson.withValue(key, values);

            } else if (property.hasValue()) {
                itemAsJson.withValue(key, property.getValue().get().asString());
            }
        }
        String href = item.getHref().get().toString();
        itemAsJson.withValue("ref", Base64Util.encode(href));
        String lastModified = (connection != null) ? connection.getHeaderField("last-modified") : null;
        if (lastModified != null) {
            itemAsJson.withValue("lastModified", lastModified);
        }
        return itemAsJson;
    }



    private static URLConnection openConnection(String questionUrl, boolean useAuthorization)  {
        try {
            URL url = new URL(questionUrl);
            URLConnection urlConnection = url.openConnection();

            if (useAuthorization) {
                String authString = Configuration.getEmsUser() + ":" + Configuration.getEmsPassword();
                String authStringEnc = Base64Util.encode(authString);
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            }

            return urlConnection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char)c);
            }
            return result.toString();
        }
    }

    public String update(String ref, List<String> taglist, String state, String lastModified) {
        Property newTag = Property.arrayObject("tags", new ArrayList<Object>(taglist));
        Property newState = Property.value("state",state);
        return update(ref, lastModified, Arrays.asList(newTag,newState));
    }
}
