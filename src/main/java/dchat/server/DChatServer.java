package dchat.server;

import dchat.utils.SSLUtils;
import dchat.utils.SystemUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.security.cert.CertificateException;

public final class DChatServer {
   private static final Logger logger = LoggerFactory.getLogger(DChatServerHandler.class);
   private static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));

   public static void main(String[] args) throws Exception {
      runMainNettyServer();
   }

   public static SslContext sslContext() throws CertificateException, SSLException {
      return SSLUtils.createContext();
   }

   public static void runMainNettyServer() throws Exception{
      // Configure the server.
      EventLoopGroup bossGroup = new NioEventLoopGroup(1);
      EventLoopGroup workerGroup = new NioEventLoopGroup();
      try {
         ServerBootstrap b = new ServerBootstrap();
         b.group(bossGroup, workerGroup)
                 .channel(NioServerSocketChannel.class)
                 .handler(new LoggingHandler(LogLevel.INFO))
                 .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                       ChannelPipeline p = ch.pipeline();
                       if(SystemUtils.isSslEnabled()){
                          p.addLast("ssl", sslContext().newHandler(ch.alloc()));
                          logger.info("App SSL Enabled for this run");
                       }else {
                          logger.info("App SSL Disabled for this run");
                       }
                       p.addLast("encoder", new HttpResponseEncoder());
                       p.addLast("decoder", new HttpRequestDecoder());
                       p.addLast("aggregator", new HttpObjectAggregator(65536));
                       p.addLast("chunkedWriter", new ChunkedWriteHandler());
                       p.addLast("handler", new DChatServerHandler());
                    }
                 });

         // Start the server.
         ChannelFuture f = b.bind(PORT).sync();
         logger.info("Dist Chat Server started");

         // Wait until the server socket is closed.
         f.channel().closeFuture().sync();
      } finally {
         logger.info("Dist Chat Server shutdown started");
         // Shut down all event loops to terminate all threads.
         bossGroup.shutdownGracefully();
         workerGroup.shutdownGracefully();
         logger.info("Dist Chat Server shutdown completed");
      }
   }
}
