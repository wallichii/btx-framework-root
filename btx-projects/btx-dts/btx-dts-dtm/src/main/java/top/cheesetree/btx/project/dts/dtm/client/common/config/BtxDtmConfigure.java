package top.cheesetree.btx.project.dts.dtm.client.common.config;


import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author van
 * @date 2022/3/1 11:37
 * @description TODO
 */
@EnableConfigurationProperties({BtxDtmProperties.class})
@Configuration
@MapperScan({"top.cheesetree.btx.project.dts.dtm.client.**.mapper"})
public class BtxDtmConfigure {
    @Autowired
    BtxDtmProperties btxDtmProperties;

    @Bean(name = "dtmRestTemplate")
    public RestTemplate dtmRestTemplate() {
        OkHttpClient okclient =
                new OkHttpClient().newBuilder().connectionPool(new ConnectionPool(btxDtmProperties.getMaxPoolIdle(),
                        btxDtmProperties.getMaxPoolLiveTime(), TimeUnit.MINUTES)).retryOnConnectionFailure(false).connectTimeout(btxDtmProperties.getConnectTimeOut(),
                        TimeUnit.SECONDS).readTimeout(btxDtmProperties.getReadTimeOut(), TimeUnit.SECONDS).writeTimeout(btxDtmProperties.getWriteTimeOut(), TimeUnit.SECONDS).build();
        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory(okclient));
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();

        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        fastConverter.setSupportedMediaTypes(supportedMediaTypes);

        //创建配置类
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setWriterFeatures(
                JSONWriter.Feature.WriteMapNullValue,
                JSONWriter.Feature.WriteNullListAsEmpty,
                JSONWriter.Feature.WriteNullStringAsEmpty,
                JSONWriter.Feature.WriteNullBooleanAsFalse
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        converters.add(fastConverter);

        return restTemplate;
    }
}
