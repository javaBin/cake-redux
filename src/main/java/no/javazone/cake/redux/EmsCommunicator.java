package no.javazone.cake.redux;

import net.hamnaberg.json.*;
import net.hamnaberg.json.parser.CollectionParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmsCommunicator {


    public String updateTags(String encodedTalkUrl,List<String> tags) {
        String talkUrl = Base64Util.decode(encodedTalkUrl);
        URLConnection connection = openConnection(talkUrl, true);
        String lastModified = connection.getHeaderField("last-modified");
        Data data;
        try (InputStream inputStream = connection.getInputStream()) {
            data = new CollectionParser().parse(inputStream).getFirstItem().get().getData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Property newVals = Property.arrayObject("tags", new ArrayList<Object>(tags));
        data = data.replace(newVals);
        Template template = Template.create(data.getDataAsMap().values());

        System.out.println(template.toString());

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
            return toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String allEvents()  {
        try {
            URLConnection connection = openConnection("http://test.2014.javazone.no/ems/server/events", false);
            Collection events = new CollectionParser().parse(connection.getInputStream());
            List<Item> items = events.getItems();
            JSONArray eventArray = new JSONArray();
            for (Item item : items) {
                Data data = item.getData();
                String eventname = data.propertyByName("name").get().getValue().get().asString();
                String href = item.getHref().get().toString();

                href = Base64Util.encode(href);

                JSONObject event = new JSONObject();

                event.put("name",eventname);
                event.put("ref",href);

                eventArray.put(event);
            }
            return eventArray.toString();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String fetchOneTalk(String encodedUrl) {
        String url = Base64Util.decode(encodedUrl);
        URLConnection connection = openConnection(url, true);

        try {
            Item talkItem = new CollectionParser().parse(connection.getInputStream()).getFirstItem().get();
            return readTalk(talkItem,connection).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String talks(String encodedEvent) {
        String url = Base64Util.decode(encodedEvent) + "/sessions";

        URLConnection connection = openConnection(url, true);
        Collection events;
        try {
            events = new CollectionParser().parse(connection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Item> items = events.getItems();
        // TODO There has to be a better way to do this
        JSONArray talkArray = new JSONArray();
        for (Item item : items) {
            URLConnection talkConn = openConnection(item.getHref().get().toString(), true);
            Item talkIktem = null;
            try (InputStream talkInpStr = talkConn.getInputStream()) {
                talkIktem = new CollectionParser().parse(talkInpStr).getFirstItem().get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JSONObject jsonTalk = readTalk(talkIktem,talkConn);
            talkArray.put(jsonTalk);
        }

        return talkArray.toString();
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

            } else {
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
        String lastModified = connection.getHeaderField("last-modified");
        if (lastModified != null) {
            try {
                itemAsJson.put("last-modified",lastModified);
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
}
