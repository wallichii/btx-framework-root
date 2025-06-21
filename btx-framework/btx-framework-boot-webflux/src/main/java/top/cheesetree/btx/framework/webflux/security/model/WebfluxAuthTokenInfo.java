package top.cheesetree.btx.framework.webflux.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import top.cheesetree.btx.framework.core.model.ValueObject;

/**
 * @author van
 * @date 2022/2/18 08:58
 * @description TODO
 */
@Getter
@Setter
@NoArgsConstructor
public class WebfluxAuthTokenInfo implements ValueObject {
    private String accessToken;
    private String refreshToken;
}
