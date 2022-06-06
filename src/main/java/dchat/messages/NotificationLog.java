package dchat.messages;

public class NotificationLog {

    private String typeHeader = "MESSAGE_LOG";
    private String data;
    private String type; // "connection", "event"
    private String source; // "<server_*>, Example: server_1234abcd", "<client_*>, Example: client_1234abcd"

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTypeHeader() {
        return typeHeader;
    }

    public void setTypeHeader(String typeHeader) {
        this.typeHeader = typeHeader;
    }

    @Override
    public String toString() {
        return "NotificationLog{" +
                "typeHeader='" + typeHeader + '\'' +
                ", data='" + data + '\'' +
                ", type='" + type + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
