package top.cheesetree.btx.framework.webflux.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import top.cheesetree.btx.framework.core.model.ValueObject;

/**
 * @author van
 * @date 2022/2/18 08:34
 * @description TODO
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class SecurityAuthUserDTO<U extends SecurityUserDTO, T> implements ValueObject {
    private U user;
    private T authinfo;
}
