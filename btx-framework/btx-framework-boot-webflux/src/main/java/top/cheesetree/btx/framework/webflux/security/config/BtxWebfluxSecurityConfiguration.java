package top.cheesetree.btx.framework.webflux.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author van
 * @date 2025/6/20 14:13
 * @description TODO
 */
@Configuration
@EnableConfigurationProperties({BtxWebfluxSecurityProperties.class, BtxWebfluxCacheProperties.class})
public class BtxWebfluxSecurityConfiguration {
    @Autowired
    BtxWebfluxSecurityProperties securityProperties;

    static AntPathMatcher matcher = new AntPathMatcher();

    @Bean
    public WebFilter tokenFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            String path = request.getURI().getPath();
            if (!securityProperties.getContextInterceptorExcludePathPatterns().isEmpty() && securityProperties.getContextInterceptorExcludePathPatterns().stream().anyMatch(c -> matcher.match(c, path))) {
                return chain.filter(ctx);
            }

            ServerHttpResponse response = ctx.getResponse();
            String token = request.getHeaders().getFirst("Authorization");
            if (!StringUtils.hasText(token) || !(redisTemplateFactory.generateRedisTemplate(String.class).hasKey(CACHE_KEY_PREFIX_TOKEN + token) || redisTemplateFactory.generateRedisTemplate(String.class).hasKey(CACHE_KEY_PREFIX_USER_TOKEN + token))) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return Mono.empty();
            }

            return chain.filter(ctx);
        };
    }
}
