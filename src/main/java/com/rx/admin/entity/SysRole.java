package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    private String description;
    private Integer sort;
    private Integer status;

    /** 数据权限范围: 1=全部 2=本部门 3=本部门及下级 4=仅本人 5=自定义 */
    private Integer dataScope;
    /** 自定义数据权限部门ID集合(逗号分隔) */
    private String dataDeptIds;

    /** 角色拥有的菜单ID列表（非数据库字段） */
    @TableField(exist = false)
    private List<Long> menuIds;
}
