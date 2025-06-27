package top.cheesetree.btx.framework.webflux.security.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.cheesetree.btx.framework.webflux.security.core.comm.BtxWebfluxSecurityEnum;

/**
 * @author van
 * @date 2022/3/20 14:24
 * @description TODO
 */
@ConfigurationProperties("btx.webflux.security.cache")
@Getter
@Setter
public class BtxWebfluxCacheProperties {
    private boolean enabled = false;
    private BtxWebfluxSecurityEnum.CacheType cacheType = BtxWebfluxSecurityEnum.CacheType.CAFFEINE;
    private Integer cacheExpire = 1800;
    private String authenticationCacheName = "authenticationCache";
    private String authorizationCacheName = "authorizationCache";

}
