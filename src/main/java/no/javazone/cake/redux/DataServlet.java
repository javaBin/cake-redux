package no.javazone.cake.redux;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.javazone.cake.redux.comments.FeedbackService;
import no.javazone.cake.redux.schedule.MoveDirection;
import no.javazone.cake.redux.schedule.TalkSceduleDao;
import no.javazone.cake.redux.schedule.TalkScheduleGrid;
import no.javazone.cake.redux.schedule.TalkScheduleService;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;
import org.jsonbuddy.pojo.JsonGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataServlet extends HttpServlet {
    private EmsCommunicator emsCommunicator;
    private AcceptorSetter acceptorSetter;
    private UserFeedbackCommunicator userFeedbackCommunicator;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/editTalk".equals(pathInfo)) {
            updateTalk(req, resp);
        } else if ("/publishTalk".equals(pathInfo)) {
            publishTalk(req, resp);
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
        } else if ("/computeSchedule".equals(pathInfo)) {
            computeSchedule(req, resp);
            resp.setContentType("application/json;charset=UTF-8");
        } else if ("/scheduleUpdate".equals(pathInfo)) {
            scheduleUpdate(req, resp);
            resp.setContentType("application/json;charset=UTF-8");

        }

    }

    private void scheduleUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject = JsonParser.parseToObject(req.getInputStream());
        List<String> talkReferences = jsonObject.requiredArray("talkReferences").stringStream().collect(Collectors.toList());
        String talkRef = jsonObject.requiredString("talkRef");
        MoveDirection moveDirection = MoveDirection.valueOf(jsonObject.requiredString("moveDirection"));
        TalkScheduleGrid talkScheduleGrid = TalkScheduleService.get().move(talkReferences, Collections.emptyList(), Collections.emptyList(), talkRef, moveDirection);
        org.jsonbuddy.JsonNode result = JsonGenerator.generate(talkScheduleGrid);
        result.toJson(resp.getWriter());
    }

    private void computeSchedule(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonObject = JsonParser.parseToObject(req.getInputStream());
        List<String> talkReferences = jsonObject.requiredArray("talkReferences").stringStream().collect(Collectors.toList());
        List<String> wantedRooms = jsonObject.requiredArray("wantedRooms").stringStream().collect(Collectors.toList());
        List<String> wantedSlots = jsonObject.requiredArray("wantedSlots").stringStream().collect(Collectors.toList());
        TalkScheduleGrid talkScheduleGrid = TalkScheduleService.get().makeGrid(talkReferences, wantedRooms, Collections.emptyList());
        org.jsonbuddy.JsonNode result = JsonGenerator.generate(talkScheduleGrid);
        result.toJson(resp.getWriter());
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
            approveTalk(ref);
            publishTheTalk(ref);
        }
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("status","ok");
        resp.setContentType("text/json");
        resp.getWriter().append(objectNode.toString());
    }

    private void approveTalk(String ref) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonTalk = objectMapper.readTree(emsCommunicator.fetchOneTalk(ref));
        List<String> tags = AcceptorSetter.toCollection((ArrayNode) jsonTalk.get("tags"));
        String lastModified = jsonTalk.get("lastModified").asText();
        emsCommunicator.update(ref,tags,"approved",lastModified);
    }

    private void publishTheTalk(String ref) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonTalk = objectMapper.readTree(emsCommunicator.fetchOneTalk(ref));
        String lastModified = jsonTalk.get("lastModified").asText();
        emsCommunicator.publishTalk(ref,lastModified);
    }


    private void assignRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = CommunicatorHelper.toString(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode update = objectMapper.readTree(inputStr);
            String ref = update.get("talkRef").asText();
            String roomRef = update.get("roomRef").asText();

            String lastModified = update.get("lastModified").asText();

            String newTalk = emsCommunicator.assignRoom(ref,roomRef,lastModified);
            resp.getWriter().append(newTalk);
        }

    }

    private void assignSlot(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = CommunicatorHelper.toString(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode update = objectMapper.readTree(inputStr);

            String ref = update.get("talkRef").asText();
            String slotRef = update.get("slotRef").asText();

            String lastModified = update.get("lastModified").asText();

            String newTalk = emsCommunicator.assignSlot(ref, slotRef, lastModified);
            resp.getWriter().append(newTalk);
        }

    }


    private void publishTalk(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            org.jsonbuddy.JsonNode update = JsonParser.parse(inputStream);


            String ref = update.requiredString("ref");

            String lastModified = update.requiredString("lastModified");

            String newTalk = emsCommunicator.publishTalk(ref, lastModified);
            resp.getWriter().append(newTalk);
        }

    }

    private void acceptTalks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = CommunicatorHelper.toString(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonObject = objectMapper.readTree(inputStr);

            ArrayNode talks = (ArrayNode) jsonObject.get("talks");
            String statusJson = acceptorSetter.accept(talks);
            resp.getWriter().append(statusJson);
        }
    }

    private void massUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = CommunicatorHelper.toString(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonObject = objectMapper.readTree(inputStr);
            String statusJson = acceptorSetter.massUpdate((ObjectNode) jsonObject);
            resp.getWriter().append(statusJson);

        }
    }
    private void updateTalk(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            JsonObject update = (JsonObject) JsonParser.parse(inputStream);

            String ref = update.requiredString("ref");
            JsonArray tags = (JsonArray) update.value("tags").orElse(JsonFactory.jsonArray());
            String state = update.requiredString("state");
            String lastModified = update.requiredString("lastModified");

            List<String> taglist = tags.nodeStream().map(org.jsonbuddy.JsonNode::stringValue).collect(Collectors.toList());

            String newTalk = emsCommunicator.update(ref, taglist, state, lastModified);
            resp.getWriter().append(newTalk);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        String pathInfo = request.getPathInfo();
        if ("/talks".equals(pathInfo)) {
            String encEvent = request.getParameter("eventId");
            writer.append(emsCommunicator.talkShortVersion(encEvent));
        } else if ("/atalk".equals(pathInfo)) {
            String encTalk = request.getParameter("talkId");
            JsonObject oneTalkAsJson = emsCommunicator.oneTalkAsJson(encTalk);
            appendFeedbacks(oneTalkAsJson,encTalk);
            appendUserFeedback(oneTalkAsJson, userFeedbackCommunicator.feedback(encTalk));
            oneTalkAsJson.toJson(writer);
        } else if ("/events".equals(pathInfo)) {
            writer.append(emsCommunicator.allEvents());
        } else if ("/roomsSlots".equals(pathInfo)) {
            String encEvent = request.getParameter("eventId");
            writer.append(emsCommunicator.allRoomsAndSlots(encEvent));
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
    }

    private String config() {
        ObjectNode conf = JsonNodeFactory.instance.objectNode();
        conf.put("submititloc",Configuration.submititLocation());
        return conf.toString();
    }


    @Override
    public void init() throws ServletException {
        emsCommunicator = EmsCommunicator.get();
        userFeedbackCommunicator = new UserFeedbackCommunicator();
        acceptorSetter = new AcceptorSetter(emsCommunicator);

    }

    public void setEmsCommunicator(EmsCommunicator emsCommunicator) {
        this.emsCommunicator = emsCommunicator;
    }

    public void setUserFeedbackCommunicator(UserFeedbackCommunicator userFeedbackCommunicator) {
        this.userFeedbackCommunicator = userFeedbackCommunicator;
    }

}
