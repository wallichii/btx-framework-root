package top.cheesetree.btx.framework.webflux.security.core.model;

import top.cheesetree.btx.framework.core.model.ValueObject;

/**
 * @author van
 * @date 2025/6/24 14:41
 * @description TODO
 */
public interface AuthenticationInfo extends ValueObject {
    Object getPrincipals();

    Object getCredentials();

    boolean isAuthenticated();
}
