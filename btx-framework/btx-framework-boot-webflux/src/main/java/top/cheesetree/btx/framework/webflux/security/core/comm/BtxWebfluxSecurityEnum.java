package top.cheesetree.btx.framework.webflux.security.core.comm;

/**
 * @author van
 * @date 2022/2/10 14:24
 * @description TODO
 */
public class BtxWebfluxSecurityEnum {

    public enum CacheType {
        CAFFEINE, REDIS
    }

    public enum AuthType {
         TOKEN, JWT
    }
}
