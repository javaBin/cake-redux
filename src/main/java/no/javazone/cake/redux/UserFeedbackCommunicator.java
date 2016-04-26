package no.javazone.cake.redux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class UserFeedbackCommunicator {

    public String feedback(String encFeedback) {
        String urlToDevNull = Base64Util.decode(encFeedback);
        URLConnection urlConnection = CommunicatorHelper.openConnection(urlToDevNull, false);
        try {
            InputStream is = CommunicatorHelper.openStream(urlConnection);
            return CommunicatorHelper.toString(is);
        } catch (IOException ignore) {
        }
        return null;
    }

}

