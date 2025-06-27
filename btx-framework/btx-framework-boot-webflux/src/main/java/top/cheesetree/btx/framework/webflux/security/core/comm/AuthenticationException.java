package top.cheesetree.btx.framework.webflux.security.core.comm;

import top.cheesetree.btx.framework.core.exception.BtxException;

/**
 * @author van
 * @date 2025/6/24 13:50
 * @description TODO
 */
public class AuthenticationException extends BtxException {
    public AuthenticationException(String msg, String code) {
        super(msg, code);
    }
}
