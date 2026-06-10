package com.rx.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysFile;
import com.rx.admin.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/sys/file")
@RequiredArgsConstructor
public class SysFileController {

    private final SysFileService fileService;

    @Value("${common-tools.upload.dir:./uploads}")
    private String uploadDir;

    @Operation(summary = "文件分页列表")
    @GetMapping("/page")
    public Result<PageResult<SysFile>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.ok(fileService.pageQuery(page, size, category, keyword));
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    @OperateLog(module = "文件管理", operation = "上传文件")
    public Result<SysFile> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(required = false) String category) throws IOException {
        long uploader = StpUtil.getLoginIdAsLong();
        SysFile sysFile = fileService.upload(file, category, uploader);
        return Result.ok(sysFile);
    }

    @Operation(summary = "下载/预览文件")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        SysFile sysFile = fileService.getById(id);
        if (sysFile == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = Paths.get(uploadDir, sysFile.getPath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String filename = URLDecoder.decode(sysFile.getOriginalName(), StandardCharsets.UTF_8);
        String contentType = sysFile.getMimeType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    @OperateLog(module = "文件管理", operation = "删除文件")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.deleteFile(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除文件")
    @DeleteMapping("/batch")
    @OperateLog(module = "文件管理", operation = "批量删除文件")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            fileService.deleteFile(id);
        }
        return Result.ok();
    }
}

