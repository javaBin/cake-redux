package no.javazone.cake.redux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class UserFeedbackCommunicator {

    public String feedback(String emsFeedbackUrl) {
        String urlToDevNull = convertFromEmsToDevNullUrl(emsFeedbackUrl);
        URLConnection urlConnection = CommunicatorHelper.openConnection(urlToDevNull, false);
        try {
            InputStream is = CommunicatorHelper.openStream(urlConnection);
            return CommunicatorHelper.toString(is);
        } catch (IOException ignore) {
        }
        return null;
    }

    private String convertFromEmsToDevNullUrl(String encEmsUrl) {
        String emsUrl = Base64Util.decode(encEmsUrl);
        return emsUrl.replaceAll("\\/ems\\/", "/devnull/") + "/feedbacks";
    }

}

