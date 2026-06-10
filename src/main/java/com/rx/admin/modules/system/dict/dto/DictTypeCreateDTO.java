package com.rx.admin.modules.system.dict.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 字典类型创建请求 */
@Data
public class DictTypeCreateDTO {
    @NotBlank(message = "字典名称不能为空")
    private String dictName;
    @NotBlank(message = "字典类型不能为空")
    private String dictType;
    private Integer status;
    private String remark;
}
