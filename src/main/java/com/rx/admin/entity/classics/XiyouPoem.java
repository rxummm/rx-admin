package com.rx.admin.entity.classics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 西游记诗词
 */
@Data
@TableName("xiyou_poems")
public class XiyouPoem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 诗词标题 */
    private String title;

    /** 作者/出处 */
    private String author;

    /** 诗词类型 */
    private String poemType;

    /** 诗词内容 */
    private String content;

    /** 相关人物 */
    private String relatedCharacter;

    /** 相关场景/章节 */
    private String relatedScene;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
