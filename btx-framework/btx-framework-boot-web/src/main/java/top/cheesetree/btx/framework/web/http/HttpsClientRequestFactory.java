package top.cheesetree.btx.framework.web.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SecureRandom;

/**
 * @Author: van
 * @Date: 2021/8/27 10:05
 * @Description: 自动识别 HTTPS 连接，并为 HTTPS 连接配置信任所有证书的 SSL 上下文与
 * 放行所有主机名的 HostnameVerifier，避免请求不受信任的 HTTPS 地址时报
 * PKIX path building failed。
 * 对于普通 HTTP 连接不做任何 SSL 处理，保持默认行为。
 */
public class HttpsClientRequestFactory extends SimpleClientHttpRequestFactory {
    /**
     * 版本标识：用于日志确认当前加载的 HttpsClientRequestFactory 是否为最新修复版本。
     * 若日志中出现该标识，说明已加载最新版本（自动识别 HTTPS + trust-all）。
     */
    private static final String VERSION = "1.0.0-SSL-FIX-2021";

    private static final Logger logger = LoggerFactory.getLogger(HttpsClientRequestFactory.class);

    static {
        logger.info("[HttpsClientRequestFactory] 已加载最新修复版本[{}]：自动识别 HTTPS 并信任所有证书", VERSION);
    }

    /**
     * Trust-all SSLContext, built once and shared across requests.
     */
    private static final SSLContext TRUST_ALL_SSL_CONTEXT = createTrustAllSslContext();

    /**
     * Hostname verifier that accepts any host.
     */
    private static final HostnameVerifier TRUST_ALL_HOSTNAME_VERIFIER = (hostname, session) -> true;

    private static SSLContext createTrustAllSslContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new CustomTrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize trust-all SSLContext", e);
        }
    }

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        // 自动识别 HTTPS 连接：只有 HTTPS 才配置信任所有证书的 SSL 上下文与主机名校验
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            httpsConnection.setSSLSocketFactory(TRUST_ALL_SSL_CONTEXT.getSocketFactory());
            httpsConnection.setHostnameVerifier(TRUST_ALL_HOSTNAME_VERIFIER);
            logger.debug("[HttpsClientRequestFactory] 已对[{}]配置 trust-all SSL SocketFactory", connection.getURL());
        }

        // 最后调用父类方法完成超时、请求方法等基础配置
        super.prepareConnection(connection, httpMethod);
    }
}