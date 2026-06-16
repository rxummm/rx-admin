package com.rx.admin.modules.tool.commonTools.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.commonTools.entity.SharedFile;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.tool.commonTools.service.CommonToolsService;
import com.rx.admin.modules.tool.commonTools.service.EmailService;
import com.rx.admin.modules.tool.commonTools.convert.SharedFileConvert;
import com.rx.admin.modules.tool.commonTools.vo.SharedFileVO;
import com.rx.admin.modules.system.user.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Tag(name = "办公工具")
@RestController
@RequestMapping("/api/common-tools")
public class CommonToolsController {

    private final CommonToolsService commonToolsService;
    private final EmailService emailService;
    private final ISysUserService sysUserService;
    private final SharedFileConvert sharedFileConvert;

    @Value("${common-tools.upload.dir:D:/vueprojects/RX/ui/public/shareddocs}")
    private String uploadDir;

    public CommonToolsController(CommonToolsService commonToolsService, EmailService emailService,
                                  ISysUserService sysUserService, SharedFileConvert sharedFileConvert) {
        this.commonToolsService = commonToolsService;
        this.emailService = emailService;
        this.sysUserService = sysUserService;
        this.sharedFileConvert = sharedFileConvert;
    }

    @Operation(summary = "Excel解析 - 上传并解析Excel文件")
    @PostMapping("/excel/parse")
    @SaCheckLogin
    public Result<Map<String, Object>> parseExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<Map<String, Object>> data = commonToolsService.parseExcel(file);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("columns", data.isEmpty() ? List.of() : new ArrayList<>(data.get(0).keySet()));
            result.put("rows", data);
            result.put("total", data.size());
            return Result.ok(result);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("解析失败：" + e.getMessage());
        }
    }

    @Operation(summary = "文档上传 - 上传文档到指定目录")
    @PostMapping("/document/upload")
    @SaCheckLogin
    public Result<SharedFileVO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "targetDir", required = false) String targetDir) {
        try {
            SharedFile sf = commonToolsService.uploadDocument(file, targetDir);
            return Result.ok("上传成功", sharedFileConvert.toVO(sf));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "文档列表 - 分页查询已上传文件")
    @GetMapping("/document/list")
    @SaCheckLogin
    public Result<Map<String, Object>> getDocumentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<SharedFile> result = commonToolsService.getUploadedFiles(page, size, keyword);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", sharedFileConvert.toVOList(result.getRecords()));
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.ok(data);
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/document/{id}")
    @SaCheckLogin
    public Result<?> deleteDocument(@PathVariable Long id) {
        boolean ok = commonToolsService.deleteFile(id);
        return ok ? Result.ok() : Result.fail("文件不存在或删除失败");
    }

    @Operation(summary = "获取默认存储路径")
    @GetMapping("/document/default-dir")
    @SaCheckLogin
    public Result<Map<String, String>> getDefaultDir() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("defaultDir", commonToolsService.getDefaultUploadDir());
        return Result.ok(data);
    }

    @Operation(summary = "文档转换 - PDF转Word")
    @PostMapping("/convert/pdf-to-word")
    @SaCheckLogin
    public Result<Map<String, String>> convertPdfToWord(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "outputDir", required = false) String outputDir) {
        try {
            String path = commonToolsService.convertPdfToWord(file, outputDir);
            Map<String, String> data = new LinkedHashMap<>();
            data.put("outputPath", path);
            data.put("message", "PDF转Word成功");
            return Result.ok(data);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("转换失败：" + e.getMessage());
        }
    }

    @Operation(summary = "文档转换 - Word转PDF")
    @PostMapping("/convert/word-to-pdf")
    @SaCheckLogin
    public Result<Map<String, String>> convertWordToPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "outputDir", required = false) String outputDir) {
        try {
            String path = commonToolsService.convertWordToPdf(file, outputDir);
            Map<String, String> data = new LinkedHashMap<>();
            data.put("outputPath", path);
            data.put("message", "Word转PDF成功");
            return Result.ok(data);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("转换失败：" + e.getMessage());
        }
    }

    @Operation(summary = "发送邮件")
    @PostMapping("/email/send")
    @SaCheckLogin
    public Result<Map<String, String>> sendEmail(
            @RequestParam("to") String to,
            @RequestParam(value = "cc", required = false) String cc,
            @RequestParam(value = "bcc", required = false) String bcc,
            @RequestParam("subject") String subject,
            @RequestParam("content") String content,
            @RequestParam(defaultValue = "false") boolean isHtml,
            @RequestParam(value = "attachmentPaths", required = false) String attachmentPaths) {
        try {
            List<String> toList = parseEmails(to);
            List<String> ccList = parseEmails(cc);
            List<String> bccList = parseEmails(bcc);
            List<File> attachments = new ArrayList<>();
            if (attachmentPaths != null && !attachmentPaths.isBlank()) {
                for (String p : attachmentPaths.split(",")) {
                    p = p.trim();
                    if (!p.isEmpty()) {
                        File f = new File(p);
                        if (f.exists()) attachments.add(f);
                    }
                }
            }
            String userEmail = getCurrentUserEmail();
            emailService.send(userEmail, toList, ccList, bccList, subject, content, isHtml, attachments);
            Map<String, String> data = new LinkedHashMap<>();
            data.put("message", "邮件发送成功");
            return Result.ok(data);
        } catch (Exception e) {
            return Result.fail("发送失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传邮件附件")
    @PostMapping("/email/upload-attachment")
    @SaCheckLogin
    public Result<Map<String, String>> uploadAttachment(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.fail("文件为空");
            }
            Path emailDir = Paths.get(uploadDir, "email-attachments");
            if (!Files.exists(emailDir)) {
                Files.createDirectories(emailDir);
            }
            String originalName = file.getOriginalFilename();
            String storedName = UUID.randomUUID().toString() + "_" + (originalName != null ? originalName : "attachment");
            Path targetPath = emailDir.resolve(storedName);
            file.transferTo(targetPath.toFile());

            Map<String, String> data = new LinkedHashMap<>();
            data.put("fileName", originalName);
            data.put("filePath", targetPath.toAbsolutePath().toString());
            data.put("fileSize", String.valueOf(file.getSize()));
            return Result.ok(data);
        } catch (IOException e) {
            return Result.fail("附件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取邮件配置")
    @GetMapping("/email/config")
    @SaCheckLogin
    public Result<Map<String, String>> getEmailConfig() {
        String userEmail = getCurrentUserEmail();
        return Result.ok(emailService.getMailConfig(userEmail));
    }

    private List<String> parseEmails(String emails) {
        if (emails == null || emails.isBlank()) return List.of();
        return Arrays.stream(emails.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String getCurrentUserEmail() {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            SysUser user = sysUserService.getById(userId);
            if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                return user.getEmail().trim();
            }
        } catch (Exception e) {
            log.warn("获取当前用户邮箱失败: {}", e.getMessage());
        }
        return null;
    }
}
