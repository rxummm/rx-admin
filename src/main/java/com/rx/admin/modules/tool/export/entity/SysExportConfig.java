package com.rx.admin.modules.tool.export.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表格导出配置实体
 * <p>
 * 通过在此表中为菜单配置记录，即可在该菜单页面出现"导出"下拉按钮。
 * 无需修改页面代码（只需引入 ExportButton 组件并传入当前表格数据）。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_export_config")
public class SysExportConfig extends BaseEntity {

    /** 关联菜单ID（sys_menu.id） */
    private Long menuId;

    /** 允许的导出类型，逗号分隔，如 "excel,pdf" */
    private String exportTypes;

    /** 是否启用 1=启用 0=禁用 */
    private Integer enabled;
}