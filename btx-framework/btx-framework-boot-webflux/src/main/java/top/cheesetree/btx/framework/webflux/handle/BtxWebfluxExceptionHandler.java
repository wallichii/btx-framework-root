package top.cheesetree.btx.framework.webflux.handle;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.cheesetree.btx.framework.core.constants.BtxMessage;
import top.cheesetree.btx.framework.core.exception.BtxException;
import top.cheesetree.btx.framework.core.exception.BusinessException;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.webflux.exception.UnauthorizedException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author van
 * @date 2025/6/24 16:28
 * @description TODO
 */
@Slf4j
@Component
public class BtxWebfluxExceptionHandler implements ErrorWebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        CommJSON<String> ret;

        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof BtxException err) {
            if (err instanceof BusinessException) {
                ret = new CommJSON<>(BtxMessage.BUSI_ERROR.getCode(), err.getErrcode(), err.getMessage(), null);
            }else if (err instanceof UnauthorizedException) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                ret = new CommJSON<>(BtxMessage.VALIDATE_ERROR);
            }
            else {
                ret = new CommJSON<>(BtxMessage.SYSTEM_ERROR.getCode(), err.getErrcode(),
                        err.getMessage());
            }

            log.error("系统异常:[{},{}]", err.getErrcode(), err.getMessage(), ex);
        } else if (ex instanceof BindException err) {
            List<String> errmsg = new ArrayList<>();
            err.getBindingResult().getFieldErrors().forEach(e -> {
                errmsg.add(e.getDefaultMessage());
            });

            ret = new CommJSON<>(BtxMessage.VALIDATE_ERROR.getCode(), "",
                    StringUtils.collectionToDelimitedString(errmsg, ";"
                    ), null);

            log.error("系统异常:[{},{}]", "", err.getMessage(), ex);
        } else {
            ret = new CommJSON<>(BtxMessage.UNKOWN_ERROR);
            String reqid = UUID.randomUUID().toString();
            ret.setMsg(String.format("%s:%s", ret.getMsg(), reqid));
            log.error("系统异常[{}]", reqid, ex);
        }

        return response.writeWith(Mono.just(response.bufferFactory().wrap(JSON.toJSONBytes(ret))));
    }

}
