package com.rx.admin.modules.literature.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("author")
public class Author {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者姓名 */
    private String name;

    /** 字 */
    private String courtesyName;

    /** 号/别名 */
    private String pseudonym;

    /** 朝代ID（外键关联 dynasty.id） */
    private Long dynastyId;

    /** 出生年份 */
    private Integer birthYear;

    /** 卒年年份 */
    private Integer deathYear;

    /** 出生地 */
    private String birthplace;

    /** 生平简介 */
    private String biography;

    /** 头像图片URL */
    private String avatarUrl;

    /** 代表作品 */
    private String representativeWorks;

    /** 成就 */
    private String achievement;

    /** 作者类型 */
    private String authorType;

    /** 标签 */
    private String tags;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0禁用 1启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}