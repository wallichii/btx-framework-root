package top.cheesetree.btx.framework.webflux.security.exception;

import top.cheesetree.btx.framework.webflux.security.core.comm.AuthenticationException;

/**
 * @author van
 * @date 2025/6/24 15:04
 * @description TODO
 */
public class ProviderNotFoundException extends AuthenticationException {
    public ProviderNotFoundException(String msg, String code) {
        super(msg, code);
    }
}
