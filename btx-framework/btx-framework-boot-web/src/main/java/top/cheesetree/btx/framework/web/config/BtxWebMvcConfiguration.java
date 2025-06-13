package top.cheesetree.btx.framework.web.config;


import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.cheesetree.btx.framework.web.http.CustomSSLSocketFactory;
import top.cheesetree.btx.framework.web.http.CustomTrustManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: van
 * @Date: 2021/8/27 10:00
 * @Description: TODO
 */
@Configuration
@EnableConfigurationProperties({BtxWebProperties.class, BtxRestProperties.class})
public class BtxWebMvcConfiguration implements WebMvcConfigurer {
    @Autowired
    BtxWebProperties btxWebProperties;

    @Autowired
    BtxRestProperties btxRestProperties;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Iterator<HttpMessageConverter<?>> iterator = converters.iterator();

        //创建fastJson消息转换器
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(new MediaType("application", "*+json"));
        supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.APPLICATION_PDF);
        supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XML);
        supportedMediaTypes.add(MediaType.IMAGE_GIF);
        supportedMediaTypes.add(MediaType.IMAGE_JPEG);
        supportedMediaTypes.add(MediaType.IMAGE_PNG);
        supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.TEXT_XML);
        fastConverter.setSupportedMediaTypes(supportedMediaTypes);

        //创建配置类
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setCharset(StandardCharsets.UTF_8);
        fastJsonConfig.setReaderFeatures(JSONReader.Feature.FieldBased, JSONReader.Feature.SupportArrayToBean);

        if (btxWebProperties.isWritedateusedateformat()) {
            fastJsonConfig.setWriterFeatures(JSONWriter.Feature.WriteNullBooleanAsFalse,
                    JSONWriter.Feature.WriteMapNullValue,
                    JSONWriter.Feature.NotWriteRootClassName);
            fastJsonConfig.setReaderFeatures(JSONReader.Feature.SupportSmartMatch);
            fastJsonConfig.setDateFormat(btxWebProperties.getDateformat());
        }

        fastConverter.setFastJsonConfig(fastJsonConfig);
        //将fastjson添加到视图消息转换器列表内
        int i = 0;
        while (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            if (converter instanceof AbstractJackson2HttpMessageConverter) {
                converters.add(i, fastConverter);
                i = -1;
                break;
            }
            i++;
        }

        if (i > -1) {
            converters.add(fastConverter);
        }

        JSONFactory.getDefaultObjectWriterProvider().setNamingStrategy(PropertyNamingStrategy.CamelCase1x);

    }

    @Bean
    @ConditionalOnClass(name = {"org.springframework.cloud.openfeign.FeignClient"})
    public WebMvcRegistrations webMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new RequestMappingHandlerMapping() {
                    @Override
                    protected boolean isHandler(Class<?> beanType) {
                        // return super.isHandler(beanType) &&
                        //								!AnnotatedElementUtils.hasAnnotation(beanType,
                        //										org.springframework.cloud.openfeign.FeignClient.class)
                        return super.isHandler(beanType);
                    }
                };
            }

        };
    }

    @Bean("btxRestTemplate")
    public RestTemplate getRestTemplate() {
        OkHttpClient okclient =
                new OkHttpClient().newBuilder().connectionPool(new ConnectionPool(btxRestProperties.getMaxPoolIdle(),
                        btxRestProperties.getMaxPoolLiveTime(), TimeUnit.MINUTES)).retryOnConnectionFailure(false).connectTimeout(btxRestProperties.getConnectTimeOut(), TimeUnit.SECONDS).readTimeout(btxRestProperties.getReadTimeOut(), TimeUnit.SECONDS).writeTimeout(btxRestProperties.getWriteTimeOut(), TimeUnit.SECONDS).build();

        return handleRestTemplate(okclient);
    }

    @Bean("btxRestSslTemplate")
    public RestTemplate getRestSslTemplate() {
        OkHttpClient okclient =
                new OkHttpClient().newBuilder().connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS,
                        ConnectionSpec.COMPATIBLE_TLS)).sslSocketFactory(new CustomSSLSocketFactory(),
                        new CustomTrustManager()).hostnameVerifier((hostname, session) -> true).connectionPool(new ConnectionPool(btxRestProperties.getMaxPoolIdle(), btxRestProperties.getMaxPoolLiveTime(), TimeUnit.MINUTES)).retryOnConnectionFailure(false).connectTimeout(btxRestProperties.getConnectTimeOut(), TimeUnit.SECONDS).readTimeout(btxRestProperties.getReadTimeOut(), TimeUnit.SECONDS).writeTimeout(btxRestProperties.getWriteTimeOut(), TimeUnit.SECONDS).build();
        return handleRestTemplate(okclient);
    }

    private RestTemplate handleRestTemplate(OkHttpClient okclient) {
        RestTemplate rt = new RestTemplate(new OkHttp3ClientHttpRequestFactory(okclient));

        rt.getMessageConverters().forEach(httpMessageConverter -> {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) httpMessageConverter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        });

        return rt;
    }

}
