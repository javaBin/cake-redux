package no.javazone.cake.redux;

import net.hamnaberg.funclite.Optional;
import net.hamnaberg.json.*;
import net.hamnaberg.json.parser.CollectionParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            JSONObject errorJson = new JSONObject();
            try {
                errorJson.put("error","Talk has been updated at " + lastModified + " not " + givenLastModified);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
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
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status",status);
            jsonObject.put("message",message);
            return jsonObject.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String confirmTalk(String encodedTalkUrl, String dinner) {
        try {
            JSONObject jsonTalk = new JSONObject(fetchOneTalk(encodedTalkUrl));
            JSONArray tagsarr = jsonTalk.getJSONArray("tags");
            List<String> tags = new ArrayList<>();
            for (int i=0;i<tagsarr.length();i++) {
                String atag = (String) tagsarr.get(i);
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
            String lastModified = jsonTalk.getString("lastModified");
            updateTags(encodedTalkUrl,tags, lastModified);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return confirmTalkMessage("ok","ok");
    }

    public String allEvents()  {
        try {
            URLConnection connection = openConnection(Configuration.emsEventLocation(), false);
            Collection events = new CollectionParser().parse(connection.getInputStream());
            List<Item> items = events.getItems();
            JSONArray eventArray = new JSONArray();
            for (Item item : items) {
                Data data = item.getData();
                String eventname = data.propertyByName("name").get().getValue().get().asString();
                String slug = data.propertyByName("slug").get().getValue().get().asString();
                String href = item.getHref().get().toString();

                href = Base64Util.encode(href);

                JSONObject event = new JSONObject();

                event.put("name",eventname);
                event.put("ref",href);
                event.put("slug",slug);

                eventArray.put(event);
            }
            return eventArray.toString();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String allRooms(String encodedEventid) {
        String eventid = Base64Util.decode(encodedEventid);
        String loc = eventid + "/rooms";
        URLConnection connection = openConnection(loc, false);
        try {
            Collection events = new CollectionParser().parse(connection.getInputStream());
            List<Item> items = events.getItems();
            JSONArray roomArray = new JSONArray();
            for (Item item : items) {
                Data data = item.getData();
                String roomname = data.propertyByName("name").get().getValue().get().asString();
                String href = item.getHref().get().toString();

                href = Base64Util.encode(href);

                JSONObject event = new JSONObject();

                event.put("name",roomname);
                event.put("ref",href);

                roomArray.put(event);
            }
            return roomArray.toString();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }




    public String fetchOneTalk(String encodedUrl) {
        String url = Base64Util.decode(encodedUrl);
        URLConnection connection = openConnection(url, true);

        try {
            InputStream is = openStream(connection);
            Item talkItem = new CollectionParser().parse(is).getFirstItem().get();
            JSONObject jsonObject = readTalk(talkItem, connection);
            String submititLocation = Configuration.submititLocation() + encodedUrl;
            try {
                jsonObject.put("submititLoc",submititLocation);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return jsonObject.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream openStream(URLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        if (false) { // flip for debug :)
            return inputStream;
        }
        String stream = toString(inputStream);
        System.out.println("***STRAN***");
        System.out.println(stream);
        return new ByteArrayInputStream(stream.getBytes());
    }

    public String assignRoom(String encodedTalk,String roomRef) {
        String talkUrl = Base64Util.decode(encodedTalk);
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
            JSONObject errorJson = new JSONObject();
            try {
                errorJson.put("error","Talk has been updated at " + lastModified + " not " + givenLastModified);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
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
        JSONArray allTalk = new JSONArray();
        for (Item item : items) {
            JSONObject jsonTalk = readItemProperties(item, null);
            List<Link> links = item.getLinks();
            JSONArray speakers = new JSONArray();
            for (Link link : links) {
             if (!"speaker item".equals(link.getRel())) {
                    continue;
                }
                JSONObject speaker = new JSONObject();
                try {
                    speaker.put("name",link.getPrompt().get().toString());
                    speakers.put(speaker);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                jsonTalk.put("speakers",speakers);
                allTalk.put(jsonTalk);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
        return allTalk.toString();
    }

    public String talksFullVersion(String encodedEvent) {
        List<Item> items = getAllTalksSummary(encodedEvent);
        // TODO There has to be a better way to do this
        JSONArray talkArray = new JSONArray();
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
            JSONObject jsonTalk = readTalk(talkIktem, talkConn);
            talkArray.put(jsonTalk);
        }

        return talkArray.toString();
    }

    private List<Item> getAllTalksSummary(String encodedEvent) {
        String url = Base64Util.decode(encodedEvent) + "/sessions";

        URLConnection connection = openConnection(url, true);
        Collection events;
        try {
            events = new CollectionParser().parse(connection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return events.getItems();
    }

    private JSONObject readTalk(Item item, URLConnection connection) {
        JSONObject jsonTalk = readItemProperties(item,connection);

        String speakerLink = item.linkByRel("speaker collection").get().getHref().toString();

        URLConnection speakerConnection = openConnection(speakerLink, true);

        Collection speakers;
        try (InputStream speakInpStream = speakerConnection.getInputStream()) {
            speakers = new CollectionParser().parse(speakInpStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONArray jsonSpeakers = new JSONArray();
        for (Item speaker : speakers.getItems()) {
            JSONObject jsonSpeaker = readItemProperties(speaker,speakerConnection);
            jsonSpeakers.put(jsonSpeaker);
        }

        try {
            jsonTalk.put("speakers", jsonSpeakers);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonTalk;
    }

    private JSONObject readItemProperties(Item item, URLConnection connection) {
        JSONObject itemAsJson = new JSONObject();
        Map<String,Property> dataAsMap = item.getData().getDataAsMap();
        for (Map.Entry<String,Property> propentry : dataAsMap.entrySet()) {
            String key = propentry.getKey();
            Property property = propentry.getValue();
            if (property.hasArray()) {
                List<Value> array = property.getArray();
                JSONArray values = new JSONArray();
                for (Value val : array) {
                    values.put(val.asString());
                }
                try {
                    itemAsJson.put(key, values);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            } else if (property.hasValue()) {
                try {
                    itemAsJson.put(key, property.getValue().get().asString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        String href = item.getHref().get().toString();
        try {
            itemAsJson.put("ref", Base64Util.encode(href));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String lastModified = (connection != null) ? connection.getHeaderField("last-modified") : null;
        if (lastModified != null) {
            try {
                itemAsJson.put("lastModified",lastModified);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
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
