package no.javazone.cake.redux;

import net.hamnaberg.funclite.Optional;
import net.hamnaberg.json.Collection;
import net.hamnaberg.json.Data;
import net.hamnaberg.json.Item;
import net.hamnaberg.json.Property;
import net.hamnaberg.json.parser.CollectionParser;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.List;

public class EmsCommunicator {
    public static void main(String[] args) throws Exception {
        System.out.println(allEvents());
    }


    public static String allEvents() throws Exception {
        String eventStr = readContent("http://test.2014.javazone.no/ems/server/events");
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
    }



    private static String readContent(String questionUrl) throws IOException {
        return readUrl(new URL(questionUrl));
    }

    private static String readUrl(URL url) throws IOException {
        return toString(url.openConnection().getInputStream());
    }

    private static String toString(InputStream inputStream) throws IOException {
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
