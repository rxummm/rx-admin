package com.rx.admin.modules.system.i18n.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_i18n_locale")
public class SysI18nLocale extends BaseEntity {
    private String code;
    private String name;
    private String nativeName;
    private Integer isDefault;
    private Integer status;
}
