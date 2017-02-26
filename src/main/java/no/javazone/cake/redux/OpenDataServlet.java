package no.javazone.cake.redux;


import no.javazone.cake.redux.comments.Contact;
import no.javazone.cake.redux.comments.Feedback;
import no.javazone.cake.redux.comments.FeedbackDao;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class OpenDataServlet extends HttpServlet {
    private SleepingpillCommunicator sleepingpillCommunicator;

    @Override
    public void init() throws ServletException {
        sleepingpillCommunicator = new SleepingpillCommunicator();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/json");
        PrintWriter writer = resp.getWriter();
        JsonObject talkJson = sleepingpillCommunicator.oneTalkStripped(req.getParameter("talkId"));
        JsonObject talkInfo = shortTalkVersion(talkJson);

        talkInfo.toJson(writer);
    }

    private JsonObject shortTalkVersion(JsonObject jsonObject) {
        JsonObject talkInfo = JsonFactory.jsonObject();

        JsonArray tags = jsonObject.requiredArray("tags");
        String error = checkTags(tags);
        if (error != null) {
            talkInfo.put("status","error");
            talkInfo.put("message",error);
            return  talkInfo;
        }
        talkInfo.put("status","ok");
        JsonObject talkData = JsonFactory.jsonObject();
        talkData.put("title",jsonObject.requiredString("title"));
        talkData.put("tags",tags);
        talkInfo.put("talk",talkData);
        return talkInfo;
    }

    private String checkTags(JsonArray tags)  {
        boolean foundAccepted = false;
        for (String tag : tags.strings()) {
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

        String lastModified = sleepingpillCommunicator.oneTalkStripped(encodedTalkUrl).requiredString("lastModified");

        FeedbackDao feedbackDao = FeedbackDao.instance();
        feedbackDao.addFeedback(contact,lastModified);

        String status = sleepingpillCommunicator.confirmTalk(encodedTalkUrl,dinner,UserAccessType.OPENSERVLET);
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
