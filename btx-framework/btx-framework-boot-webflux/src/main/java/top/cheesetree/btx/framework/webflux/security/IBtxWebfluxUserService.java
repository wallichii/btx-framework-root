package top.cheesetree.btx.framework.webflux.security;

import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.webflux.security.core.model.SecurityUserDTO;

/**
 * @Author: van
 * @Date: 2022/1/12 15:10
 * @Description: TODO
 */
public interface IBtxWebfluxUserService<T extends SecurityUserDTO> {
    CommJSON<T> login(String... args);
}
