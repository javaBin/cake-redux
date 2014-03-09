package no.javazone.cake.redux;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class EntranceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        if (req.getParameter("error") != null) {
            writer.println(req.getParameter("error"));
            return;
        }

        String code = req.getParameter("code");

        StringBuilder postParameters = new StringBuilder();
        postParameters.append(para("code", code) + "&");
        postParameters.append(para("client_id", Configuration.getGoogleClientId()) + "&");
        postParameters.append(para("client_secret", Configuration.getGoogleClientSecret()) + "&");
        postParameters.append(para("redirect_uri", Configuration.getGoogleRedirectUrl()) + "&");
        postParameters.append(para("grant_type", "authorization_code"));
        URL url = new URL("https://accounts.google.com/o/oauth2/token");
        URLConnection urlConnection = url.openConnection();


        ((HttpURLConnection)urlConnection).setRequestMethod("POST");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length", "" + postParameters.toString().length());

        // Create I/O streams
        DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());
        // Send request
        outStream.writeBytes(postParameters.toString());
        outStream.flush();
        outStream.close();

        String googleresp;
        try (InputStream inputStream = urlConnection.getInputStream()) {
            googleresp = EmsCommunicator.toString(inputStream);
        }

        JSONObject jsonObject = null;
        String accessToken;
        try {
            jsonObject = new JSONObject(googleresp);
            // get the access token from json and request info from Google
            accessToken = (String) jsonObject.get("access_token");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // get some info about the user with the access token
        String getStr = "https://www.googleapis.com/oauth2/v1/userinfo?" + para("access_token",accessToken);
        URLConnection inconn = new URL(getStr).openConnection();
        String json;
        try (InputStream is = inconn.getInputStream()) {
            json = EmsCommunicator.toString(is);
        }


        String username = null;
        String userEmail = null;
        try {
            JSONObject userInfo = new JSONObject(json);
            username = userInfo.getString("name");
            userEmail = userInfo.getString("email");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        String userid = username + "<" + userEmail + ">";
        if (!Configuration.getAutorizedUsers().contains(userid)) {
            HttpServletResponse response = (HttpServletResponse) resp;
            ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_FORBIDDEN,"User not registered " + userid);
            return;
        }

        req.getSession().setAttribute("access_token", accessToken);

        resp.setContentType("text/html");
        writer.append("<html><body>");
        writer.append("<p>You are now logged in as " + userid + "</p>");
        writer.append("<p><a href='secured/#/'>To cake</a></p>");
        writer.append("</body></html>");
    }

    private String para(String name,String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(name, "utf-8") + "=" + URLEncoder.encode(value,"UTF-8");
    }

}
