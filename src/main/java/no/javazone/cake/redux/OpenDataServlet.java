package no.javazone.cake.redux;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class OpenDataServlet extends HttpServlet {
    private EmsCommunicator emsCommunicator;

    @Override
    public void init() throws ServletException {
        emsCommunicator = new EmsCommunicator();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter writer = resp.getWriter();
        ObjectNode talkJson = emsCommunicator.fetchOneTalkAsObjectNode(req.getParameter("talkId"));
        ObjectNode talkInfo = shortTalkVersion(talkJson);

        writer.append(talkInfo.toString());
    }

    private ObjectNode shortTalkVersion(ObjectNode node) {
        ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
        JsonNode tags = node.get("tags");
        String error = checkTags(tags);
        if (error != null) {
            jsonObject.put("status", "error");
            jsonObject.put("message", error);
        }
        else {
            jsonObject.put("status", "ok");
            ObjectNode talkData = JsonNodeFactory.instance.objectNode();
            talkData.set("title", jsonObject.get("title"));
            talkData.set("tags", tags);
            talkData.set("lastModified", jsonObject.get("lastModified"));
            jsonObject.set("talk", talkData);
        }
        return jsonObject;
    }

    private String checkTags(JsonNode tags) {
        boolean foundAccepted = false;
        for (JsonNode tag : tags) {
            if ("confirmed".equals(tag.asText())) {
                return "Talk is already confirmed";
            }
            if ("accepted".equals(tag.asText())) {
                foundAccepted=true;
                break;
            }
        }
        if (foundAccepted) {
            return null;
        }
        return "Talk is not accepted";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (InputStream is = req.getInputStream()) {
            ObjectNode input = EmsCommunicator.parse(is);
            String encodedTalkUrl = input.get("id").asText();
            String dinner = input.get("dinner").asText();
            String status = emsCommunicator.confirmTalk(encodedTalkUrl,dinner);
            resp.setContentType("application/json");
            resp.getWriter().append(status);
        }
    }
}
