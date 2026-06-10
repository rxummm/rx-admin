package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysExportLog;
import com.rx.admin.service.ExportLogService;
import com.rx.admin.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "数据导出")
@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;
    private final ExportLogService exportLogService;
    private final HttpServletRequest request;

    public ExportController(ExportService exportService, ExportLogService exportLogService, HttpServletRequest request) {
        this.exportService = exportService;
        this.exportLogService = exportLogService;
        this.request = request;
    }

    /**
     * 查询指定菜单页面是否启用了导出功能，以及支持哪些导出类型
     *
     * @param path 菜单路径，如 /system/user
     */
    @Operation(summary = "查询导出配置")
    @GetMapping("/config")
    @SaCheckLogin
    public Result<Map<String, Object>> getConfig(@RequestParam String path) {
        List<String> types = exportService.getExportTypes(path);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", !types.isEmpty());
        data.put("exportTypes", types);
        return Result.ok(data);
    }

    /**
     * 导出 Excel
     * <p>接收前端传来的列定义 + 表格数据，生成 .xlsx 文件并返回下载流</p>
     */
    @Operation(summary = "导出Excel")
    @PostMapping("/excel")
    @SaCheckLogin
    public void exportExcel(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String title = safeString(body.get("title"), "数据导出");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> columns = (List<Map<String, String>>) body.get("columns");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("列定义不能为空");
        }

        byte[] bytes = exportService.exportExcel(title, columns, data != null ? data : List.of());
        String fileName = title + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        // 记录导出审计日志
        recordExportLog("excel", title, data != null ? data.size() : 0, fileName);
        writeFileResponse(response, bytes, fileName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    /**
     * 导出 PDF
     * <p>接收前端传来的列定义 + 表格数据，生成 .pdf 文件并返回下载流</p>
     */
    @Operation(summary = "导出PDF")
    @PostMapping("/pdf")
    @SaCheckLogin
    public void exportPdf(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String title = safeString(body.get("title"), "数据导出");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> columns = (List<Map<String, String>>) body.get("columns");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("列定义不能为空");
        }

        byte[] bytes = exportService.exportPdf(title, columns, data != null ? data : List.of());
        String fileName = title + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".pdf";
        // 记录导出审计日志
        recordExportLog("pdf", title, data != null ? data.size() : 0, fileName);
        writeFileResponse(response, bytes, fileName, MediaType.APPLICATION_PDF_VALUE);
    }

    // ─── 工具方法 ───

    private void writeFileResponse(HttpServletResponse response, byte[] bytes, String fileName, String contentType) {
        try {
            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");
            response.setContentLength(bytes.length);
            try (OutputStream os = response.getOutputStream()) {
                os.write(bytes);
                os.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("文件写出失败: " + e.getMessage(), e);
        }
    }

    private void recordExportLog(String exportType, String title, int recordCount, String fileName) {
        try {
            SysExportLog log = new SysExportLog();
            log.setUserId(StpUtil.getLoginIdAsLong());
            log.setUsername("user_" + StpUtil.getLoginIdAsLong());
            log.setExportType(exportType);
            log.setExportTitle(title);
            log.setRecordCount(recordCount);
            log.setFileName(fileName);
            log.setIp(request.getRemoteAddr());
            log.setCreateTime(LocalDateTime.now());
            exportLogService.save(log);
        } catch (Exception ignored) {
            // 审计日志记录失败不影响导出
        }
    }

    private String safeString(Object obj, String defaultVal) {
        return obj != null && !obj.toString().isBlank() ? obj.toString().trim() : defaultVal;
    }
}
