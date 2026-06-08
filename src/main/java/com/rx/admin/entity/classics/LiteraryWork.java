package com.rx.admin.entity.classics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("literary_work")
public class LiteraryWork {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作品标题 */
    private String title;

    /** 副标题 */
    private String subtitle;

    /** 作者ID（外键关联 author.id） */
    private Long authorId;

    /** 朝代ID（外键关联 dynasty.id） */
    private Long dynastyId;

    /** 体裁ID（外键关联 genre.id） */
    private Long genreId;

    /** 体裁编码 */
    private String genreCode;

    /** 正文 */
    private String content;

    /** 正文HTML */
    private String contentHtml;

    /** 前言 */
    private String preface;

    /** 后记 */
    private String epilogue;

    /** 注释（JSON） */
    private String annotations;

    /** 赏析 */
    private String appreciation;

    /** 译文 */
    private String translation;

    /** 关键词 */
    private String keywords;

    /** 标签 */
    private String tags;

    /** 难度等级 */
    private Integer difficultyLevel;

    /** 字数 */
    private Integer wordCount;

    /** 来源 */
    private String source;

    /** 封面URL */
    private String coverUrl;

    /** 摘要 */
    private String summary;

    /** 是否精选 */
    private Integer isFeatured;

    /** 浏览次数 */
    private Integer viewCount;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0禁用 1启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ====== 关联查询字段（非数据库字段） ======

    /** 作者姓名 */
    @TableField(exist = false)
    private String authorName;

    /** 朝代名称 */
    @TableField(exist = false)
    private String dynastyName;

    /** 体裁名称 */
    @TableField(exist = false)
    private String genreName;
}
