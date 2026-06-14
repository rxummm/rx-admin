package com.rx.admin.modules.as400.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class IServiceCategoryVO {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String description;
    private String docUrl;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<IServiceItemVO> items;
}