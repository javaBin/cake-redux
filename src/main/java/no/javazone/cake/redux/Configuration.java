package no.javazone.cake.redux;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private static Map<String,String> properties = null;

    public static void init(String filename) {
        Map<String,String> readProps = new HashMap<>();
        String config = readConfigFile(filename);
        for (String line : config.split("\n")) {
            if (line.startsWith("#")) {
                continue;
            }
            int eqpos = line.indexOf("=");
            if (eqpos == -1) {
                throw new IllegalArgumentException("Illegal line : " + line);
            }
            readProps.put(line.substring(0,eqpos),line.substring(eqpos+1));
        }
        properties = readProps;
    }

    private static String readConfigFile(String filename) {
        try (FileInputStream inputStream = new FileInputStream(filename)) {
            return EmsCommunicator.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getProperty(String key) {
        if (properties == null) {
            throw new IllegalStateException("Properties not initalized");
        }
        return properties.get(key);

    }

    public static String getEmsUser() {
        return getProperty("emsUser");
    }

    public static String getEmsPassword() {
        return getProperty("emsPassword");
    }

    public static String getGoogleClientId() {
        return getProperty("googleClientId");
    }

    public static String getGoogleClientSecret() {
        return getProperty("googleClientSecret");
    }

    public static String getGoogleRedirectUrl() {
        return getProperty("googleRedirectUrl");
    }

    public static String getAutorizedUsers() {
        String authorizedUsers = getProperty("authorizedUsers");
        if (authorizedUsers == null) {
            return "";
        }
        return authorizedUsers;
    }

    public static boolean noAuthMode() {
        return "true".equals(getProperty("noAuthMode"));
    }
}
