package top.cheesetree.btx.framework.security.shiro.support.jwt;

import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * @author van
 * @date 2022/2/21 09:19
 * @description TODO
 */
@Getter
@Setter
public class JwtToken extends UsernamePasswordToken {
    private String jwt = "";

    public JwtToken() {
        super();
    }

    public JwtToken(String jwt) {
        super();
        this.jwt = jwt;
    }

    public JwtToken(String username, char[] password) {
        super(username, password);
    }

    public JwtToken(String username, String password) {
        super(username, password);
    }

    public JwtToken(String username, char[] password, String host) {
        super(username, password, host);
    }

    public JwtToken(String username, String password, String host) {
        super(username, password, host);
    }

    public JwtToken(String username, char[] password, boolean rememberMe) {
        super(username, password, rememberMe);
    }

    public JwtToken(String username, String password, boolean rememberMe) {
        super(username, password, rememberMe);
    }

    public JwtToken(String username, char[] password, boolean rememberMe, String host) {
        super(username, password, rememberMe, host);
    }

    public JwtToken(String username, String password, boolean rememberMe, String host) {
        super(username, password, rememberMe, host);
    }
}
