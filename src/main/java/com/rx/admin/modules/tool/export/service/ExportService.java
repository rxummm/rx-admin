package com.rx.admin.modules.tool.export.service;

import java.util.List;
import java.util.Map;

/**
 * 通用数据导出服务
 * <p>
 * 接收前端传来的表格列定义和数据，生成 Excel / PDF 文件。
 * 各种数据格式都统一为 List&lt;Map&gt;，不强依赖特定实体类。
 * </p>
 */
public interface ExportService {

    /**
     * 导出为 Excel（.xlsx）
     *
     * @param title   工作表标题（对应页面菜单名）
     * @param columns 列定义 [{field: "username", label: "用户名"}]
     * @param data    表格数据 [{username: "admin", nickname: "管理员"}]
     * @return Excel 文件字节数组
     */
    byte[] exportExcel(String title, List<Map<String, String>> columns, List<Map<String, Object>> data);

    /**
     * 导出为 PDF
     *
     * @param title   文档标题
     * @param columns 列定义
     * @param data    表格数据
     * @return PDF 文件字节数组
     */
    byte[] exportPdf(String title, List<Map<String, String>> columns, List<Map<String, Object>> data);

    /**
     * 根据菜单路径查询导出配置是否已启用
     *
     * @param menuPath 菜单路径，如 /system/user
     * @return 允许的导出类型列表，空列表表示未启用
     */
    List<String> getExportTypes(String menuPath);
}
