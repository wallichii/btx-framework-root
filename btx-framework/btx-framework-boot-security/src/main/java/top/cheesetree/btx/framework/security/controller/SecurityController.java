package top.cheesetree.btx.framework.security.controller;

import top.cheesetree.btx.framework.core.model.ValueObject;
import top.cheesetree.btx.framework.security.model.SecurityUserDTO;

/**
 * @Author: van
 * @Date: 2021/8/27 11:34
 * @Description: TODO
 */
public interface SecurityController<T extends SecurityUserDTO, A extends ValueObject> {
    String getUserId();

     T getUser();

     A getAuthInfo();
}
