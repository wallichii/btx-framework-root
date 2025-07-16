package top.cheesetree.btx.framework.webflux.security.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.cheesetree.btx.framework.security.constants.BtxSecurityEnum;
import top.cheesetree.btx.framework.webflux.security.core.comm.BtxWebfluxSecurityConst;

/**
 * @Author: van
 * @Date: 2021/8/27 13:58
 * @Description: TODO
 */
@ConfigurationProperties(prefix = "btx.security.webflux")
@Getter
@Setter
public class BtxWebfluxSecurityProperties{
    private BtxSecurityEnum.AuthType authType;
    private String tokenKey = BtxWebfluxSecurityConst.AUTHORIZATION_KEY;
    private boolean autoPermission = false;
    private int timeOut = 3600;

}
