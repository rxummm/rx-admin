package com.rx.admin.modules.system.dict.dto;

import lombok.Data;

/** 字典类型查询参数 */
@Data
public class DictTypeQueryDTO {
    private String dictName;
    private String dictType;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
