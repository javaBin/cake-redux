package no.javazone.cake.redux;

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
        writer.append(emsCommunicator.fetchOneTalk(req.getParameter("talkId")));
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
