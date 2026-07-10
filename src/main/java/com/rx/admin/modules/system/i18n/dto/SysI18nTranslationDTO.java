package com.rx.admin.modules.system.i18n.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysI18nTranslationDTO {
    @NotNull(message = "翻译键ID不能为空")
    private Long keyId;
    @NotBlank(message = "语言代码不能为空")
    private String localeCode;
    @NotBlank(message = "翻译内容不能为空")
    private String translation;
}
