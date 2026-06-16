package com.rx.admin.modules.literature.shuihu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 水浒传诗词
 */
@Data
@TableName("shuihu_poems")
public class ShuihuPoem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 诗词标题 */
    private String title;

    /** 作者/出处 */
    private String author;

    /** 诗词内容 */
    private String content;

    /** 诗词类型（诗/词/曲/偈语/赞） */
    private String poemType;

    /** 相关人物 */
    private String relatedCharacter;

    /** 相关场景 */
    private String relatedScene;

    /** 赏析 */
    private String appreciation;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}