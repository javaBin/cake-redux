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
    private AcceptorSetter acceptorSetter;

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
        }

    }

    private void assignRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = EmsCommunicator.toString(inputStream);
            JSONObject update = new JSONObject(inputStr);
            String ref = update.getString("talkRef");
            String roomRef = update.getString("roomRef");

            String lastModified = update.getString("lastModified");

            String newTalk = emsCommunicator.assignRoom(ref,roomRef,lastModified);
            resp.getWriter().append(newTalk);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void assignSlot(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = EmsCommunicator.toString(inputStream);
            JSONObject update = new JSONObject(inputStr);
            String ref = update.getString("talkRef");
            String slotRef = update.getString("slotRef");

            String lastModified = update.getString("lastModified");

            String newTalk = emsCommunicator.assignSlot(ref,slotRef,lastModified);
            resp.getWriter().append(newTalk);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    private void publishTalk(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = EmsCommunicator.toString(inputStream);
            JSONObject update = new JSONObject(inputStr);
            String ref = update.getString("ref");

            String lastModified = update.getString("lastModified");

            String newTalk = emsCommunicator.publishTalk(ref,lastModified);
            resp.getWriter().append(newTalk);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void acceptTalks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = EmsCommunicator.toString(inputStream);
            try {
                JSONObject jsonObject = new JSONObject(inputStr);
                JSONArray talks = jsonObject.getJSONArray("talks");
                String statusJson = acceptorSetter.accept(talks);
                resp.getWriter().append(statusJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void massUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = EmsCommunicator.toString(inputStream);
            try {
                JSONObject jsonObject = new JSONObject(inputStr);

                String statusJson = acceptorSetter.massUpdate(jsonObject);
                resp.getWriter().append(statusJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void updateTalk(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream inputStream = req.getInputStream()) {
            String inputStr = EmsCommunicator.toString(inputStream);
            JSONObject update = new JSONObject(inputStr);
            String ref = update.getString("ref");
            JSONArray tags = update.getJSONArray("tags");
            String state = update.getString("state");
            String lastModified = update.getString("lastModified");
            List<String> taglist = new ArrayList<>();
            for (int i=0;i<tags.length();i++) {
                taglist.add(tags.getString(i));
            }

            String newTalk = emsCommunicator.update(ref, taglist, state,lastModified);
            resp.getWriter().append(newTalk);
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
        } else if ("/events".equals(pathInfo)) {
            writer.append(emsCommunicator.allEvents());
        } else if ("/roomsSlots".equals(pathInfo)) {
            String encEvent = request.getParameter("eventId");
            writer.append(emsCommunicator.allRoomsAndSlots(encEvent));
        }
    }

    private String config() {
        JSONObject conf = new JSONObject();
        try {
            conf.put("submititloc",Configuration.submititLocation());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return conf.toString();
    }


    @Override
    public void init() throws ServletException {
        emsCommunicator = new EmsCommunicator();
        acceptorSetter = new AcceptorSetter(emsCommunicator);
    }

    public void setEmsCommunicator(EmsCommunicator emsCommunicator) {
        this.emsCommunicator = emsCommunicator;
    }
}
