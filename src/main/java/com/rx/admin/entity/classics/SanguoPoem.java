package com.rx.admin.entity.classics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 三国演义诗词
 */
@Data
@TableName("sanguo_poems")
public class SanguoPoem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 诗词标题 */
    private String title;

    /** 作者/出处 */
    private String author;

    /** 朝代 */
    private String dynasty;

    /** 诗词内容 */
    private String content;

    /** 翻译/注释 */
    private String translation;

    /** 赏析 */
    private String appreciation;

    /** 章节 */
    private String chapter;

    /** 分类 */
    private String category;

    /** 相关人物 */
    private String relatedCharacter;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
