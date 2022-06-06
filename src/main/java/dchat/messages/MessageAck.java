package dchat.messages;

/**
 *
 */
public class MessageAck {

    private String typeHeader = "MESSAGE_ACK";
    private String uuid;
    private String ackRefPayloadId;
    private String fromAddress;
    private String toAddress;
    private String type; //"sent", "received", "read", "error"
    private String data;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAckRefPayloadId() {
        return ackRefPayloadId;
    }

    public void setAckRefPayloadId(String ackRefPayloadId) {
        this.ackRefPayloadId = ackRefPayloadId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTypeHeader() {
        return typeHeader;
    }

    public void setTypeHeader(String typeHeader) {
        this.typeHeader = typeHeader;
    }

    @Override
    public String toString() {
        return "MessageAck{" +
                "typeHeader='" + typeHeader + '\'' +
                ", uuid='" + uuid + '\'' +
                ", ackRefPayloadId='" + ackRefPayloadId + '\'' +
                ", fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
