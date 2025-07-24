package top.cheesetree.btx.framework.webflux.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import top.cheesetree.btx.framework.security.IBtxSecurityOperation;
import top.cheesetree.btx.framework.security.controller.SecurityController;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxAuthTokenInfo;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxSecurityUserDTO;

/**
 * @author van
 * @date 2022/4/6 15:24
 * @description TODO
 */
public abstract class BtxWebfluxSecurityController<T extends WebfluxSecurityUserDTO, A extends WebfluxAuthTokenInfo> implements SecurityController<T, A> {
    @Autowired
    IBtxSecurityOperation<T,A> btxSecurityOperation;

    @Override
    public String getUserId() {
        return btxSecurityOperation.getUserId();
    }

    @Override
    public T getUser() {
        return btxSecurityOperation.getUserInfo();
    }

    @Override
    public A getAuthInfo() {
        return btxSecurityOperation.getAuthInfo();
    }


}
