package top.cheesetree.btx.framework.security;

import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.core.model.ValueObject;
import top.cheesetree.btx.framework.security.model.SecurityAuthUserDTO;
import top.cheesetree.btx.framework.security.model.SecurityUserDTO;

/**
 * @Author: van
 * @Date: 2022/1/13 09:21
 * @Description: TODO
 */
public interface IBtxSecurityOperation<T extends SecurityUserDTO, A extends ValueObject> {

    CommJSON<? extends SecurityAuthUserDTO> login(String... args);

    CommJSON<Object> logout();

    String getUserId();

    T getUserInfo();

    A getAuthInfo();

    CommJSON<Object> runas(T user);
}
