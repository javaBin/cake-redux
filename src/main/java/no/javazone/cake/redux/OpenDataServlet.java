package no.javazone.cake.redux;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        JSONObject talkInfo = shortTalkVersion(talkJson);

        writer.append(talkInfo.toString());
    }

    private JSONObject shortTalkVersion(String talkJson) {
        JSONObject talkInfo = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(talkJson);
            JSONArray tags = jsonObject.getJSONArray("tags");
            String error = checkTags(tags);
            if (error != null) {
                jsonObject.put("status","error");
                jsonObject.put("message",error);
                return  jsonObject;
            }
            jsonObject.put("status","ok");
            JSONObject talkData = new JSONObject();
            talkData.put("title",jsonObject.getString("title"));
            talkData.put("tags",tags);
            talkData.put("lastModified",jsonObject.getString("lastModified"));
            jsonObject.put("talk",talkData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return talkInfo;
    }

    private String checkTags(JSONArray tags) throws JSONException {
        boolean foundAccepted = false;
        for (int i=0;i<tags.length();i++) {
            String tag = tags.getString(i);
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
        String input = EmsCommunicator.toString(req.getInputStream());
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(input);
            String encodedTalkUrl = jsonObject.getString("id");
            String dinner = jsonObject.getString("dinner");
            String status = emsCommunicator.confirmTalk(encodedTalkUrl,dinner);
            resp.setContentType("text/json");
            resp.getWriter().append(status);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
