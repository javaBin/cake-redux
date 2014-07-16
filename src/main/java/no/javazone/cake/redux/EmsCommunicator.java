package no.javazone.cake.redux;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.hamnaberg.funclite.Optional;
import net.hamnaberg.funclite.Predicate;
import net.hamnaberg.json.*;
import net.hamnaberg.json.data.JsonObjectFromData;
import net.hamnaberg.json.parser.CollectionParser;
import net.hamnaberg.json.util.PropertyFunctions;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmsCommunicator {
    private CollectionParser collectionParser = new CollectionParser();


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
            data = collectionParser.parse(inputStream).getFirstItem().get().getData();
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

        return fetchOneTalk(encodedTalkUrl);
    }

    private String confirmTalkMessage(String status, String message) {
        ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
        jsonObject.put("status",status);
        jsonObject.put("message",message);
        return jsonObject.toString();
    }

    public String confirmTalk(String encodedTalkUrl, String dinner) {
        ObjectNode jsonTalk = fetchOneTalkAsObjectNode(encodedTalkUrl);
        JsonNode tagsarr = jsonTalk.get("tags");
        List<String> tags = new ArrayList<>();
        for (JsonNode n : tagsarr) {
            tags.add(n.asText());
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

        return confirmTalkMessage("ok","ok");
    }

    public String allEvents()  {
        try {
            URLConnection connection = openConnection(Configuration.emsEventLocation(), false);
            Collection events = collectionParser.parse(connection.getInputStream());
            List<Item> items = events.getItems();
            ArrayNode eventArray = JsonNodeFactory.instance.arrayNode();
            for (Item item : items) {
                Data data = item.getData();

                String eventname = data.propertyByName("name").flatMap(PropertyFunctions.propertyToValueStringF).get();

                String slug = data.propertyByName("slug").flatMap(PropertyFunctions.propertyToValueStringF).get();
                String href = item.getHref().get().toString();

                href = Base64Util.encode(href);

                ObjectNode event = JsonNodeFactory.instance.objectNode();

                event.put("name",eventname);
                event.put("ref",href);
                event.put("slug",slug);

                eventArray.add(event);
            }
            return eventArray.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String fetchOneTalk(String encodedUrl) {
        return fetchOneTalkAsObjectNode(encodedUrl).toString();
    }

    public ObjectNode fetchOneTalkAsObjectNode(String encodedUrl) {
        String url = Base64Util.decode(encodedUrl);
        URLConnection connection = openConnection(url, true);

        try {
            InputStream is = openStream(connection);
            Item talkItem = collectionParser.parse(is).getFirstItem().get();
            ObjectNode jsonObject = readTalk(talkItem, connection);
            String submititLocation = Configuration.submititLocation() + encodedUrl;
            jsonObject.put("submititLoc", submititLocation);
            return jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            Collection parse = collectionParser.parse(inputStream);
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

        return fetchOneTalk(encodedTalkUrl);
    }

    public String talkShortVersion(String encodedEvent) {
        List<Item> items = getAllTalksSummary(encodedEvent);
        ArrayNode allTalk = JsonNodeFactory.instance.arrayNode();
        for (Item item : items) {
            ObjectNode jsonTalk = readItemProperties(item, null);
            List<Link> links = item.findLinks(new Predicate<Link>() {
                @Override
                public boolean apply(Link input) {
                    return "speaker item".equals(input.getRel());
                }
            });
            ArrayNode speakers = JsonNodeFactory.instance.arrayNode();
            for (Link link : links) {
                ObjectNode speaker = JsonNodeFactory.instance.objectNode();
                speaker.put("name", link.getPrompt().get());
                speakers.add(speaker);
            }
            jsonTalk.set("speakers", speakers);
            allTalk.add(jsonTalk);

        }
        return allTalk.toString();
    }

    public String talksFullVersion(String encodedEvent) {
        List<Item> items = getAllTalksSummary(encodedEvent);
        // TODO There has to be a better way to do this
        ArrayNode talkArray = JsonNodeFactory.instance.arrayNode();
        int num=items.size();
        for (Item item : items) {
            System.out.println(num--);
            URLConnection talkConn = openConnection(item.getHref().get().toString(), true);
            Item talkIktem;

            try (InputStream talkInpStr = talkConn.getInputStream()) {
                talkIktem = collectionParser.parse(talkInpStr).getFirstItem().get();
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
            events = collectionParser.parse(connection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return events.getItems();
    }

    private ObjectNode readTalk(Item item, URLConnection connection) {
        ObjectNode jsonTalk = readItemProperties(item,connection);

        String speakerLink = item.linkByRel("speaker collection").get().getHref().toString();

        URLConnection speakerConnection = openConnection(speakerLink, true);

        Collection speakers;
        try (InputStream speakInpStream = speakerConnection.getInputStream()) {
            speakers = collectionParser.parse(speakInpStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ArrayNode jsonSpeakers = JsonNodeFactory.instance.arrayNode();
        for (Item speaker : speakers.getItems()) {
            ObjectNode jsonSpeaker = readItemProperties(speaker, speakerConnection);
            jsonSpeakers.add(jsonSpeaker);
        }

        jsonTalk.set("speakers", jsonSpeakers);
        return jsonTalk;
    }

    private ObjectNode readItemProperties(Item item, URLConnection connection) {
        ObjectNode data = new JsonObjectFromData().apply(item.getData());

        String href = item.getHref().get().toString();
        data.put("ref", Base64Util.encode(href));

        String lastModified = (connection != null) ? connection.getHeaderField("last-modified") : null;
        if (lastModified != null) {
            data.put("lastModified",lastModified);
        }
        return data;
    }


    private static URLConnection openConnection(String questionUrl, boolean useAuthorization)  {
        try {
            final URL url = new URL(questionUrl);
            URLConnection urlConnection = url.openConnection();

            if (useAuthorization) {
                Authenticator.setDefault(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        String host = getRequestingHost();
                        int port = url.getPort();
                        int actualPort = port == -1 ? url.getDefaultPort() : port;
                        if (url.getHost().equals(host) && actualPort == getRequestingPort()) {
                            return new PasswordAuthentication(Configuration.getEmsUser(), Configuration.getEmsPassword().toCharArray());
                        }
                        return null;
                    }
                });
            }

            return urlConnection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectNode parse(InputStream inputStream) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(inputStream);
            if (jsonNode.isObject()) {
                return (ObjectNode) jsonNode;
            }
            return null;
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
