package com.rx.admin.entity.classics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 水浒传章节
 */
@Data
@TableName("shuihu_chapters")
public class ShuihuChapter {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 章节编号 */
    private Integer chapterNumber;

    /** 章节标题 */
    private String chapterTitle;

    /** 章节副标题 */
    private String chapterSubtitle;

    /** 章节内容 */
    private String chapterContent;

    /** 精彩看点 */
    private String highlights;

    /** 出场人物 */
    private String characters;

    /** 涉及地点 */
    private String locations;

    /** 主题 */
    private String themes;

    /** 关键词 */
    private String keywords;

    /** 阅读难度 */
    private String readingDifficulty;

    /** 预计阅读时间(分钟) */
    private Integer estimatedReadingTime;

    /** 状态 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
