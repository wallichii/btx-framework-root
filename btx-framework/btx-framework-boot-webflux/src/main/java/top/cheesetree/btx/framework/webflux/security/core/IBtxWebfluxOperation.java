package top.cheesetree.btx.framework.webflux.security.core;

import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.webflux.security.core.model.SecurityAuthUserDTO;

/**
 * @author van
 * @date 2025/6/24 11:13
 * @description TODO
 */
public interface IBtxWebfluxOperation {
    CommJSON<? extends SecurityAuthUserDTO> login(String... args);

    <T extends SecurityAuthUserDTO> T getAuthInfo();

}
