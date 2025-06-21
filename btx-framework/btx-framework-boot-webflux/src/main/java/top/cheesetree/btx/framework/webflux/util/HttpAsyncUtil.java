package top.cheesetree.btx.framework.webflux.util;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import top.cheesetree.btx.framework.core.model.ValueObject;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: van
 * @Date: 2021/8/27 10:05
 * @Description: TODO
 */
@Slf4j
public class HttpAsyncUtil {
    private static final Map<HttpReqDTO, WebClient> webClientMap = new ConcurrentHashMap<>();

    private final static int DEF_TIMEOUT = 10000;
    private final static int DEF_CON_TIMEOUT = 5000;
    private final static int DEF_FILE_TIMEOUT = 60000;

    public static Flux<byte[]> httpAsyncGet(String url, boolean isHttps) {
        return httpAsyncGet(url, null, DEF_TIMEOUT, isHttps);
    }

    public static Flux<byte[]> httpAsyncGet(String url, int to, boolean isHttps) {
        return httpAsyncGet(url, null, to, isHttps);
    }

    public static Flux<byte[]> httpAsyncGet(String url, HashMap<String, String> headers, boolean isHttps) {
        return httpAsyncGet(url, headers, DEF_TIMEOUT, isHttps);
    }

    public static Flux<byte[]> httpAsyncPostJson(String url, String pa, boolean isHttps) {
        return httpAsyncPostJson(url, pa, DEF_TIMEOUT, isHttps);
    }

    public static Flux<byte[]> httpAsyncPostJson(String url, HashMap<String, String> headers, String pa,
                                                 boolean isHttps) {
        return httpAsyncPost(url, pa, headers, DEF_TIMEOUT, isHttps);
    }

    public static Flux<byte[]> httpAsyncPostJson(String url, String pa, int to, boolean isHttps) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return httpAsyncPost(url, pa, headers, to, isHttps);
    }

    /**
     * get方法
     */
    public static Flux<byte[]> httpAsyncGet(String url, HashMap<String, String> headers, int to, boolean isHttps) {
        return httpAsyncRequest(url, "", headers, to, isHttps, HttpMethod.GET);
    }

    public static <T> Flux<byte[]> httpAsyncPost(String url, T params, HashMap<String, String> headers, int to,
                                                 boolean isHttps) {
        return httpAsyncRequest(url, params, headers, to, isHttps, HttpMethod.POST);
    }

    public static <T> Flux<byte[]> httpAsyncPut(String url, HashMap<String, String> headers, T params,
                                                boolean isHttps) {
        return httpAsyncRequest(url, params, headers, DEF_FILE_TIMEOUT, isHttps, HttpMethod.PUT);
    }

    public static <T> Flux<byte[]> httpAsyncPut(String url, T params, HashMap<String, String> headers, int to,
                                                boolean isHttps) {
        return httpAsyncRequest(url, params, headers, to, isHttps, HttpMethod.PUT);
    }

    public static <T> Flux<byte[]> httpAsyncDel(String url, HashMap<String, String> headers, T params,
                                                boolean isHttps) {
        return httpAsyncRequest(url, params, headers, DEF_FILE_TIMEOUT, isHttps, HttpMethod.DELETE);
    }

    public static <T> Flux<byte[]> httpAsyncRequest(String url, T params, HashMap<String, String> headers, int to,
                                                    boolean isHttps,
                                                    HttpMethod method) {
        return getWebClient(isHttps, to).method(method).uri(url).headers(h -> h.addAll(opRequsetHead(headers))).bodyValue(params).retrieve().bodyToFlux(byte[].class).onErrorResume(Flux::error);
    }

    public static HttpHeaders opRequsetHead(HashMap<String, String> headers) {
        HttpHeaders header = new HttpHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                header.add(entry.getKey(), entry.getValue());
            }
        }

        if (headers == null || !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            header.put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
            header.put(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
        }

        return header;
    }

    public static WebClient getWebClient(boolean isHttps, int timeout) {
        WebClient webClient;

        HttpReqDTO r = HttpReqDTO.builder().https(isHttps).timeout(timeout).build();
        if (webClientMap.containsKey(r)) {
            webClient = webClientMap.get(r);
        } else {
            HttpClient httpClient;
            if (isHttps) {
                SslContext context;
                try {
                    context = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                } catch (SSLException e) {
                    throw new RuntimeException(e);
                }

                httpClient =
                        HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEF_CON_TIMEOUT).responseTimeout(Duration.ofSeconds(timeout)).secure(t -> t.sslContext(context));
            } else {
                httpClient =
                        HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEF_CON_TIMEOUT).responseTimeout(Duration.ofSeconds(timeout));
            }

            webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
            webClientMap.put(r, webClient);
        }

        return webClient;
    }

    @Getter
    @Setter
    @Builder
    public static class HttpReqDTO implements ValueObject {
        Boolean https;
        Integer timeout;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HttpReqDTO that = (HttpReqDTO) o;
            return https.equals(that.https) && timeout.equals(that.timeout);
        }

        @Override
        public int hashCode() {
            return Objects.hash(https, timeout);
        }

    }

}