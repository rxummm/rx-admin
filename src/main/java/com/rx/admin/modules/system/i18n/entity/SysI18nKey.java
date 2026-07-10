package com.rx.admin.modules.system.i18n.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_i18n_key")
public class SysI18nKey extends BaseEntity {
    private String keyPath;
    private String module;
    private String description;
}
