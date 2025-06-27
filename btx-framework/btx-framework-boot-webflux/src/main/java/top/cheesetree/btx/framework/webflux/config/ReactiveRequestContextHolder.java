package top.cheesetree.btx.framework.webflux.config;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static top.cheesetree.btx.framework.webflux.comm.BtxWebfluxyConst.SERVER_WEB_EXCHANGE;

/**
 * @author van
 * @date 2025/6/27 15:52
 * @description TODO
 */
public class ReactiveRequestContextHolder {
    private static final ThreadLocal<ServerWebExchange> EXCHANGE_THREAD_LOCAL = new ThreadLocal<>();

    public static void set(ServerWebExchange exchange) {
        EXCHANGE_THREAD_LOCAL.set(exchange);
    }

    public static ServerWebExchange get() {
        return EXCHANGE_THREAD_LOCAL.get();
    }

    public static Mono<ServerWebExchange> currentExchange() {
        return Mono.deferContextual(Mono::just)
                .mapNotNull(ctx -> ctx.getOrDefault(SERVER_WEB_EXCHANGE, null));
    }
}
