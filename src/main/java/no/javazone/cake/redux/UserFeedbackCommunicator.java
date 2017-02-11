package no.javazone.cake.redux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Optional;

public class UserFeedbackCommunicator {

    public Optional<String> feedback(Optional<String> emslocation) {
        if (!emslocation.isPresent()) {
            return Optional.empty();
        }

        String urlToDevNull = convertFromEmsToDevNullUrl(emslocation.get());
        URLConnection urlConnection = CommunicatorHelper.openConnection(urlToDevNull, false);
        try {
            InputStream is = CommunicatorHelper.openStream(urlConnection);
            return Optional.of(CommunicatorHelper.toString(is));
        } catch (IOException ignore) {
            return Optional.empty();
        }
    }

    private String convertFromEmsToDevNullUrl(String emsUrl) {
        return emsUrl.replaceAll("\\/ems\\/", "/devnull/") + "/feedbacks";
    }

}

