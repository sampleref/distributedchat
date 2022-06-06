package dchat.messages;

public class MessagePayload {

    private String typeHeader = "MESSAGE_PAYLOAD";
    private String uuid;
    private String data;
    private String fromAddress;
    private String toAddress;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getTypeHeader() {
        return typeHeader;
    }

    public void setTypeHeader(String typeHeader) {
        this.typeHeader = typeHeader;
    }

    @Override
    public String toString() {
        return "MessagePayload{" +
                "typeHeader='" + typeHeader + '\'' +
                ", uuid='" + uuid + '\'' +
                ", data='" + data + '\'' +
                ", fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                '}';
    }
}
