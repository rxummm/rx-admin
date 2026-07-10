package com.rx.admin.modules.system.i18n.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_i18n_translation")
public class SysI18nTranslation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long keyId;
    private String localeCode;
    private String translation;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
