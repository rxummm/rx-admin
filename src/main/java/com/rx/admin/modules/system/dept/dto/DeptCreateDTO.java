package com.rx.admin.modules.system.dept.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 部门创建请求 */
@Data
public class DeptCreateDTO {
    private Long parentId;
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
}
