package com.rx.admin.modules.audio.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audio_segment")
public class AudioSegment extends BaseEntity {
    private Long transcriptionId;
    private Double startTime;
    private Double endTime;
    private String text;
    private Float confidence;
}
