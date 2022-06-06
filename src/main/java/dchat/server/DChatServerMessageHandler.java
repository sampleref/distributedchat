package dchat.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import dchat.messages.MessageAck;
import dchat.messages.MessageNone;
import dchat.messages.MessagePayload;
import dchat.messages.NotificationLog;
import dchat.utils.DChatUtils;
import dchat.utils.MessageUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class DChatServerMessageHandler implements WebSocketMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(DChatServerMessageHandler.class);
    private static final String SERVER_NAME = "SERVER";

    //Channel Map
    private static Map<String, AtomicReference<Channel>> handlerContextMap = new ConcurrentHashMap<>();
    private static Map<String, String> unameChannelIdMap = new ConcurrentHashMap<>();

    // stateless JSON serializer/deserializer
    private static Gson gson = new Gson();

    // need an executor for the thread that will intermittently send data to the client
    private ExecutorService executor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("ticker-processor-%d")
                    .build()
    );

    public DChatServerMessageHandler() {
    }

    public String handleMessage(ChannelHandlerContext ctx, String frameText) {
        MessageAck serverAck = new MessageAck();
        final String typeHeader = MessageUtils.readHeaderFromTextFrame(frameText);
        switch (typeHeader) {
            case "MESSAGE_ACK": {
                MessageAck messageAck = MessageUtils.getGsonInstance().fromJson(frameText, MessageAck.class);
                sendMessageAckToDestination(messageAck);
            }
            case "MESSAGE_PAYLOAD": {
                MessagePayload messagePayload = MessageUtils.getGsonInstance().fromJson(frameText, MessagePayload.class);
                //Send to receiver
                boolean result = sendMessagePayloadToDestination(messagePayload);
                if (result) {
                    //Send ACK to sender
                    Attribute<String> attr = ctx.channel().attr(DChatUtils.UNAME_ATTR);
                    String uname = attr.get();
                    serverAck.setType("ACK");
                    serverAck.setAckRefPayloadId(messagePayload.getUuid());
                    serverAck.setFromAddress(SERVER_NAME);
                    serverAck.setToAddress(StringUtils.isBlank(messagePayload.getFromAddress()) ? uname : messagePayload.getFromAddress());
                    return gson.toJson(serverAck);
                } else {
                    //Send ErrLog to sender
                    NotificationLog messageLog = new NotificationLog();
                    messageLog.setType("invalidreceiver");
                    messageLog.setSource(SERVER_NAME);
                    messageLog.setData("No valid receiver for address: " + messagePayload.getToAddress() + " with messageId: " + messagePayload.getUuid());
                    return gson.toJson(messageLog);
                }
            }
            case "MESSAGE_LOG": {
                NotificationLog notificationLog = MessageUtils.getGsonInstance().fromJson(frameText, NotificationLog.class);
                logger.info(" Received notification: " + notificationLog);
            }
            default: {
                logger.error("No valid message type header: " + typeHeader);
            }
        }
        return gson.toJson(new MessageNone());
    }

    private boolean sendMessagePayloadToDestination(MessagePayload messageRequest) {
        String channelId = unameChannelIdMap.get(messageRequest.getToAddress());
        if (StringUtils.isBlank(channelId)) {
            logger.error("No valid channel for uname: " + messageRequest.getToAddress());
            return false;
        }
        AtomicReference<Channel> channel = handlerContextMap.get(channelId);
        if (channel != null || channel.get() != null) {
            channel.get().writeAndFlush(new TextWebSocketFrame(gson.toJson(messageRequest)));
            return true;
        } else {
            logger.error("No valid channel for channelId: " + channelId);
            return false;
        }
    }

    private boolean sendMessageAckToDestination(MessageAck messageAck) {
        String channelId = unameChannelIdMap.get(messageAck.getToAddress());
        if (StringUtils.isBlank(channelId)) {
            logger.error("No valid channel for uname: " + messageAck.getToAddress());
            return false;
        }
        AtomicReference<Channel> channel = handlerContextMap.get(channelId);
        if (channel != null || channel.get() != null) {
            channel.get().writeAndFlush(new TextWebSocketFrame(gson.toJson(messageAck)));
            return true;
        } else {
            logger.error("No valid channel for channelId: " + channelId);
            return false;
        }
    }

    public void addChannelIfNotExists(ChannelHandlerContext context, String uname) {
        final String uniqueChannelId = context.channel().toString();

        if (checkIfUNameAlreadyExists(uname) || handlerContextMap.containsKey(uniqueChannelId)) {
            logger.error("Connection with given uname: " + uname + " already exists, CLOSING");
            context.channel().writeAndFlush(new TextWebSocketFrame(
                    MessageUtils.prepareLogMessageFromServer(uname, "Duplicate uname: " + uname, "duplicateuser")));
            context.channel().close();
            return;
        }

        final AtomicReference<Channel> channel = new AtomicReference(context.channel());
        handlerContextMap.put(uniqueChannelId, channel);
        unameChannelIdMap.put(uname, uniqueChannelId);
        keepTalking(uniqueChannelId);
        channel.get().closeFuture().addListener((ChannelFutureListener) future -> {
            logger.info("Channel with id: " + uniqueChannelId + " is closed");
            handlerContextMap.remove(context.channel().toString());
            unameChannelIdMap.remove(uname);
        });
    }

    private void keepTalking(String uniqueChannelId) {
        CompletableFuture.runAsync(() -> {
            while (handlerContextMap.containsKey(uniqueChannelId)) {
                //Send LOG to sender
                NotificationLog messageLog = new NotificationLog();
                messageLog.setType("connection");
                messageLog.setData("ServerPing");
                String response = gson.toJson(messageLog);
                handlerContextMap.get(uniqueChannelId).get().writeAndFlush(new TextWebSocketFrame(response));
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }, executor);
    }

    public boolean checkIfUNameAlreadyExists(String uname) {
        return unameChannelIdMap.containsKey(uname);
    }

}
