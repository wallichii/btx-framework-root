package top.cheesetree.btx.framework.security.shiro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import top.cheesetree.btx.framework.security.IBtxSecurityOperation;
import top.cheesetree.btx.framework.security.controller.SecurityController;
import top.cheesetree.btx.framework.security.shiro.model.AuthTokenInfo;
import top.cheesetree.btx.framework.security.shiro.model.BtxShiroSecurityUserDTO;

/**
 * @author van
 * @date 2022/4/6 15:24
 * @description TODO
 */
public class BtxSpringSecurityController<T extends BtxShiroSecurityUserDTO, A extends AuthTokenInfo> implements SecurityController<T,A> {
    @Autowired
    IBtxSecurityOperation<T, A> btxSecurityShiroOperation;

    @Override
    public String getUserId() {
        return btxSecurityShiroOperation.getUserId();
    }

    @Override
    public  T getUser() {
        return btxSecurityShiroOperation.getUserInfo();
    }

    @Override
    public A getAuthInfo() {
        return btxSecurityShiroOperation.getAuthInfo();
    }


}
