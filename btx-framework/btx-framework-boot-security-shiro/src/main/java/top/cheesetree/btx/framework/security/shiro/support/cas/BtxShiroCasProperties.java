package top.cheesetree.btx.framework.security.shiro.support.cas;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.client.Protocol;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author van
 * @date 2022/2/21 10:43
 * @description TODO
 */
@ConfigurationProperties(prefix = "btx.security.shiro.cas", ignoreUnknownFields = false)
@Getter
@Setter
public class BtxShiroCasProperties {
    private String serverUrlPrefix;

    private Protocol validationType = Protocol.CAS3;

    /**
     * CAS server login URL E.g. https://example.com/cas/login or https://cas.example/login. Required.
     */
    private String serverLoginUrl;

    /**
     * CAS-protected client application host URL E.g. https://myclient.example.com Required.
     */
    private String clientHostUrl;

    /**
     * Cas20ProxyReceivingTicketValidationFilter proxyCallbackUrl parameter.
     */
    private String proxyCallbackUrl;

    /**
     * Cas20ProxyReceivingTicketValidationFilter proxyReceptorUrl parameter.
     */
    private String proxyReceptorUrl;

    private Boolean skipTicketValidation = false;

    private String devUserName;

}
