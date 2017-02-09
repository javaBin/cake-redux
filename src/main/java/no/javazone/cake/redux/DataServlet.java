package no.javazone.cake.redux;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.javazone.cake.redux.comments.FeedbackService;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataServlet extends HttpServlet {
    private SleepingpillCommunicator sleepingpillCommunicator;
    private AcceptorSetter acceptorSetter;
    private UserFeedbackCommunicator userFeedbackCommunicator;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/editTalk".equals(pathInfo)) {
            updateTalk(req, resp);
        } else if ("/publishTalk".equals(pathInfo)) {
            throw new NotImplementedException();
        } else if ("/acceptTalks".equals(pathInfo)) {
            acceptTalks(req,resp);
        } else if ("/massUpdate".equals(pathInfo)) {
            massUpdate(req, resp);
        } else if ("/assignRoom".equals(pathInfo)) {
            assignRoom(req,resp);
        } else if ("/assignSlot".equals(pathInfo)) {
            assignSlot(req,resp);
        } else if ("/massPublish".equals(pathInfo)) {
            massPublish(req, resp);
        } else if ("/addComment".equals(pathInfo)) {
            addComment(req, resp);
            resp.setContentType("application/json;charset=UTF-8");
        } else if ("/giveRating".equals(pathInfo)) {
            giveRating(req, resp);
            resp.setContentType("application/json;charset=UTF-8");
        }

    }

    private void giveRating(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonArray ratings = FeedbackService.get().giveRating(JsonParser.parseToObject(req.getInputStream()), req.getSession().getAttribute("username").toString());
        ratings.toJson(resp.getWriter());
    }

    private void addComment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonArray comments = FeedbackService.get().addComment(JsonParser.parseToObject(req.getInputStream()), req.getSession().getAttribute("username").toString());
        comments.toJson(resp.getWriter());
    }

    private void massPublish(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = CommunicatorHelper.toString(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode update = objectMapper.readTree(inputStr);
            String ref = update.get("ref").asText();
            approveTalk(ref,computeAccessType(req));
        }
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("status","ok");
        resp.setContentType("text/json");
        resp.getWriter().append(objectNode.toString());
    }

    private void approveTalk(String ref,UserAccessType userAccessType) throws IOException {
        sleepingpillCommunicator.approveTalk(ref,userAccessType);
    }



    private void assignRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        throw new NotImplementedException();
        /*
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = CommunicatorHelper.toString(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode update = objectMapper.readTree(inputStr);
            String ref = update.get("talkRef").asText();
            String roomRef = update.get("roomRef").asText();

            String lastModified = update.get("lastModified").asText();

            //String newTalk = xemsCommunicator.assignRoom(ref,roomRef,lastModified,computeAccessType(req));
            //resp.getWriter().append(newTalk);
        }*/

    }

    private void assignSlot(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        throw new NotImplementedException();
        /*
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = CommunicatorHelper.toString(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode update = objectMapper.readTree(inputStr);

            String ref = update.get("talkRef").asText();
            String slotRef = update.get("slotRef").asText();

            String lastModified = update.get("lastModified").asText();

            String newTalk = emsCommunicator.assignSlot(ref, slotRef, lastModified,computeAccessType(req));
            resp.getWriter().append(newTalk);
        }*/

    }




    private static UserAccessType computeAccessType(HttpServletRequest request) {
        return UserAccessType.READ_ONLY;
    }

    private void acceptTalks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            JsonObject obj = JsonParser.parseToObject(inputStream);
            JsonArray talks = obj.requiredArray("talks");
            String inputStr = CommunicatorHelper.toString(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonObject = objectMapper.readTree(inputStr);

            //ArrayNode talks = (ArrayNode) jsonObject.get("talks");
            String statusJson = acceptorSetter.accept(talks,computeAccessType(req));
            resp.getWriter().append(statusJson);
        }
    }

    private void massUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            JsonObject input = JsonParser.parseToObject(inputStream);
            String statusJson = acceptorSetter.massUpdate(input,computeAccessType(req));
            resp.getWriter().append(statusJson);

        }
    }
    private void updateTalk(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject update;
        try (InputStream inputStream = req.getInputStream()) {
            update = JsonParser.parseToObject(inputStream);
        }

        String ref = update.requiredString("ref");
        JsonArray tags = (JsonArray) update.value("tags").orElse(JsonFactory.jsonArray());
        String state = update.requiredString("state");
        String lastModified = update.stringValue("lastModified").orElse("xx");

        List<String> taglist = tags.nodeStream().map(org.jsonbuddy.JsonNode::stringValue).collect(Collectors.toList());

        //String newTalk = emsCommunicator.update(ref, taglist, state, lastModified,computeAccessType(req));
        String newTalk = sleepingpillCommunicator.update(ref, taglist, state, lastModified,computeAccessType(req));
        resp.getWriter().append(newTalk);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        String pathInfo = request.getPathInfo();
        if ("/talks".equals(pathInfo)) {
            String encEvent = request.getParameter("eventId");
            writer.append(sleepingpillCommunicator.talkShortVersion(encEvent));
        } else if ("/atalk".equals(pathInfo)) {
            String encTalk = request.getParameter("talkId");
            JsonObject oneTalkAsJson = sleepingpillCommunicator.oneTalkAsJson(encTalk);
            appendFeedbacks(oneTalkAsJson,encTalk);
            // TODO Fix feedbacks
            //appendUserFeedback(oneTalkAsJson, userFeedbackCommunicator.feedback(encTalk));
            appendUserFeedback(oneTalkAsJson, null);
            oneTalkAsJson.toJson(writer);
        } else if ("/events".equals(pathInfo)) {
            writer.append(sleepingpillCommunicator.allEvents());
        } else if ("/roomsSlots".equals(pathInfo)) {
            String encEvent = request.getParameter("eventId");
            JsonFactory.jsonObject()
                    .put("rooms",JsonFactory.jsonArray())
                    .put("slots",JsonFactory.jsonArray())
                    .toJson(writer);
            //writer.append(emsCommunicator.allRoomsAndSlots(encEvent));
        }
    }

    private void appendUserFeedback(JsonObject oneTalkAsJson, String feedback) {
        if (feedback != null) {
            JsonObject feedbackAsJson = JsonParser.parseToObject(feedback);
            oneTalkAsJson.put("userFeedback", feedbackAsJson);
        } else {
            oneTalkAsJson.put("userFeedback", new JsonNull());
        }
    }

    private void appendFeedbacks(JsonObject oneTalkAsJson, String encTalk) {
        JsonArray comments = FeedbackService.get().commentsForTalk(encTalk);
        oneTalkAsJson.put("comments",comments);
        JsonArray ratings = FeedbackService.get().ratingsForTalk(encTalk);
        oneTalkAsJson.put("ratings",ratings);
        Optional<String> contact = FeedbackService.get().contactForTalk(encTalk);
        oneTalkAsJson.put("contactPhone",contact.orElse("Unknown"));
    }

    private String config() {
        ObjectNode conf = JsonNodeFactory.instance.objectNode();
        conf.put("submititloc",Configuration.submititLocation());
        return conf.toString();
    }


    @Override
    public void init() throws ServletException {
        sleepingpillCommunicator = new SleepingpillCommunicator();
        userFeedbackCommunicator = new UserFeedbackCommunicator();
        acceptorSetter = new AcceptorSetter(sleepingpillCommunicator);
    }

    public void setEmsCommunicator(EmsCommunicator emsCommunicator) {

    }

    public void setUserFeedbackCommunicator(UserFeedbackCommunicator userFeedbackCommunicator) {
        this.userFeedbackCommunicator = userFeedbackCommunicator;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (NoUserAceessException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,"User do not have write access");
        }
    }

    public DataServlet setSleepingpillCommunicator(SleepingpillCommunicator sleepingpillCommunicator) {
        this.sleepingpillCommunicator = sleepingpillCommunicator;
        return this;
    }
}
