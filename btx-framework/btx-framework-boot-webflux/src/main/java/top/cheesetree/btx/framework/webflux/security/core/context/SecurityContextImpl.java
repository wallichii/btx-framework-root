package top.cheesetree.btx.framework.webflux.security.core.context;

import lombok.*;
import top.cheesetree.btx.framework.core.model.ValueObject;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationInfo;

/**
 * @author van
 * @date 2025/7/16 16:34
 * @description TODO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SecurityContextImpl implements ValueObject {
    private AuthenticationInfo authentication;
}
