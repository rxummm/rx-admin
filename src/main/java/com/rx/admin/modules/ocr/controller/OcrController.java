package com.rx.admin.modules.ocr.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.config.AppConfig;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.common.util.FileUploadUtils;
import com.rx.admin.modules.ocr.dto.OcrQueryDTO;
import com.rx.admin.modules.ocr.service.IOcrService;
import com.rx.admin.modules.ocr.vo.OcrRecognitionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Tag(name = "OCR文档识别")
@RestController
@ApiVersion(1)
@RequestMapping("/ocr")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OcrController {

    private final IOcrService ocrService;
    private final AppConfig appConfig;

    private static final List<String> SUPPORTED_EXTENSIONS = List.of(
        ".pdf", ".docx", ".doc", ".xls", ".xlsx",
        ".png", ".jpg", ".jpeg", ".bmp", ".tiff", ".tif", ".gif",
        ".txt", ".md", ".html", ".htm", ".xml", ".csv", ".json", ".yaml", ".yml",
        ".sql", ".java", ".js", ".ts", ".py", ".go", ".css", ".scss", ".sh", ".log"
    );

    @Operation(summary = "上传文件并OCR识别")
    @PostMapping("/recognize")
    @SaCheckPermission("ocr:recognition:recognize")
    @OperateLog(module = "OCR识别", operation = "上传识别")
    public Result<OcrRecognitionVO> recognize(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", required = false) String language) throws IOException {

        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "unknown";

        if (!FileUploadUtils.isValidFile(originalName, SUPPORTED_EXTENSIONS)) {
            return Result.fail("不支持的文件格式，支持: pdf/docx/png/jpg/jpeg/bmp/tiff/gif");
        }

        Path tempDir = getTempDir();
        Path tempFile = FileUploadUtils.saveTempFile(file, tempDir);

        try {
            OcrRecognitionVO result = ocrService.recognize(tempFile.toFile(), originalName, language);
            return Result.ok(result);
        } finally {
            FileUploadUtils.cleanupTempFile(tempFile);
        }
    }

    @Operation(summary = "分页查询识别记录")
    @GetMapping("/page")
    @SaCheckPermission("ocr:recognition:list")
    public Result<PageResult<OcrRecognitionVO>> page(OcrQueryDTO query) {
        return Result.ok(ocrService.pageQuery(query));
    }

    @Operation(summary = "获取识别详情")
    @GetMapping("/{id}")
    @SaCheckPermission("ocr:recognition:view")
    public Result<OcrRecognitionVO> getById(@PathVariable Long id) {
        OcrRecognitionVO result = ocrService.getById(id);
        if (result == null) {
            return Result.fail("记录不存在");
        }
        return Result.ok(result);
    }

    @Operation(summary = "下载识别结果")
    @GetMapping("/{id}/download")
    @SaCheckPermission("ocr:recognition:view")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        OcrRecognitionVO vo = ocrService.getById(id);
        if (vo == null) {
            return ResponseEntity.notFound().build();
        }

        String content = ocrService.generateResultTxt(id);
        if (content == null || content.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String fileName = vo.getFileName().replaceAll("\\.[^.]+$", "") + "_ocr.txt";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @Operation(summary = "删除识别记录")
    @DeleteMapping("/{id}")
    @SaCheckPermission("ocr:recognition:delete")
    @OperateLog(module = "OCR识别", operation = "删除记录")
    public Result<Void> deleteById(@PathVariable Long id) {
        ocrService.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除识别记录")
    @DeleteMapping("/batch/{ids}")
    @SaCheckPermission("ocr:recognition:delete")
    @OperateLog(module = "OCR识别", operation = "批量删除")
    public Result<Void> deleteBatch(@PathVariable String ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("请选择要删除的记录");
        }
        List<Long> idList = java.util.Arrays.stream(ids.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::valueOf)
            .toList();
        if (idList.isEmpty()) {
            return Result.fail("请选择要删除的记录");
        }
        ocrService.deleteBatch(idList);
        return Result.ok();
    }

    private Path getTempDir() {
        String configuredDir = appConfig.getOcr() != null ? appConfig.getOcr().getTempDir() : null;
        if (configuredDir != null && !configuredDir.isEmpty()) {
            return Path.of(configuredDir);
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "rx-ocr");
    }
}
