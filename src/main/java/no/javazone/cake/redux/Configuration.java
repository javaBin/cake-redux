package no.javazone.cake.redux;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private Map<String,String> properties = null;
    private static Configuration instance = new Configuration();

    private Configuration() {

    }

    private static String readConfigFile(String filename) {
        try (FileInputStream inputStream = new FileInputStream(filename)) {
            return EmsCommunicator.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getProperty(String key) {
        if (instance.properties == null) {
            instance.loadProps();
            if (instance.properties == null) {
                throw new IllegalStateException("Properties not initalized getting " +key);
            }
        }
        return instance.properties.get(key);

    }

    private synchronized void loadProps() {
        Map<String,String> readProps = new HashMap<>();
        String config = readConfigFile(System.getProperty("cake-redux-config-file"));
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

    public static String emsEventLocation() {
        return getProperty("emsEventLocation");
    }

    public static String submititLocation() {
        return getProperty("submititLocation");
    }

    public static Integer serverPort() {
        String serverPortStr = getProperty("serverPort");
        if (serverPortStr == null || serverPortStr.isEmpty()) {
            return null;
        }
        return Integer.parseInt(serverPortStr);
    }

    public static String smtpServer() {
        return getProperty("smthost");
    }

    public static int smtpPort() {
        return Integer.parseInt(getProperty("smtpport"));
    }



}
