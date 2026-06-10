package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysMessageTemplate;
import com.rx.admin.entity.SysNotifyRecord;
import com.rx.admin.service.MessageTemplateService;
import com.rx.admin.service.NotifyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "通知中心")
@RestController
@RequestMapping("/api/notify-center")
public class NotifyCenterController {

    private final MessageTemplateService templateService;
    private final NotifyRecordService recordService;

    public NotifyCenterController(MessageTemplateService templateService, NotifyRecordService recordService) {
        this.templateService = templateService;
        this.recordService = recordService;
    }

    // === 消息模板 ===
    @Operation(summary = "模板列表")
    @GetMapping("/templates/page")
    @SaCheckRole("admin")
    public Result<PageResult<SysMessageTemplate>> templatePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        return Result.ok(templateService.pageQuery(page, size, name));
    }

    @Operation(summary = "新增模板")
    @PostMapping("/templates")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "新增模板")
    public Result<?> addTemplate(@RequestBody SysMessageTemplate template) {
        templateService.save(template);
        return Result.ok();
    }

    @Operation(summary = "更新模板")
    @PutMapping("/templates")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "更新模板")
    public Result<?> updateTemplate(@RequestBody SysMessageTemplate template) {
        templateService.updateById(template);
        return Result.ok();
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/templates/{id}")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "删除模板")
    public Result<?> deleteTemplate(@PathVariable Long id) {
        templateService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "发送通知")
    @PostMapping("/send")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "发送通知")
    public Result<?> sendNotify(@RequestBody Map<String, Object> body) {
        Long templateId = body.get("templateId") != null ? ((Number) body.get("templateId")).longValue() : null;
        String channel = (String) body.getOrDefault("channel", "message");
        String receiver = (String) body.get("receiver");
        String title = (String) body.get("title");
        String content = (String) body.get("content");

        SysNotifyRecord record = new SysNotifyRecord();
        record.setTemplateId(templateId);
        record.setChannel(channel);
        record.setReceiver(receiver);
        record.setTitle(title);
        record.setContent(content);
        record.setStatus(1); // 直接标记成功，实际可扩展为异步发送
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        record.setCreateTime(now);
        record.setSendTime(now);
        recordService.save(record);
        return Result.ok("发送成功", null);
    }

    // === 发送记录 ===
    @Operation(summary = "发送记录列表")
    @GetMapping("/records/page")
    @SaCheckRole("admin")
    public Result<PageResult<SysNotifyRecord>> recordPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Integer status) {
        return Result.ok(recordService.pageQuery(page, size, channel, status));
    }

    @Operation(summary = "删除记录")
    @DeleteMapping("/records/{id}")
    @SaCheckRole("admin")
    public Result<?> deleteRecord(@PathVariable Long id) {
        recordService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除记录")
    @DeleteMapping("/records/batch")
    @SaCheckRole("admin")
    public Result<?> deleteRecords(@RequestBody List<Long> ids) {
        recordService.removeByIds(ids);
        return Result.ok();
    }

    @Operation(summary = "重发通知")
    @PostMapping("/records/{id}/retry")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "重发通知")
    public Result<?> retry(@PathVariable Long id) {
        SysNotifyRecord record = recordService.getById(id);
        if (record == null) return Result.fail(404, "记录不存在");
        record.setStatus(1);
        record.setRetryCount(record.getRetryCount() != null ? record.getRetryCount() + 1 : 1);
        record.setSendTime(java.time.LocalDateTime.now());
        record.setErrorMsg(null);
        recordService.updateById(record);
        return Result.ok("重发成功", null);
    }
}
