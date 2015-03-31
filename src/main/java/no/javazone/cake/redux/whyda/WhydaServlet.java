package no.javazone.cake.redux.whyda;

import no.javazone.cake.redux.Configuration;
import no.javazone.cake.redux.EntranceServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class WhydaServlet extends HttpServlet {
    private static final String APP_AUTH = "applicationcredential=<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n" +
            " <applicationcredential>\n" +
            "    <params>\n" +
            "        <applicationID>#appid#</applicationID>\n" +
            "        <applicationSecret>#appsecret#</applicationSecret>\n" +
            "    </params> \n" +
            "</applicationcredential>\n";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!Configuration.whydaSupported()) {
            resp.getWriter().append("Whyda login is not supported here");
            return;
        }
        String token = (String) req.getSession().getAttribute("access_token");
        PrintWriter writer = resp.getWriter();
        if (token != null) {
            // already logged in
            EntranceServlet.writeLoginMessage(resp, writer,token);
            return;
        }

        String apptokenXml = readAppToken();
        String apptokenid = readAppTokenFromXml(apptokenXml);

        String userticket = req.getParameter("userticket");
        if (userticket == null || userticket.length() < 3) {
            // Need to login - redirecting to whydal
            resp.sendRedirect(Configuration.logonRedirectUrl());
            return;
        }
        // Reading user info
        String usertoken = readUserToken(userticket,apptokenXml,apptokenid);

        UserInfo userInfo = UserXmlUtil.read(usertoken, Configuration.applicationId());
        if (userInfo == null) {
            // This user is not authorized
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // Puting login on session
        String userid = userInfo.firstname + " " + userInfo.lastname + "<" + userInfo.email + ">";
        req.getSession().setAttribute("access_token", userid);
        EntranceServlet.writeLoginMessage(resp, writer, userid);
    }

    private String readUserToken(String userticket, String apptokenXml, String apptokenid) throws IOException {

        String path = Configuration.tokenServiceUrl() + "/user/" + apptokenid + "/get_usertoken_by_userticket";
        StringBuilder payload = new StringBuilder();
        payload.append("userticket=");
        payload.append(userticket);
        payload.append("&apptoken=");
        payload.append(apptokenXml);

        URLConnection conn = new URL(path).openConnection();
        conn.setDoOutput(true);
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"))) {
            printWriter.append(payload.toString());
        }
        return toString(conn.getInputStream());
    }

    private static String readAppTokenFromXml(String apptokenXml) {
        int ind = apptokenXml.indexOf("<applicationtokenID>");
        int endind = apptokenXml.indexOf("</applicationtokenID>");
        String token = apptokenXml.substring(ind + "<applicationtokenID>".length(),endind);
        return token;
    }

    private String readAppToken() throws IOException {
        String url = Configuration.tokenServiceUrl() + "/logon";
        String csq = computeAppAuth();
        URLConnection conn = new URL(url).openConnection();
        conn.setDoOutput(true);
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"))) {
            printWriter.append(csq);
        }
        return toString(conn.getInputStream());
    }

    private String computeAppAuth() {
        String appid = Configuration.applicationId();
        String appsecret = Configuration.applicationSecret();
        return APP_AUTH.replaceAll("#appid#",appid).replaceAll("#appsecret#",appsecret);
    }


    private static String toString(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char)c);
            }
            return result.toString();
        }
    }

}
