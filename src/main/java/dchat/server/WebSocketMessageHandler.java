package dchat.server;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by jwb on 3/13/15.
 */
public interface WebSocketMessageHandler {
    String handleMessage(ChannelHandlerContext ctx, String frameText);

    boolean checkIfUNameAlreadyExists(String uname);

    void addChannelIfNotExists(ChannelHandlerContext context, String name);
}
