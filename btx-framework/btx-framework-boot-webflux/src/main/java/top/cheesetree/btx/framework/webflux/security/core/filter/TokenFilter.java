package top.cheesetree.btx.framework.webflux.security.core.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.cheesetree.btx.framework.security.config.BtxSecurityProperties;
import top.cheesetree.btx.framework.webflux.security.BtxWebfluxCacheFactory;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxSecurityProperties;
import top.cheesetree.btx.framework.webflux.security.core.context.SecurityContextHolder;
import top.cheesetree.btx.framework.webflux.security.core.context.SecurityContextImpl;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationInfo;
import top.cheesetree.btx.framework.webflux.security.core.model.SimpleAuthorizationInfo;

import static top.cheesetree.btx.framework.webflux.security.core.comm.BtxWebfluxSecurityConst.CACHE_KEY_PREFIX_TOKEN;

/**
 * @author van
 * @date 2025/6/24 16:11
 * @description TODO
 */
@Component
@ConditionalOnProperty(name = "btx.security.webflux.auth-type", havingValue = "TOKEN")
public class TokenFilter implements WebFilter {
    @Autowired
    BtxWebfluxSecurityProperties btxWebfluxSecurityProperties;
    @Autowired
    BtxSecurityProperties securityProperties;

    @Autowired(required = false)
    BtxWebfluxCacheFactory btxWebfluxCacheFactory;

    static AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange ctx, WebFilterChain chain) {
        ServerHttpRequest request = ctx.getRequest();
        String path = request.getURI().getPath();
        if (!securityProperties.getContextInterceptorExcludePathPatterns().isEmpty() && securityProperties.getContextInterceptorExcludePathPatterns().stream().anyMatch(c -> matcher.match(c, path))) {
            return chain.filter(ctx);
        }

        ServerHttpResponse response = ctx.getResponse();
        if (btxWebfluxCacheFactory != null) {
            String token = request.getHeaders().getFirst(btxWebfluxSecurityProperties.getTokenKey());

            if (!StringUtils.hasText(token) || !btxWebfluxCacheFactory.generateCache().containsKey(CACHE_KEY_PREFIX_TOKEN + token)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return Mono.empty();
            } else {
                AuthenticationInfo auth = btxWebfluxCacheFactory.generateCache().get(CACHE_KEY_PREFIX_TOKEN + token);
                auth = new SimpleAuthorizationInfo(auth.getCredentials(),
                        auth.getPrincipals(), true);
                SecurityContextHolder.setContext(new SecurityContextImpl(auth));
            }
        } else {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return Mono.empty();
        }

        return chain.filter(ctx);
    }
}
