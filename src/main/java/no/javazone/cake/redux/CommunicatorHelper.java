package no.javazone.cake.redux;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public final class CommunicatorHelper {


    public static URLConnection openConnection(String questionUrl, boolean useAuthorization) {
        try {
            URL url = new URL(questionUrl);
            URLConnection urlConnection = url.openConnection();

            if (useAuthorization) {
                String authString = Configuration.getEmsUser() + ":" + Configuration.getEmsPassword();
                String authStringEnc = Base64Util.encode(authString);
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            }

            return urlConnection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream openStream(URLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        if (true) { // flip for debug :)
            return inputStream;
        }
        String stream = toString(inputStream);
        System.out.println("***STRAN***");
        System.out.println(stream);
        return new ByteArrayInputStream(stream.getBytes());
    }

    public static String toString(InputStream inputStream) throws IOException {
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

