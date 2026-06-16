package com.rx.admin.modules.video.player.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video_file")
public class VideoFile extends BaseEntity {

    /** 视频标题 */
    private String title;

    /** 文件名 */
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 时长(秒) */
    private Integer duration;

    /** 分辨率宽 */
    private Integer width;

    /** 分辨率高 */
    private Integer height;

    /** 视频格式(mp4/webm/ogg/mkv/avi/flv/mov) */
    private String videoType;

    /** 播放次数 */
    private Integer playCount;

    /** 最后播放时间 */
    private LocalDateTime lastPlayTime;
}
