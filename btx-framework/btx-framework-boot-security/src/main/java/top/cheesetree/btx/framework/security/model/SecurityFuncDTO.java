package top.cheesetree.btx.framework.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import top.cheesetree.btx.framework.core.model.ValueObject;

/**
 * @Author: van
 * @Date: 2021/8/27 13:48
 * @Description: TODO
 */
@Getter
@Setter
@NoArgsConstructor
public class SecurityFuncDTO implements ValueObject {
    private String funcCode;
    private String funcName;
    private String funcDesc;
    private String note;
    private String actionLink;
}
