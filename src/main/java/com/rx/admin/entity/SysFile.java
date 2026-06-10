package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity {
    private String originalName;
    private String storedName;
    private String path;
    private Long size;
    private String mimeType;
    private String storageType;
    private String category;
    private Long uploader;
}