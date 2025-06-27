package top.cheesetree.btx.framework.webflux.security.comm;

import top.cheesetree.btx.framework.core.constants.BtxMessage;

/**
 * @author van
 * @date 2025/6/24 15:24
 * @description TODO
 */
public class BtxWebfluxSecurityMessage extends BtxMessage {

    public BtxWebfluxSecurityMessage(int code, String msg) {
        super(code, msg);
    }

    public static final BtxWebfluxSecurityMessage SECURIT_LOGIN_ERROR = new BtxWebfluxSecurityMessage(20001, "登录失败");
}
