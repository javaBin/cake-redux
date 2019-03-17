package no.javazone.cake.redux;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class SlackServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String clientid=Configuration.slackAppId();
        String secret= Configuration.slackClientSecret();
        String channel = Configuration.slackAuthChannel();

        if (clientid == null || secret == null || channel == null || Configuration.slackApiToken() == null) {
            sendError(resp,"Server not setup for slack signin");
            return;
        }

        String code = req.getParameter("code");
        Optional<JsonObject> jsonObject = fetchFromSlack("https://slack.com/api/oauth.access?client_id=" + clientid + "&client_secret=" + secret + "&code=" + code);
        if (!jsonObject.isPresent()) {
            sendError(resp,"Could not get token from slack");
            return;
        }
        JsonObject loginObject = jsonObject.get();
        Optional<String> accessToken = loginObject.stringValue("access_token");

        if (!accessToken.isPresent()) {
            sendError(resp,"Could not read access token");
            return;
        }
        Optional<String> userid = loginObject.objectValue("user").orElse(new JsonObject()).stringValue("id");
        if (!userid.isPresent()) {
            sendError(resp,"Could not read userid from slack");
            return;
        }
        if (!SlackCheckAccess.hasAccess(userid.get())) {
            sendError(resp,"No access found");
            return;
        }
        String username = loginObject.objectValue("user").orElse(new JsonObject()).stringValue("name").orElse("UnkownName");
        req.getSession().setMaxInactiveInterval(-1); // Keep session open until browser closes (hopefully).
        req.getSession().setAttribute("access_token", accessToken.get());
        req.getSession().setAttribute("username",username);

        EntranceServlet.writeLoginMessage(resp, resp.getWriter(), username);
    }

    private void sendError(HttpServletResponse response,String error) throws IOException {
        StringBuilder builder=new StringBuilder();
        builder.append("<html><body><h1>Error</h1><p>");
        builder.append(error);
        builder.append("</p></body></html>");
        response.setContentType("text/html");
        response.getWriter().append(builder.toString());
    }

    public static Optional<JsonObject> fetchFromSlack(String urlAddr) throws IOException {
        URL url = new URL(urlAddr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int responsecoee = conn.getResponseCode();
        if (responsecoee >= 400) {
            return Optional.empty();
        }
        String respCont;
        try (InputStream is = conn.getInputStream()){
            respCont = CommunicatorHelper.toString(is);
        }
        JsonObject jsonObject = JsonParser.parseToObject(respCont);
        return Optional.of(jsonObject);
    }
}
