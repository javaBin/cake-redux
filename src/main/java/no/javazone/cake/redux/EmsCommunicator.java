package no.javazone.cake.redux;

import net.hamnaberg.json.*;
import net.hamnaberg.json.parser.CollectionParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class EmsCommunicator {
    public static void main(String[] args) throws Exception {
        String eventText = "aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWY=";
        Configuration.init(args[0]);
        System.out.println(new EmsCommunicator().talks(eventText));
        //System.out.println(new EmsCommunicator().allEvents());
    }


    public String allEvents()  {
        String eventStr = null;
        try {
            eventStr = readContent("http://test.2014.javazone.no/ems/server/events",false);
            Collection events = new CollectionParser().parse(new StringReader(eventStr));
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

        String sessionJson = readContent(url, true);
        Collection events;
        try {
            events = new CollectionParser().parse(new StringReader(sessionJson));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Item> items = events.getItems();
        // TODO There has to be a better way to do this
        JSONArray talkArray = new JSONArray();
        for (Item item : items) {
            Map<String,Property> dataAsMap = item.getData().getDataAsMap();
            JSONObject jsonTalk = new JSONObject();

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
                        jsonTalk.put(key,values);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    try {
                        jsonTalk.put(key, property.getValue().get().asString());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            talkArray.put(jsonTalk);
        }

        return talkArray.toString();
    }



    private static String readContent(String questionUrl,boolean useAuthorization)  {
        try {
            URL url = new URL(questionUrl);
            URLConnection urlConnection = url.openConnection();

            if (useAuthorization) {
                String authString = Configuration.getEmsUser() + ":" + Configuration.getEmsPassword();
                String authStringEnc = Base64Util.encode(authString);
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            }

            return toString(urlConnection.getInputStream());
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
