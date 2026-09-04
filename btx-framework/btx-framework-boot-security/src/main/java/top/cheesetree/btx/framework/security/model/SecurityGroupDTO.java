package top.cheesetree.btx.framework.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import top.cheesetree.btx.framework.core.exception.ExceptionCodeUtil;
import top.cheesetree.btx.framework.core.model.ValueObject;

/**
 * @Author: van
 * @Date: 2021/8/27 13:48
 * @Description: TODO
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class SecurityGroupDTO implements ValueObject {
    private String groupId;
    private String pGroupId;
    private String groupCode; // 机构编码
    private String groupName; // 机构名称
    private String groupPath; // 机构路径
    private String groupType;
    private String groupSort;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SecurityGroupDTO) {
            SecurityGroupDTO gt = (SecurityGroupDTO) obj;
            if (ExceptionCodeUtil.isNotBlank(this.groupId)) {
                return this.groupId.equals(gt.getGroupId());
            } else if (ExceptionCodeUtil.isNotBlank(gt.getGroupId())) {
                return gt.getGroupId().equals(this.groupId);
            } else {
                return true;
            }
        } else {
            return super.equals(obj);
        }
    }
}
