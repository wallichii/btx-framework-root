package top.cheesetree.btx.framework.webflux.security.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.cheesetree.btx.framework.webflux.security.core.comm.BtxWebfluxSecurityConst;
import top.cheesetree.btx.framework.webflux.security.core.comm.BtxWebfluxSecurityEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: van
 * @Date: 2021/8/27 13:58
 * @Description: TODO
 */
@ConfigurationProperties(prefix = "btx.webflux.security")
@Getter
@Setter
public class BtxWebfluxSecurityProperties {
    private List<String> contextInterceptorExcludePathPatterns = new ArrayList<>();
    private BtxWebfluxSecurityEnum.AuthType authType;
    private String tokenKey = BtxWebfluxSecurityConst.AUTHORIZATION_KEY;
    private boolean autoPermission = false;
    private int timeOut = 3600;

}
