package no.javazone.cake.redux;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

public class EmsCopy {
    public static void main(String[] args) throws Exception {
        System.setProperty("cake-redux-config-file",args[0]);
        EmsCommunicator emsCommunicator = new EmsCommunicator();
        JsonArray nodes = JsonParser.parseToArray(emsCommunicator.talkShortVersion("aHR0cDovL2phdmF6b25lLm5vL2Vtcy9zZXJ2ZXIvZXZlbnRzLzNiYWEyNWQzLTljY2EtNDU5YS05MGQ3LTlmYzM0OTIwOTI4OQ=="));
        JsonObject all = JsonFactory.jsonObject();
        nodes.nodeStream().forEach(jn -> {
            JsonObject jo = (JsonObject) jn;
            String ref = jo.requiredString("ref");
            System.out.println("Fetching " + ref);
            JsonObject jsonObject = emsCommunicator.oneTalkAsJson(ref);
            all.put(ref,jsonObject);
        });
        JsonObject result = JsonFactory.jsonObject().put("all", nodes).put("individ", all);
        try (PrintWriter writer = new PrintWriter(new FileWriter(new File("/Users/anderskarlsen/Dropbox/javabin/backups/emscopy.txt")))) {
            result.toJson(writer);
        }

    }
}
