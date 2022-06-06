package dchat.utils;

import com.google.gson.Gson;
import dchat.messages.NotificationLog;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

public class MessageUtils {

    public static Gson getGsonInstance() {
        // stateless JSON serializer/deserializer
        return new Gson();
    }

    public static String prepareLogMessageFromServer(String clientId, String message, String type) {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setType(type);
        notificationLog.setData(message);
        notificationLog.setSource(SystemUtils.getServerUuid());
        return getGsonInstance().toJson(notificationLog);
    }

    public static String readHeaderFromTextFrame(String text){
        Properties data = getGsonInstance().fromJson(text, Properties.class);
        String typeHeader = data.getProperty("typeHeader");
        return StringUtils.isBlank(typeHeader) ? "" : typeHeader;
    }
}
