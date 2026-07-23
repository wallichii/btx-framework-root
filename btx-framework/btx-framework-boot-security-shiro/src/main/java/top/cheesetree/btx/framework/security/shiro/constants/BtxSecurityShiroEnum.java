package top.cheesetree.btx.framework.security.shiro.constants;

/**
 * @author van
 * @date 2022/2/10 14:24
 * @description TODO
 */
public class BtxSecurityShiroEnum {

    public enum CACHE_TYPE {
        INNER, EHCACHE2, REDIS
    }

    public enum JWT_ALGORITHM_TYPE {
        HS256,
        HS384,
        HS512,

        /**
         * RSA PKCS1-v1_5
         */
        RS256,
        RS384,
        RS512,

        /**
         * RSA-PSS
         */
        PS256,
        PS384,
        PS512,

        /**
         * ECDSA 椭圆曲线
         */
        ES256,
        ES384,
        ES512,

        /**
         * EdDSA RFC8037
         */
        Ed25519,
        Ed448
    }
}
