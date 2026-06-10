package com.rx.admin.modules.system.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 VO - 返回给前端的数据视图
 * 不包含 password 等敏感字段
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer gender;
    private Integer status;
    private Long deptId;
    private String deptName;
    private List<Long> roleIds;
    private List<String> roleNames;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
