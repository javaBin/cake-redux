package no.javazone.cake.redux.util;

import no.javazone.cake.redux.Configuration;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jetbrains.annotations.NotNull;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SetRegisterlocOnWorkshops {
    private final SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    private final String conferenceid;

    public SetRegisterlocOnWorkshops(String conferenceid) {
        this.conferenceid = conferenceid;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage SetRegisterlocOnWorkshops <configfile> <conferenceid>");
            return;
        }
        Configuration.setConfigFile(args);
        new SetRegisterlocOnWorkshops(args[1]).doUpdates();
    }

    public void doUpdates() throws Exception {
        List<JsonObject> publishedWorkshops = readCakeWorkshops();
        List<JsonObject> mooseheadWorkshops = allMooseheadWorkshops();

        Map<String,String> mappings = new HashMap<>();

        for (JsonObject mooseheadWorkshop : mooseheadWorkshops) {
            String title = mooseheadWorkshop.requiredString("title");
            final Optional<JsonObject> first = publishedWorkshops.stream()
                    .filter(taobj -> title.equals(taobj.requiredObject("data").requiredObject("title").requiredString("value")))
                    .findFirst();
            if (!first.isPresent()) {
                System.out.println("Did not find workshop " + title);
                return;
            }
            String spid = first.get().requiredString("sessionId");
            final String current = mappings.get(spid);
            if (current != null) {
                System.out.println(String.format("Duplicate %s '%s' <-> '%s'",spid,current,title));
            }
            mappings.put(spid,mooseheadWorkshop.requiredString("id"));
        }

        System.out.println("Found matches " + mappings.size());

        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String talkref = entry.getKey();
            String registerloc = "https://moosehead.javazone.no/#/register/" + entry.getValue();
            System.out.println(talkref + "->" + registerloc);
            JsonObject payload = JsonFactory.jsonObject()
                    .put("registerLoc", JsonFactory.jsonObject().put("value", registerloc).put("privateData", false));
            sleepingpillCommunicator.sendTalkUpdate(talkref,JsonFactory.jsonObject().put("data",payload));
        }


    }

    private List<JsonObject> allMooseheadWorkshops() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://moosehead.javazone.no/data/workshopList").openConnection();
        try (InputStream is = connection.getInputStream()) {
            return JsonParser.parseToArray(is).objectStream().collect(Collectors.toList());
        }
    }

    @NotNull
    private List<JsonObject> readCakeWorkshops() {
        final JsonArray allTalks = sleepingpillCommunicator.allTalkFromConferenceSleepingPillFormat(conferenceid);
        List<JsonObject> publishedWorkshops = allTalks.objectStream()
                .filter(taobj -> ("APPROVED".equals(taobj.stringValue("status").orElse("-")) && "workshop".equals(taobj.objectValue("data").orElse(new JsonObject()).objectValue("format").orElse(new JsonObject()).stringValue("value").orElse("-"))))
                .collect(Collectors.toList());
        return publishedWorkshops;
    }


}
