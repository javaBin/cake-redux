package no.javazone.cake.redux;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class SlackCheckAccess {
    private static SlackCheckAccess INSTANCE = new SlackCheckAccess();

    private SlackCheckAccess() {
    }

    private final ConcurrentMap<String,String> accesses = new ConcurrentHashMap<String,String>();
    private volatile LocalDateTime lastChecked = null;

    public static boolean hasAccess(String userid) {
        if (INSTANCE.accesses.containsKey(userid)) {
            return true;
        }
        return INSTANCE.readAndCheckAccess(userid);
    }

    private synchronized boolean readAndCheckAccess(String userid) {
        if (accesses.containsKey(userid)) {
            return true;
        }
        if (lastChecked != null && lastChecked.plusSeconds(60).isAfter(LocalDateTime.now())) {
            return false;
        }
        String urlstr = "https://slack.com/api/conversations.members?token=" + Configuration.slackApiToken() + "&channel=" + Configuration.slackAuthChannel();
        Optional<JsonObject> jsonObjectOpt;
        try {
            jsonObjectOpt = SlackServlet.fetchFromSlack(urlstr);
        } catch (IOException e) {
            return false;
        }
        if (!jsonObjectOpt.isPresent()) {
            return false;
        }
        JsonObject slackData = jsonObjectOpt.get();
        if (!slackData.booleanValue("ok").orElse(false)) {
            return false;
        }
        Map<String,String> members = slackData.arrayValue("members").orElse(new JsonArray()).stringStream().collect(Collectors.toMap(a->a, a->a));
        for (String key:accesses.keySet()) {
            if (!members.containsKey(key)) {
                accesses.remove(key);
            }
        }
        accesses.putAll(members);
        lastChecked = LocalDateTime.now();
        return accesses.containsKey(userid);

    }
}
