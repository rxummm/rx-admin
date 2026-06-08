package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("play_records")
public class PlayRecord extends BaseEntity {

    /** 歌曲ID */
    private Long songId;

    /** 歌曲标题 */
    private String songTitle;

    /** 播放用户 */
    private String username;

    /** 播放时长(秒) */
    private Integer playedSeconds;
}
