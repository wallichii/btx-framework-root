package top.cheesetree.btx.framework.webflux.security.core.model;

import top.cheesetree.btx.framework.core.model.ValueObject;

/**
 * @author van
 * @date 2025/6/24 14:10
 * @description TODO
 */
public interface AuthenticationToken extends ValueObject {
    Object getPrincipal();

    Object getCredentials();

}
