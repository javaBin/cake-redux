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
            ObjectNode jsonObject = readTalk(talkItem, connection);
            String submititLocation = Configuration.submititLocation() + encodedUrl;
            jsonObject.put("submititLoc",submititLocation);
            jsonObject.put("eventId",eventFromTalk(url));
            return jsonObject.toString();
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
        ArrayNode allTalk = JsonNodeFactory.instance.arrayNode();
        for (Item item : items) {
            ObjectNode jsonTalk = readItemProperties(item, null);
            addSpeakersToTalkFromLink(allTalk, item, jsonTalk);

            readRoom(item, jsonTalk);
            readSlot(item, jsonTalk);
        }
        return allTalk.toString();
    }

    private void addSpeakersToTalkFromLink(ArrayNode allTalk, Item item, ObjectNode jsonTalk) {
        List<Link> links = item.getLinks();
        ArrayNode speakers = JsonNodeFactory.instance.arrayNode();
        for (Link link : links) {
            if (!"speaker item".equals(link.getRel())) {
                continue;
            }
            ObjectNode speaker = JsonNodeFactory.instance.objectNode();
            speaker.put("name",link.getPrompt().get().toString());
            speakers.add(speaker);
        }
        jsonTalk.put("speakers",speakers);
        allTalk.add(jsonTalk);
    }


    public String talksFullVersion(String encodedEvent) {
        List<Item> items = getAllTalksSummary(encodedEvent);
        // TODO There has to be a better way to do this
        ArrayNode talkArray = JsonNodeFactory.instance.arrayNode();
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
            ObjectNode jsonTalk = readTalk(talkIktem, talkConn);
            talkArray.add(jsonTalk);
        }

        return talkArray.toString();
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

    private ObjectNode readTalk(Item item, URLConnection connection) {
        ObjectNode jsonTalk = readItemProperties(item, connection);

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
        ArrayNode jsonSpeakers = JsonNodeFactory.instance.arrayNode();
        for (Item speaker : speakers.getItems()) {
            ObjectNode jsonSpeaker = readItemProperties(speaker,speakerConnection);
            jsonSpeakers.add(jsonSpeaker);
        }

        jsonTalk.put("speakers", jsonSpeakers);
        return jsonTalk;
    }

    private void readRoom(Item item, ObjectNode jsonTalk) {
        Optional<Link> roomLinkOpt = item.linkByRel("room item");
        if (roomLinkOpt.isSome()) {
            Link roomLink = roomLinkOpt.get();
            String roomName = roomLink.getPrompt().get();
            String ref = roomLink.getHref().toString();
            ObjectNode room = JsonNodeFactory.instance.objectNode();
            room.put("name",roomName);
            room.put("ref", Base64Util.encode(ref));
            jsonTalk.put("room",room);
        }
    }

    private void readSlot(Item item, ObjectNode jsonTalk) {
        Optional<Link> slotLinkOpt = item.linkByRel("slot item");
        if (slotLinkOpt.isSome()) {
            Link slotLink = slotLinkOpt.get();
            String ref = slotLink.getHref().toString();
            String slotcode = slotLink.getPrompt().get();
            SlotTimeFormatter slotTimeFormatter = new SlotTimeFormatter(slotcode);
            ObjectNode slot = JsonNodeFactory.instance.objectNode();
            slot.put("ref", Base64Util.encode(ref));
            slot.put("start",slotTimeFormatter.getStart());
            slot.put("end",slotTimeFormatter.getEnd());
            jsonTalk.put("slot",slot);
        }
    }

    private ObjectNode readItemProperties(Item item, URLConnection connection) {
        ObjectNode itemAsJson = JsonNodeFactory.instance.objectNode();
        Map<String,Property> dataAsMap = item.getData().getDataAsMap();
        for (Map.Entry<String,Property> propentry : dataAsMap.entrySet()) {
            String key = propentry.getKey();
            Property property = propentry.getValue();
            if (property.hasArray()) {
                List<Value> array = property.getArray();
                ArrayNode values = JsonNodeFactory.instance.arrayNode();
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
            itemAsJson.put("lastModified",lastModified);
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
