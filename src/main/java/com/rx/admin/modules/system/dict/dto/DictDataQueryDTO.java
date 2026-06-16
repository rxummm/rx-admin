package com.rx.admin.modules.system.dict.dto;

import lombok.Data;

/** 字典数据查询参数 */
@Data
public class DictDataQueryDTO {
    private Long typeId;
    private String dictLabel;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
