package com.rx.admin.modules.system.dict.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 字典数据视图对象 */
@Data
public class DictDataVO {
    private Long id;
    private Long typeId;
    private String dictLabel;
    private String dictValue;
    private String cssClass;
    private String listClass;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
