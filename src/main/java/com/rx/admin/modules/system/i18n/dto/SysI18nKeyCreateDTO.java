package com.rx.admin.modules.system.i18n.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysI18nKeyCreateDTO {
    @NotBlank(message = "翻译键不能为空")
    private String keyPath;
    private String module;
    private String description;
}
