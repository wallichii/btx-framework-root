package top.cheesetree.btx.framework.webflux.security.core.model;

import lombok.Setter;

/**
 * @author van
 * @date 2025/6/24 13:35
 * @description TODO
 */

public class StatelessToken implements AuthenticationToken {
    private String username;
    private String password;
    @Setter
    private boolean authenticated = false;

    public StatelessToken(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Object getPrincipal() {
        return this.username;
    }

    @Override
    public Object getCredentials() {
        return this.password;
    }
}
