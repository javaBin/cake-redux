package no.javazone.cake.redux;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class SecurityFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (Configuration.noAuthMode()) {
            chain.doFilter(req,resp);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) req;
        String token = (String) request.getSession().getAttribute("access_token");
        if (token != null) {
            chain.doFilter(req,resp);
            return;
        }
        if (req.getParameter("error") != null) {
            resp.getWriter().println(req.getParameter("error"));
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

        request.getSession().setAttribute("access_token", accessToken);

        chain.doFilter(req, resp);

}

    private String para(String name,String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(name,"utf-8") + "=" + URLEncoder.encode(value,"UTF-8");
    }

    @Override
    public void destroy() {

    }
}
