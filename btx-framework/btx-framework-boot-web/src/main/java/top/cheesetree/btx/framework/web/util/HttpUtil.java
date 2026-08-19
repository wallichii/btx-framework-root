package top.cheesetree.btx.framework.web.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import top.cheesetree.btx.framework.web.model.dto.FileInfoDTO;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: van
 * @Date: 2021/8/27 10:05
 * @Description: TODO
 */
@Slf4j
public class HttpUtil {
    private final static int DEF_TIMEOUT = 10000;
    private final static int DEF_CON_TIMEOUT = 5000;
    private final static int DEF_FILE_TIMEOUT = 60000;

    public static String httpGet(String url, @Deprecated boolean isHttps) {
        return httpGet(url, null, DEF_TIMEOUT, isHttps);
    }

    public static String httpGet(String url, int to, @Deprecated boolean isHttps) {
        return httpGet(url, null, to, isHttps);
    }

    public static String httpGet(String url, HashMap<String, String> headers, @Deprecated boolean isHttps) {
        return httpGet(url, headers, DEF_TIMEOUT, isHttps);
    }


    public static String httpPostJson(String url, String pa, @Deprecated boolean isHttps) {
        return httpPostJson(url, pa, DEF_TIMEOUT, isHttps);
    }

    public static String httpPostJson(String url, HashMap<String, String> headers, String pa,
                                      @Deprecated boolean isHttps) {
        return httpPost(url, pa, headers, DEF_TIMEOUT, isHttps);
    }

    public static String httpPostJson(String url, HashMap<String, String> headers, String pa, int to,
                                      @Deprecated boolean isHttps) {
        return httpPost(url, pa, headers, to, isHttps);
    }

    public static String httpPostJson(String url, String pa, int to, @Deprecated boolean isHttps) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return httpPost(url, pa, headers, to, isHttps);
    }

    public static String httpAjaxPost(String url, HashMap<String, String> pa, @Deprecated boolean isHttps) {
        return httpAjaxPost(url, pa, DEF_TIMEOUT, isHttps);
    }

    public static String httpAjaxPost(String url, HashMap<String, String> pa, int to, @Deprecated boolean isHttps) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();

        if (pa != null) {
            for (Map.Entry<String, String> entry : pa.entrySet()) {
                param.add(entry.getKey(), entry.getValue());
            }
        }

        return httpPost(url, param, headers, to, isHttps);
    }

    public static String httpUploadFile(String url, FileInfoDTO info,
                                        @Deprecated boolean isHttps) {
        return httpUploadFile(url, info, null, DEF_FILE_TIMEOUT, isHttps);
    }

    public static String httpUploadFile(String url, FileInfoDTO info, HashMap<String, String> headers,
                                        @Deprecated boolean isHttps) {
        return httpUploadFile(url, info, headers, DEF_FILE_TIMEOUT, isHttps);

    }

    public static String httpUploadFile(String url, FileInfoDTO info, HashMap<String, String> headers, int to,
                                        @Deprecated boolean isHttps) {
        String ret = "";

        if (info == null) {
            return "";
        }
        if (headers == null) {
            headers = new HashMap<>(1);
        }

        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
        String filekey = StringUtils.hasLength(info.getFilekey()) ? info.getFilekey() : "file";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition =
                ContentDisposition
                        .builder("form-data")
                        .filename(info.getFilename())
                        .name(filekey)
                        .build();

        params.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<Resource> fileEntity =
                new HttpEntity<>(new ByteArrayResource(info.getFiledata()), params);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(filekey, fileEntity);
        RestTemplate restTemplate = getRestTemplate(url.startsWith("https"), to);
        HttpHeaders header = new HttpHeaders();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            header.add(entry.getKey(), entry.getValue());
        }

        HttpEntity<Object> httpEntity = new HttpEntity<>(body, header);
        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        if (res.getStatusCode().is2xxSuccessful()) {
            ret = res.getBody();
        } else {
            log.error("upload req error:url[{}] res[{}]", url, res);
        }

        return ret;
    }

    public static String httpGet(String url, HashMap<String, String> headers, int to, @Deprecated boolean isHttps
    ) {
        String ret = "";
        RestClient restClient = getRestClient(url.startsWith("https"), to);
        HttpHeaders header = new HttpHeaders();

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                header.add(entry.getKey(), entry.getValue());
            }
        }

        ResponseEntity<String> res = restClient.get().uri(url).headers(httpHeaders -> {
            httpHeaders.putAll(header);
        }).retrieve().toEntity(String.class);
        if (res.getStatusCode().is2xxSuccessful()) {
            ret = res.getBody();
        } else {
            log.error("get req error:url[{}] res[{}]", url, res);
        }

        return ret;
    }

    public static <T> String httpPost(String url, T params, HashMap<String, String> headers, int to,
                                      @Deprecated boolean isHttps
    ) {
        return httpRequest(url, params, headers, to, isHttps, HttpMethod.POST);
    }


    public static <T> String httpPut(String url, HashMap<String, String> headers, T params,
                                     @Deprecated boolean isHttps) {
        return httpRequest(url, params, headers, DEF_FILE_TIMEOUT, isHttps, HttpMethod.PUT);
    }

    public static <T> String httpPut(String url, T params, HashMap<String, String> headers, int to,
                                     @Deprecated boolean isHttps) {
        return httpRequest(url, params, headers, to, isHttps, HttpMethod.PUT);
    }

    public static <T> String httpDel(String url, HashMap<String, String> headers, T params,
                                     @Deprecated boolean isHttps) {
        return httpRequest(url, params, headers, DEF_FILE_TIMEOUT, isHttps, HttpMethod.DELETE);
    }

    public static <T> String httpDel(String url, T params, HashMap<String, String> headers, int to,
                                     @Deprecated boolean isHttps) {
        return httpRequest(url, params, headers, to, isHttps, HttpMethod.DELETE);
    }

    public static <T> String httpPatch(String url, HashMap<String, String> headers, T params,
                                       @Deprecated boolean isHttps) {
        return httpPatch(url, headers, params, DEF_FILE_TIMEOUT, isHttps);
    }

    public static <T> String httpPatch(String url, HashMap<String, String> headers, T params, int to,
                                       @Deprecated boolean isHttps) {
        return httpRequest(url, params, headers, to, isHttps, HttpMethod.PATCH);
    }

    public static <T> String httpRequest(String url, T params, HashMap<String, String> headers, int to,
                                         @Deprecated boolean isHttps,
                                         HttpMethod method) {
        String ret = "";
        RestClient restClient = getRestClient(url.startsWith("https"), to);
        HttpHeaders header = new HttpHeaders();

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                header.add(entry.getKey(), entry.getValue());
            }

            if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                header.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));
                header.put(HttpHeaders.ACCEPT, List.of(MediaType.APPLICATION_JSON_VALUE));
            }
        }

        ResponseEntity<String> res = restClient.method(method).uri(url).headers(httpHeaders -> {
            httpHeaders.putAll(header);
        }).body(params).retrieve().toEntity(String.class);
        if (res.getStatusCode().is2xxSuccessful()) {
            ret = res.getBody();
        } else {
            log.error("REQ ERROR:URL[{}] RES[{}][{}]", url, res, res.getBody());
        }

        return ret;
    }

    public static RestClient getRestClient(boolean isHttps, int timeout) {
        RestClient restClient;

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));

        if (isHttps) {
            try {
                restClient =
                        RestClient.builder().requestFactory(getSSLFactory(timeout)).messageConverters(converters).build();
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } else {
            JdkClientHttpRequestFactory hrf = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeout)) // 连接超时：5秒
                    .build());
            hrf.setReadTimeout(timeout);
            restClient = RestClient.builder().requestFactory(hrf).messageConverters(converters).build();
        }

        return restClient;
    }

    private static JdkClientHttpRequestFactory getSSLFactory(int timeout) throws NoSuchAlgorithmException,
            KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        // 关闭主机名校验，否则自签名证书报PKIX异常
        SSLParameters sslParameters = sslContext.getDefaultSSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm(null);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(timeout));
        return factory;
    }

    public static RestTemplate getRestTemplate(boolean isHttps, int timeout) {
        JdkClientHttpRequestFactory requestFactory;
        if (isHttps) {
            try {
                requestFactory = getSSLFactory(timeout);
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                throw new RuntimeException("创建HTTPS RestTemplate失败", e);
            }
        } else {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeout))
                    .build();
            requestFactory = new JdkClientHttpRequestFactory(httpClient);
            // setReadTimeout接收Duration
            requestFactory.setReadTimeout(Duration.ofSeconds(timeout));
        }

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().forEach(httpMessageConverter -> {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) httpMessageConverter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        });

        return restTemplate;
    }

}