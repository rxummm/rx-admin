package com.rx.admin.modules.tool.commonTools.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SharedFileVO {
    private Long id;
    private String fileName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String uploadUser;
    private LocalDateTime uploadTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}