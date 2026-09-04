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
public class SecurityMenuDTO implements ValueObject {
    private String menuId;
    private String menuName;
    private String menuUrl;
    private String menuDesc;
    private String menuLevel;
    private String authLevel;
    private String target;
    private String note;
    private String menuIcon;
    private String pMenuId;
    private String param1;
    private String param2;
}
