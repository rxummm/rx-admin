package com.rx.admin.modules.system.dept.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 部门更新请求 */
@Data
public class DeptUpdateDTO {
    @NotNull(message = "部门ID不能为空")
    private Long id;
    private Long parentId;
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
}
