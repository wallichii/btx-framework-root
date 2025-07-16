package top.cheesetree.btx.framework.webflux.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import top.cheesetree.btx.framework.security.model.SecurityAuthUserDTO;
import top.cheesetree.btx.framework.security.model.SecurityUserDTO;

/**
 * @author van
 * @date 2022/3/25 19:54
 * @description TODO
 */
@NoArgsConstructor
@Getter
@Setter
public class WebfluxSecurityAuthUserDTO<U extends SecurityUserDTO, T extends WebfluxAuthTokenInfo> extends SecurityAuthUserDTO<U, T> {
}
