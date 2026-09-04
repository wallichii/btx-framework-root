package top.cheesetree.btx.framework.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import top.cheesetree.btx.framework.core.exception.ExceptionCodeUtil;
import top.cheesetree.btx.framework.core.model.ValueObject;

import java.util.List;

/**
 * @Author: van
 * @Date: 2021/8/27 10:52
 * @Description: TODO
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class SecurityUserDTO implements ValueObject {
    private String uid;
    private String hdUrl;
    private String loginName;
    private String name;
    private String phone;
    private String email;
    private List<? extends SecurityGroupDTO> depts;
    private List<? extends SecurityFuncDTO> funcs;
    private List<? extends SecurityRoleDTO> roles;

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SecurityUserDTO) {
            SecurityUserDTO gt = (SecurityUserDTO) obj;
            if (ExceptionCodeUtil.isNotBlank(this.uid)) {
                return this.uid.equals(gt.getUid());
            } else if (ExceptionCodeUtil.isNotBlank(gt.getUid())) {
                return gt.getUid().equals(this.uid);
            } else {
                return true;
            }
        } else {
            return super.equals(obj);
        }
    }

}
