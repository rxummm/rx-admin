package com.rx.admin.modules.content.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message_template")
public class SysMessageTemplate extends BaseEntity {
    private String name;
    private String code;
    private String titleTemplate;
    private String contentTemplate;
    private String channels;
    private Integer status;
}
