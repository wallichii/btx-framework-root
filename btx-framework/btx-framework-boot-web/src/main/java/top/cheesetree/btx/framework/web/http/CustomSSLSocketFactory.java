package top.cheesetree.btx.framework.web.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;

/**
 * @Author: van
 * @Date: 2021/8/27 10:05
 * @Description: 默认无参构造器会创建信任所有证书的 SSLSocketFactory，用于请求不受信任的
 * HTTPS 地址，避免报 PKIX path building failed。
 */
public class CustomSSLSocketFactory extends SSLSocketFactory {
    /**
     * 版本标识：用于日志确认当前加载的 CustomSSLSocketFactory 是否为最新修复版本。
     * 若日志中出现该标识，说明已加载最新版本（trust-all 证书）.
     */
    private static final String VERSION = "1.0.0-SSL-FIX-2021";

    private static final Logger log = LoggerFactory.getLogger(CustomSSLSocketFactory.class);

    private SSLSocketFactory delegate;

    public CustomSSLSocketFactory() {
        this(createTrustAllSslSocketFactory());
    }

    public CustomSSLSocketFactory(SSLSocketFactory delegate) {
        this.delegate = delegate;
        log.info("[CustomSSLSocketFactory] 已加载最新修复版本[{}]：信任所有证书", VERSION);
    }

    private static SSLSocketFactory createTrustAllSslSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new CustomTrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize trust-all SSLSocketFactory", e);
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
            throws IOException {
        final Socket underlyingSocket = delegate.createSocket(socket, host, port, autoClose);
        return overrideProtocol(underlyingSocket);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        final Socket underlyingSocket = delegate.createSocket(host, port);
        return overrideProtocol(underlyingSocket);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort)
            throws IOException {
        final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
        return overrideProtocol(underlyingSocket);
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        final Socket underlyingSocket = delegate.createSocket(host, port);
        return overrideProtocol(underlyingSocket);
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port, final InetAddress localAddress,
                               final int localPort) throws IOException {
        final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
        return overrideProtocol(underlyingSocket);
    }

    private Socket overrideProtocol(final Socket socket) {
        if (!(socket instanceof SSLSocket)) {
            throw new RuntimeException("An instance of SSLSocket is expected");
        }
        ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        return socket;
    }
}