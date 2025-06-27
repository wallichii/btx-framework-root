package top.cheesetree.btx.framework.webflux.security.core.authentication;

import top.cheesetree.btx.framework.webflux.security.core.comm.AuthenticationException;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationInfo;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationToken;

/**
 * @author van
 * @date 2025/6/24 13:49
 * @description TODO
 */
public interface AuthenticationProvider {
    AuthenticationInfo authenticate(AuthenticationToken authentication) throws AuthenticationException;

    boolean supports(Class<?> authentication);
}
