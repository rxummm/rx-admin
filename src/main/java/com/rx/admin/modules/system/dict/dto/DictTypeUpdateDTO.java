package com.rx.admin.modules.system.dict.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 字典类型更新请求 */
@Data
public class DictTypeUpdateDTO {
    @NotNull(message = "字典类型ID不能为空")
    private Long id;
    @NotBlank(message = "字典名称不能为空")
    private String dictName;
    @NotBlank(message = "字典类型不能为空")
    private String dictType;
    private Integer status;
    private String remark;
}
