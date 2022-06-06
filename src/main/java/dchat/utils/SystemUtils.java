package dchat.utils;

public class SystemUtils {

    private static final String SERVER_UUID = generateServerUUID();
    private static final String SSL_CERT_PATH_KEY = "SSL_CERT_PATH";
    private static final String SSL_KEY_PATH_KEY = "SSL_KEY_PATH";

    private static final String SSL_ENABLED = "SSL_ENABLED";

    public static String getSSLCertificatePath() {
        return getEnv(SSL_CERT_PATH_KEY);
    }

    public static String getSSLKeyPath() {
        return getEnv(SSL_KEY_PATH_KEY);
    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (value == null) {
            throw new RuntimeException("No valid value for key " + key + " found");
        }
        return value;
    }

    private static String generateServerUUID() {
        //TODO
        return "";
    }

    public static String getServerUuid() {
        return SERVER_UUID;
    }

    public static boolean isSslEnabled(){
        String value = System.getenv(SSL_ENABLED);
        return value == null ? true : Boolean.getBoolean(value);
    }

}
