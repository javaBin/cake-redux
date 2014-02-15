package no.javazone.cake.redux;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DataServlet extends HttpServlet {
    private EmsCommunicator emsCommunicator;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = EmsCommunicator.toString(inputStream);
            JSONObject update = new JSONObject(inputStr);
            String ref = update.getString("ref");
            JSONArray tags = update.getJSONArray("tags");
            List<String> taglist = new ArrayList<>();
            for (int i=0;i<tags.length();i++) {
                taglist.add(tags.getString(i));
            }

            emsCommunicator.updateTags(ref,taglist);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/json");
        PrintWriter writer = response.getWriter();
        String pathInfo = request.getPathInfo();
        if ("/talks".equals(pathInfo)) {
            String encEvent = request.getParameter("eventId");
            writer.append(emsCommunicator.talkShortVersion(encEvent));
        } else if ("/atalk".equals(pathInfo)) {
            String encTalk = request.getParameter("talkId");
            writer.append(emsCommunicator.fetchOneTalk(encTalk));
        } else {
            writer.append(emsCommunicator.allEvents());
        }
    }


    @Override
    public void init() throws ServletException {
        emsCommunicator = new EmsCommunicator();
    }

    public void setEmsCommunicator(EmsCommunicator emsCommunicator) {
        this.emsCommunicator = emsCommunicator;
    }
}
