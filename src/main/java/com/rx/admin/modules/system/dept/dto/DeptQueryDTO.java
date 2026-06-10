package com.rx.admin.modules.system.dept.dto;

import lombok.Data;

/** 部门查询参数 */
@Data
public class DeptQueryDTO {
    private String deptName;
    private Integer status;
}
