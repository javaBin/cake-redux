package no.javazone.cake.redux;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Configuration {

    private Map<String,String> properties = null;
    private static final Configuration instance = new Configuration();

    private Configuration() {

    }

    private static String readConfigFile(String filename) {
        if (filename == null) {
            return "#xx";
        }
        try (FileInputStream inputStream = new FileInputStream(filename)) {
            return CommunicatorHelper.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setProps(Map<String,String> props) {
        instance.properties = props;
    }

    private static String getProperty(String prop, String env) {
        if (instance.properties == null) {
            instance.loadProps();
            if (instance.properties == null) {
                throw new IllegalStateException("Properties not initalized getting " +prop);
            }
        }
        return instance.properties.getOrDefault(prop, System.getenv(env));

    }

    private static String readConf(String prop, String env, String defaultValue) {
        return Optional.ofNullable(getProperty(prop, env)).orElse(defaultValue);
    }

    private synchronized void loadProps() {
        Map<String,String> readProps = new HashMap<>();
        String filename = System.getProperty("cake-redux-config-file");
        if (!filename.equals("env-only")) {
            String config = readConfigFile(filename);
            for (String line : config.split("\n")) {
                if (line.startsWith("#")) {
                    continue;
                }
                int eqpos = line.indexOf("=");
                if (eqpos == -1) {
                    throw new IllegalArgumentException("Illegal line : " + line);
                }
                readProps.put(line.substring(0, eqpos), line.substring(eqpos + 1));
            }
        }
        properties = readProps;
    }

    public static String getEmsUser() {
        return getProperty("emsUser", "EMS_USER");
    }

    public static String getEmsPassword() {
        return getProperty("emsPassword", "EMS_PASSWORD");
    }

    public static String getGoogleClientId() {
        return getProperty("googleClientId", "GOOGLE_CLIENT_ID");
    }

    public static String getGoogleClientSecret() {
        return getProperty("googleClientSecret", "GOOGLE_CLIENT_SECRET");
    }

    public static String getGoogleRedirectUrl() {
        return getProperty("googleRedirectUrl", "GOOGLE_REDIRECT_URL");
    }

    public static String getAutorizedUsers() {
        String authorizedUsers = getProperty("authorizedUsers", "AUTHORIZED_USERS");
        if (authorizedUsers == null) {
            return "";
        }
        return authorizedUsers;
    }

    public static String autorizedUserFile() {
        return getProperty("autorizedUserFile", "AUTORIZED_USER_FILE");
    }

    public static String fullUsers() {
        return getProperty("fullUsers", "FULL_USERS");
    }

    public static boolean noAuthMode() {
        return "true".equals(getProperty("noAuthMode", "NO_AUTH_MODE"));
    }

    public static String emsEventLocation() {
        return getProperty("emsEventLocation", "EMS_EVENT_LOCATION");
    }

    public static String submititLocation() {
        return getProperty("submititLocation", "SUBMITIT_LOCATION");
    }

    public static Integer serverPort() {
        String serverPortStr = getProperty("serverPort", "SERVER_PORT");
        if (serverPortStr == null || serverPortStr.isEmpty()) {
            return null;
        }
        return Integer.parseInt(serverPortStr);
    }

    public static String mailSenderImplementation() {
        return readConf("mailSenderImplementation", "MAIL_SENDER_IMPLEMENTATION","smtp");
    }

    public static String cakeLocation() {
        return getProperty("cakeLocation", "CAKE_LOCATION");
    }

    public static boolean whydaSupported() {
        return "true".equals(readConf("supportWhyda", "SUPPORT_WHYDA","false"));
    }

    public static String logonRedirectUrl() {
        return readConf("logonRedirectUrl",  "LOGON_REDIRECT_URL", "http://localhost:9997/sso/login?redirectURI=http://localhost:8088/admin/");
    }

    public static String tokenServiceUrl() {
        return readConf("tokenServiceUrl", "TOKEN_SERIVCE_URL","http://localhost:9998/tokenservice");
    }

    public static String applicationId() {
        return readConf("applicationId", "APPLICATION_ID", "99");
    }

    public static String feedbackStoreFilename() {
        return readConf("feedbackStoreFilename", "FEEDBACK_STORE_FILENAME",null);
    }

    public static String applicationSecret() { return readConf("applicationSecret", "APPLICATION_SECRET", "33879936R6Jr47D4Hj5R6p9qT");}

    public static long emailSleepTime() {
        return Long.parseLong(readConf("emailSleepTime", "EMAIL_SLEEP_TIME","5000"));
    }

    public static String sleepingPillBaseLocation() {
        return readConf("sleepingPillBaseLocation", "SLEEPINGPILL_BASE_LOCATION","http://localhost:8082");
    }

    public static String sleepingpillUser() {
        return readConf("sleepingpillUser", "SLEEPINGPILL_USER",null);
    }

    public static String sleepingpillPassword() {
        return readConf("sleepingpillPassword", "SLEEPINGPILL_PASSWORD",null);
    }

    public static String feedbackDaoImpl() {
        return readConf("feedbackDaoImpl", "FEEDBACK_DAO_IMPL","sleepingpill");
    }

    public static String videoAdminPassword() {
        return readConf("videoAdminPassword", "VIDEO_ADMIN_PASSWORD","dummy:bingo");
    }

    public static String videoAdminConference() {
        return readConf("videoAdminConference", "VIDEO_ADMIN_CONFERENCE","30d5c2f1cb214fc8b0649a44fdf3b4bf");
    }

    public static String sendGridKey() {
        return readConf("sendGridKey", "SENDGRID_KEY",null);
    }

    public static String slackAppId() {
        return readConf("slackAppId", "SLACK_APP_ID", null);
    }

    public static String slackClientSecret() {
        return readConf("slackClientSecret", "SLACK_CLIENT_SECRET",null);
    }

    public static String slackAuthChannel() {
        return readConf("slackAuthChannel", "SLACK_AUTH_CHANNEL",null);
    }

    public static String slackApiToken() {
        return readConf("slackApiToken", "SLACK_API_TOKEN",null);
    }

    public static LocalDate conferenceWednesday() {
        return LocalDate.parse(readConf("conferenceWednesday", "CONFERENCE_WEDNESDAY","2019-09-11"));
    }


    public static void setConfigFile(String[] args) {
        System.setProperty("cake-redux-config-file",args[0]);
    }
}
