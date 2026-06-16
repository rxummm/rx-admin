package com.rx.admin.modules.video.player.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video_play_record")
public class VideoPlayRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 视频ID */
    private Long videoId;

    /** 视频标题(冗余) */
    private String videoTitle;

    /** 播放用户 */
    private String username;

    /** 播放时长(秒) */
    private Integer playedSeconds;

    /** 创建时间 */
    private LocalDateTime createTime;
}
