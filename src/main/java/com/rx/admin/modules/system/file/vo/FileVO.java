package com.rx.admin.modules.system.file.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 文件视图对象 */
@Data
public class FileVO {
    private Long id;
    private String originalName;
    private String storedName;
    private String path;
    private Long size;
    private String mimeType;
    private String storageType;
    private String category;
    private Long uploader;
    private LocalDateTime createTime;
}
