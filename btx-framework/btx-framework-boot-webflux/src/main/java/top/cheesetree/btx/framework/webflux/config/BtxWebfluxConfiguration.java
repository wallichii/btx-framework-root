package top.cheesetree.btx.framework.webflux.config;


import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.cheesetree.btx.framework.webflux.fastjson.FastJson2Reader;
import top.cheesetree.btx.framework.webflux.fastjson.FastJson2Writer;

import static top.cheesetree.btx.framework.webflux.comm.BtxWebfluxyConst.SERVER_WEB_EXCHANGE;

/**
 * @Author: van
 * @Date: 2021/8/27 10:00
 * @Description: TODO
 */
@Configuration
@EnableConfigurationProperties({BtxWebfluxCorsProperties.class})
@EnableWebFlux
public class BtxWebfluxConfiguration implements WebFluxConfigurer {
    @Resource
    BtxWebfluxCorsProperties corsProperties;

    @Bean
    @ConditionalOnProperty(name = "btx.webflux.cors.enabled", havingValue = "true")
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();

            if (CorsUtils.isCorsRequest(request)) {
                HttpHeaders requestHeaders = request.getHeaders();
                ServerHttpResponse response = ctx.getResponse();
                HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();
                HttpHeaders headers = response.getHeaders();

                if (corsProperties.getOrigins().isEmpty()) {
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, requestHeaders.getOrigin());
                } else if (corsProperties.getOrigins().contains(requestHeaders.getOrigin())) {
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, requestHeaders.getOrigin());
                }

                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, corsProperties.getAllowCredentials());
                //允许请求方式
                if (corsProperties.getMethods().isEmpty()) {
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod == null ? "" :
                            requestMethod.name());
                } else {
                    headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, corsProperties.getMethods());
                }

                headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, !corsProperties.getAllowHeaders().isEmpty()
                        ? corsProperties.getAllowHeaders() : requestHeaders.getAccessControlRequestHeaders());

                if (corsProperties.getExposedHeaders().isEmpty()) {
                    headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                } else {
                    headers.addAll(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, corsProperties.getExposedHeaders());
                }

                headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(corsProperties.getMaxAge()));
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }

            }

            return chain.filter(ctx);
        };
    }

    @Bean
    @Order
    @ConditionalOnProperty(name = "btx.webflux.exchange.enable", havingValue = "true", matchIfMissing = true)
    public WebFilter exchangeContextWebFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ReactiveRequestContextHolder.set(exchange);
            return chain.filter(exchange)
                    .contextWrite(ctx -> ctx.put(SERVER_WEB_EXCHANGE, exchange));
        };
    }

    @Bean
    public CodecCustomizer fastjson2CodecCustomizer() {
        return (configurer) -> {
            configurer.registerDefaults(false);
            configurer.customCodecs().register(new FastJson2Reader());
            configurer.customCodecs().register(new FastJson2Writer());
        };
    }


}
