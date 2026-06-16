package com.rx.admin.modules.system.dict.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 字典数据更新请求 */
@Data
public class DictDataUpdateDTO {
    @NotNull(message = "字典数据ID不能为空")
    private Long id;
    @NotNull(message = "字典类型不能为空")
    private Long typeId;
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;
    @NotBlank(message = "字典值不能为空")
    private String dictValue;
    private String cssClass;
    private String listClass;
    private Integer sort;
    private Integer status;
    private String remark;
}
