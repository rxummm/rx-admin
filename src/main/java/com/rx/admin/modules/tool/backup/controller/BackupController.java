package com.rx.admin.modules.tool.backup.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Tag(name = "数据库备份与恢复")
@RestController
@RequestMapping("/api/tool/backup")
public class BackupController {

    @Value("${spring.datasource.primary.url:}")
    private String dbUrl;
    @Value("${spring.datasource.primary.username:}")
    private String dbUser;
    @Value("${spring.datasource.primary.password:}")
    private String dbPass;

    private static final String BACKUP_DIR = "backups";

    @Operation(summary = "查询备份文件列表")
    @GetMapping("/list")
    @SaCheckPermission("tool:backup:list")
    public Result<List<Map<String, Object>>> list() {
        File dir = new File(BACKUP_DIR);
        List<Map<String, Object>> files = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            File[] list = dir.listFiles((d, name) -> name.endsWith(".sql"));
            if (list != null) {
                for (File f : list) {
                    files.add(Map.of(
                        "name", f.getName(),
                        "size", f.length(),
                        "sizeDisplay", formatSize(f.length()),
                        "createTime", new Date(f.lastModified()).toString()
                    ));
                }
            }
        }
        return Result.ok(files);
    }

    @Operation(summary = "创建数据库备份")
    @PostMapping("/create")
    @SaCheckPermission("tool:backup:list")
    public Result<Map<String, String>> create() {
        try {
            new File(BACKUP_DIR).mkdirs();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "backup_rx_admin_" + timestamp + ".sql";
            String filepath = BACKUP_DIR + "/" + filename;

            // Extract db name from URL
            String dbName = "rx_admin";
            if (dbUrl != null && dbUrl.contains("/")) {
                String[] parts = dbUrl.split("/");
                String last = parts[parts.length - 1];
                if (last.contains("?")) last = last.substring(0, last.indexOf("?"));
                dbName = last;
            }

            ProcessBuilder pb = new ProcessBuilder(
                "mysqldump", "-u" + dbUser, "-p" + dbPass, dbName
            );
            pb.redirectOutput(new File(filepath));
            pb.redirectError(new File(BACKUP_DIR + "/dump_error.log"));
            Process p = pb.start();
            p.waitFor();

            return Result.ok(Map.of("filename", filename, "message", "备份成功"));
        } catch (Exception e) {
            log.error("备份失败", e);
            return Result.fail("备份失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除备份文件")
    @DeleteMapping("/{filename}")
    @SaCheckPermission("tool:backup:list")
    public Result<Void> delete(@PathVariable String filename) {
        try {
            Files.deleteIfExists(Path.of(BACKUP_DIR, filename));
            return Result.ok();
        } catch (IOException e) {
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    @Operation(summary = "下载备份文件")
    @GetMapping("/download/{filename}")
    public void download(@PathVariable String filename, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        File file = new File(BACKUP_DIR, filename);
        if (!file.exists()) { response.sendError(404); return; }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        Files.copy(file.toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024*1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
