package top.cheesetree.btx.framework.security.shiro.constants;

import top.cheesetree.btx.framework.security.constants.BtxSecurityMessage;

/**
 * @Author: van
 * @Date: 2022/1/13 10:28
 * @Description: TODO
 */
public class BtxSecurityShiroMessage extends BtxSecurityMessage {
    public BtxSecurityShiroMessage(Integer code, String message) {
        super(code, message);
    }

    public static final BtxSecurityShiroMessage SECURIT_RUNAS_ERROR = new BtxSecurityShiroMessage(21000,
            "身份切换必须是BtxShiroSecurityUserDTO类型");

}
