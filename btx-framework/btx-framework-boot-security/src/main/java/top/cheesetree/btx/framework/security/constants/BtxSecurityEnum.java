package top.cheesetree.btx.framework.security.constants;

/**
 * @author van
 * @date 2022/2/10 14:24
 * @description TODO
 */
public class BtxSecurityEnum {
    public enum AuthType {
        SESSION, TOKEN, JWT, CAS, EXT_TOKEN
    }

    public enum CacheType {
        CAFFEINE,
        REDIS
    }
}
