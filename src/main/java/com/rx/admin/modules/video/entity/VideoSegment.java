package com.rx.admin.modules.video.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video_segment")
public class VideoSegment extends BaseEntity {
    private Long transcriptionId;
    private Double startTime;
    private Double endTime;
    private String text;
    private String speakerLabel;
    private String speakerName;
    private Float confidence;
}