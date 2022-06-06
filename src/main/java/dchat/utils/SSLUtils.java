package dchat.utils;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;

public class SSLUtils {

    private static final Logger logger = LoggerFactory.getLogger(SSLUtils.class);

    public static SslContext createContext() throws SSLException {
        final SslContext sslCtx;
        File crtfile = new File(SystemUtils.getSSLCertificatePath());
        File keyFile = new File(SystemUtils.getSSLKeyPath());
        sslCtx = SslContextBuilder.forServer(crtfile, keyFile).build();
        return sslCtx;
    }
}
