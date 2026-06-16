package com.rx.admin.modules.video.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video_transcription")
public class VideoTranscription extends BaseEntity {
    private String fileName;
    private String filePath;
    private String audioPath;
    private String language;
    private String fullText;
    private Double duration;
    private String modelName;
    private Integer speakerCount;
    private Integer status;
    private String srtPath;
    private String assPath;
    private String errorMessage;
}