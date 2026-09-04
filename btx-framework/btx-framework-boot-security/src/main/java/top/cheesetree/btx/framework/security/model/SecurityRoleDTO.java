package top.cheesetree.btx.framework.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import top.cheesetree.btx.framework.core.model.ValueObject;

/**
 * @author hanxulin
 */
@Getter
@Setter
@NoArgsConstructor
public  class SecurityRoleDTO implements ValueObject {

    private String roleId;
    private String roleCode;
    private String roleName;
    private String roleType;
    private String roleDesc;
    private String groupId;
}
