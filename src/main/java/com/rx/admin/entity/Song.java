package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("songs")
public class Song extends BaseEntity {

    /** 歌曲文件名(不含扩展名) */
    private String title;

    /** 艺术家 */
    private String artist;

    /** 专辑 */
    private String album;

    /** MP3文件路径 */
    private String mp3Path;

    /** LRC歌词文件路径(可能为空) */
    private String lrcPath;

    /** 歌词内容(从LRC读取缓存) */
    private String lrcContent;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 时长(秒) */
    private Integer duration;

    /** 播放次数 */
    private Integer playCount;
}
