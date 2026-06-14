package com.rx.admin.modules.tool.commonTools.dto;

import lombok.Data;

@Data
public class SharedFileCreateDTO {
    private String fileName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String uploadUser;
}