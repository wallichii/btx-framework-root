package top.cheesetree.btx.framework.webflux.security.core.model;

/**
 * @author van
 * @date 2025/6/24 15:34
 * @description TODO
 */
public class SimpleAuthorizationInfo implements AuthenticationInfo {
    private Object principal;
    private Object credentials;
    private final boolean authenticated;

    public SimpleAuthorizationInfo(Object credentials, Object principal, boolean authenticated) {
        this.principal = principal;
        this.credentials = credentials;
        this.authenticated = authenticated;
    }

    @Override
    public Object getPrincipals() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

}
