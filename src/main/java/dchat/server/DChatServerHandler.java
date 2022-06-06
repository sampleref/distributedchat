package dchat.server;

import dchat.utils.DChatUtils;
import dchat.utils.MessageUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.Attribute;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DChatServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(DChatServerHandler.class);

    protected WebSocketServerHandshaker handshaker;
    private StringBuilder frameBuffer = null;
    protected WebSocketMessageHandler wsMessageHandler = new DChatServerMessageHandler();
    protected NettyHttpFileHandler httpFileHandler = new NettyHttpFileHandler();

    public DChatServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            this.handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            this.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    protected void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        logger.debug("Received incoming frame [{}]", frame.getClass().getName());
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            if (frameBuffer != null) {
                handleMessageCompleted(ctx, frameBuffer.toString());
            }
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (frame instanceof PongWebSocketFrame) {
            logger.info("Pong frame received");
            return;
        }

        if (frame instanceof TextWebSocketFrame) {
            frameBuffer = new StringBuilder();
            frameBuffer.append(((TextWebSocketFrame) frame).text());
        } else if (frame instanceof ContinuationWebSocketFrame) {
            if (frameBuffer != null) {
                frameBuffer.append(((ContinuationWebSocketFrame) frame).text());
            } else {
                logger.warn("Continuation frame received without initial frame.");
            }
        } else {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        // Check if Text or Continuation Frame is final fragment and handle if needed.
        if (frame.isFinalFragment()) {
            handleMessageCompleted(ctx, frameBuffer.toString());
            frameBuffer = null;
        }
    }

    protected void handleMessageCompleted(ChannelHandlerContext ctx, String frameText) {
        String response = wsMessageHandler.handleMessage(ctx, frameText);
        if (response != null) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(response));
        }
    }

    protected boolean handleREST(ChannelHandlerContext ctx, FullHttpRequest req) {
        // check request path here and process any HTTP REST calls
        // return true if message has been processed
        return false;
    }

    protected void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req)
            throws Exception {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            httpFileHandler.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        // If you're going to do normal HTTP POST authentication before upgrading the
        // WebSocket, the recommendation is to handle it right here
        if (req.method() == HttpMethod.POST) {
            httpFileHandler.sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        // Allow only GET methods.
        if (req.method() != HttpMethod.GET) {
            httpFileHandler.sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        // Send the demo page and favicon.ico
        if ("/".equals(req.uri())) {
            httpFileHandler.sendRedirect(ctx, "/index.html");
            return;
        }

        // check for websocket upgrade request
        String upgradeHeader = req.headers().get("Upgrade");
        if (upgradeHeader != null && "websocket".equalsIgnoreCase(upgradeHeader)) {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
            if (queryStringDecoder.parameters() == null
                    || !queryStringDecoder.parameters().containsKey("uname")
                    || StringUtils.isBlank(queryStringDecoder.parameters().get("uname").get(0))) {
                logger.error("Invalid ws request, missing uname");
                ctx.channel().writeAndFlush(new TextWebSocketFrame(MessageUtils.prepareLogMessageFromServer("", "No valid uname", "invaliduser")));
                ctx.channel().close();
                return;
            }
            String url = "wss://" + req.headers().get("Host") + "/wsdchat";
            final String uname = queryStringDecoder.parameters().get("uname").get(0);
            Attribute<String> attr = ctx.channel().attr(DChatUtils.UNAME_ATTR);
            attr.set(queryStringDecoder.parameters().get("uname").get(0));
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(url, null, false);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                ChannelFuture future = handshaker.handshake(ctx.channel(), req);
                future.addListener((ChannelFutureListener) futureListener -> {
                    if (!futureListener.isSuccess()) {
                        logger.error(futureListener.cause().getMessage());
                    } else {
                        logger.info("Websocket handshake success -> OPEN");
                        wsMessageHandler.addChannelIfNotExists(ctx, uname);
                    }
                });
            }
        } else {
            boolean handled = handleREST(ctx, req);
            if (!handled) {
                httpFileHandler.sendFile(ctx, req);
            }
        }
    }
}
