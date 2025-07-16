package top.cheesetree.btx.framework.webflux.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import top.cheesetree.btx.framework.security.IBtxSecurityOperation;
import top.cheesetree.btx.framework.security.controller.SecurityController;
import top.cheesetree.btx.framework.security.model.SecurityUserDTO;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxAuthTokenInfo;

/**
 * @author van
 * @date 2022/4/6 15:24
 * @description TODO
 */
public class BtxWebfluxSecurityController implements SecurityController {
    @Autowired
    IBtxSecurityOperation  btxSecurityOperation;

    @Override
    public String getUserId() {
        return btxSecurityOperation.getUserId();
    }

    @Override
    public <T extends SecurityUserDTO> T getUser() {
        return btxSecurityOperation.getUserInfo();
    }

    @Override
    public WebfluxAuthTokenInfo getAuthInfo() {
        return btxSecurityOperation.getAuthInfo();
    }


}
