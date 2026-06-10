package com.rx.admin.modules.system.dept.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/** 部门视图对象 */
@Data
public class DeptVO {
    private Long id;
    private Long parentId;
    private String deptName;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<DeptVO> children;
}
