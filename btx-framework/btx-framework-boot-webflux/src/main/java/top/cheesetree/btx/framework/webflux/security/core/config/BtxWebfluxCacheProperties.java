package top.cheesetree.btx.framework.webflux.security.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.cheesetree.btx.framework.security.constants.BtxSecurityEnum;

/**
 * @author van
 * @date 2022/3/20 14:24
 * @description TODO
 */
@ConfigurationProperties("btx.security.webflux.cache")
@Getter
@Setter
public class BtxWebfluxCacheProperties {
    private boolean enabled = false;
    private BtxSecurityEnum.CacheType cacheType = BtxSecurityEnum.CacheType.CAFFEINE;
    private Integer cacheExpire = 1800;
    private String authenticationCacheName = "authenticationCache";
    private String authorizationCacheName = "authorizationCache";

}
