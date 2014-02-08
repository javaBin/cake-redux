package no.javazone.cake.redux;

import net.hamnaberg.json.*;
import net.hamnaberg.json.parser.CollectionParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class EmsCommunicator {
    public static void main(String[] args) throws Exception {
        String eventText = "aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWY=";
       // String ev="http://test.java.no/ems-redux/server/events/cee37cc1-5399-47ef-9418-21f9b6444bfa/sessions/35efacf4-e9be-4980-9fb1-5baa83bb050f";
        Configuration.init(args[0]);
        //new EmsCommunicator().readContent(ev, true);
        System.out.println(new EmsCommunicator().talks(eventText));
        //System.out.println(new EmsCommunicator().allEvents());
    }


    public String allEvents()  {
        try {
            URLConnection connection = readContent("http://test.2014.javazone.no/ems/server/events", false);
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

    public String talks(String encodedEvent) {
        String url = Base64Util.decode(encodedEvent) + "/sessions";

        URLConnection connection = readContent(url, true);
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
            JSONObject jsonTalk = readItemProperties(item);

            String speakerLink = item.linkByRel("speaker collection").get().getHref().toString();

            URLConnection speakerConnection = readContent(speakerLink,true);

            Collection speakers;
            try {
                speakers = new CollectionParser().parse(speakerConnection.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JSONArray jsonSpeakers = new JSONArray();
            for (Item speaker : speakers.getItems()) {
                JSONObject jsonSpeaker = readItemProperties(speaker);
                jsonSpeakers.put(jsonSpeaker);
            }

            try {
                jsonTalk.put("speakers", jsonSpeakers);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            talkArray.put(jsonTalk);
        }

        return talkArray.toString();
    }

    private JSONObject readItemProperties(Item item) {
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
        return itemAsJson;
    }


    private static URLConnection readContent(String questionUrl,boolean useAuthorization)  {
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
