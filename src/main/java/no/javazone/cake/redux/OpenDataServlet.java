package no.javazone.cake.redux;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.javazone.cake.redux.comments.Contact;
import no.javazone.cake.redux.comments.Feedback;
import no.javazone.cake.redux.comments.FeedbackDao;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class OpenDataServlet extends HttpServlet {
    private EmsCommunicator emsCommunicator;

    @Override
    public void init() throws ServletException {
        emsCommunicator = new EmsCommunicator();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/json");
        PrintWriter writer = resp.getWriter();
        String talkJson = emsCommunicator.fetchOneTalk(req.getParameter("talkId"));
        ObjectNode talkInfo = shortTalkVersion(talkJson);

        writer.append(talkInfo.toString());
    }

    private ObjectNode shortTalkVersion(String talkJson) {
        ObjectNode talkInfo = JsonNodeFactory.instance.objectNode();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonObject = (ObjectNode) objectMapper.readTree(talkJson);
            ArrayNode tags = (ArrayNode) jsonObject.get("tags");
            String error = checkTags(tags);
            if (error != null) {
                jsonObject.put("status","error");
                jsonObject.put("message",error);
                return  jsonObject;
            }
            jsonObject.put("status","ok");
            ObjectNode talkData = JsonNodeFactory.instance.objectNode();
            talkData.put("title",jsonObject.get("title").asText());
            talkData.put("tags",tags);
            talkData.put("lastModified",jsonObject.get("lastModified").asText());
            jsonObject.put("talk",talkData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return talkInfo;
    }

    private String checkTags(ArrayNode tags)  {
        boolean foundAccepted = false;
        for (int i=0;i<tags.size();i++) {
            String tag = tags.get(i).asText();
            if ("confirmed".equals(tag)) {
                return "Talk is already confirmed";
            }
            if ("accepted".equals(tag)) {
                foundAccepted=true;
            }
        }
        if (foundAccepted) {
            return null;
        }
        return "Talk is not accepted";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String input = CommunicatorHelper.toString(req.getInputStream());
        JsonObject jsonObject = JsonParser.parseToObject(input);

        String encodedTalkUrl = jsonObject.requiredString("id");
        String dinner = jsonObject.requiredString("dinner");
        String contactPhone = removeIllegalChars(jsonObject.stringValue("contactPhone").filter(te -> !te.trim().isEmpty()).orElse("Unknown"));



        Feedback contact = Contact.builder()
                .setContactPhone(contactPhone)
                .setTalkid(encodedTalkUrl)
                .setAuthor("SpeakerFromSystem")
                .create();

        FeedbackDao feedbackDao = FeedbackDao.instance();
        feedbackDao.addFeedback(contact);

        String status = emsCommunicator.confirmTalk(encodedTalkUrl,dinner,UserAccessType.OPENSERVLET);
        resp.setContentType("text/json");
        resp.getWriter().append(status);
    }

    private String removeIllegalChars(String text) {
        StringBuilder res = new StringBuilder(text);
        for (int i=0;i<res.length();i++) {
            if (Character.isLetterOrDigit(res.charAt(i))) {
                continue;
            }
            res.replace(i,i+1," ");
        }
        return res.toString();
    }
}
