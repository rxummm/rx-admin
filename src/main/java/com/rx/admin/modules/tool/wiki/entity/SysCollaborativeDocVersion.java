package com.rx.admin.modules.tool.wiki.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 协作文档版本实体
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_collaborative_doc_version")
public class SysCollaborativeDocVersion extends BaseEntity {
    /** 文档ID */
    private Long docId;
    
    /** 版本号 */
    private Integer versionNumber;
    
    /** 版本内容 */
    private String content;
    
    /** 版本标题 */
    private String title;
    
    /** 编辑者ID */
    private Long editorId;
    
    /** 编辑者姓名 */
    private String editorName;
    
    /** 版本说明 */
    private String changeNote;
}
