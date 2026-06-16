package com.rx.admin.modules.audio.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audio_transcription")
public class AudioTranscription extends BaseEntity {
    private String fileName;
    private String filePath;
    private String language;
    private String fullText;
    private Double duration;
    private String modelName;
    private Float accuracy;
    private Integer status;
    private String errorMessage;
}
