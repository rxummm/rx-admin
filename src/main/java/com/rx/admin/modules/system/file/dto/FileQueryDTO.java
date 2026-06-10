package com.rx.admin.modules.system.file.dto;

import lombok.Data;

/** 文件查询参数 */
@Data
public class FileQueryDTO {
    private String originalName;
    private String category;
    private String storageType;
    private Integer page = 1;
    private Integer pageSize = 10;
}
