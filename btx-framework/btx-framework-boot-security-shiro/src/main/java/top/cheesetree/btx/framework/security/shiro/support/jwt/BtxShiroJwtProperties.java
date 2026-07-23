package top.cheesetree.btx.framework.security.shiro.support.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.cheesetree.btx.framework.security.shiro.constants.BtxSecurityShiroEnum;

/**
 * @author van
 * @date 2022/2/21 10:43
 * @description TODO
 */
@ConfigurationProperties(prefix = "btx.security.shiro.jwt", ignoreUnknownFields = false)
@Getter
@Setter
public class BtxShiroJwtProperties {
    private String secretKey;
    private String subKey = "sub";
    private String devUserName;
    private BtxSecurityShiroEnum.JWT_ALGORITHM_TYPE algorithmName = BtxSecurityShiroEnum.JWT_ALGORITHM_TYPE.HS256;

}
