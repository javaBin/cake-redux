package no.javazone.cake.redux;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
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

        StringBuilder urlAdr = new StringBuilder();
        urlAdr.append(para("code", code) + "&");
        urlAdr.append(para("client_id", Configuration.getGoogleClientId()) + "&");
        urlAdr.append(para("client_secret", Configuration.getGoogleClientSecret()) + "&");
        urlAdr.append(para("redirect_uri", Configuration.getGoogleRedirectUrl()) + "&");
        urlAdr.append(para("grant_type", "authorization_code"));
        System.out.println(urlAdr);
        URL url = new URL("https://accounts.google.com/o/oauth2/token");
        URLConnection urlConnection = url.openConnection();


        ((HttpURLConnection)urlConnection).setRequestMethod("POST");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length", "" + urlAdr.toString().length());

        // Create I/O streams
        DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());
        // Send request
        outStream.writeBytes(urlAdr.toString());
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

            // google tokens expire after an hour, but since we requested offline access we can get a new token without user involvement via the refresh token
            accessToken = (String) jsonObject.get("access_token");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // you may want to store the access token in session

        // get some info about the user with the access token
        String getStr = "https://www.googleapis.com/oauth2/v1/userinfo?" + para("access_token",accessToken);
        URLConnection inconn = new URL(getStr).openConnection();
        String json;
        try (InputStream is = inconn.getInputStream()) {
            json = EmsCommunicator.toString(is);
        }

        // now we could store the email address in session

        //chain.doFilter(req,resp);
        // return the json of the user's basic info
        resp.getWriter().println(json);

        request.getSession().setAttribute("access_token", accessToken);
        /*HttpServletRequest request = (HttpServletRequest) req;
        System.out.println("Hit filter " + request.getPathInfo());
        */
}

    private String para(String name,String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(name,"utf-8") + "=" + URLEncoder.encode(value,"UTF-8");
    }

    @Override
    public void destroy() {

    }
}
