package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shared_files")
public class SharedFile extends BaseEntity {

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 存储文件名(UUID)
     */
    private String storedName;

    /**
     * 存储路径
     */
    private String filePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件类型/扩展名
     */
    private String fileType;

    /**
     * 上传用户
     */
    private String uploadUser;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
}
