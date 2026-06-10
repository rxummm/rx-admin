package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 技术博客文章
 * 数据来源: nicklitten / faq400 / rpgpgm / as400sql / apimy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tech_blog_article")
public class TechBlogArticle extends BaseEntity {
    /** 文章标题 */
    private String title;
    /** 原始页面URL slug */
    private String slug;
    /** 原始文章链接 */
    private String sourceUrl;
    /** 作者 */
    private String author;
    /** 发布日期 */
    private String publishDate;
    /** 分类标签(逗号分隔) */
    private String categories;
    /** 摘要 */
    @TableField("excerpt_text")
    private String excerptText;
    /** HTML正文内容 */
    private String contentHtml;
    /** 纯文本正文(用于搜索) */
    private String contentText;
    /** 封面图URL */
    private String coverImage;
    /** 排序 */
    private Integer sort;
    /** 浏览次数 */
    private Integer viewCount;
    /** 博客来源标识: nicklitten / faq400 / rpgpgm / as400sql / apimy */
    private String source;
}
