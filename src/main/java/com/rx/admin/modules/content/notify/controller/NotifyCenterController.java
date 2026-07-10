package com.rx.admin.modules.content.notify.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.exception.ErrorCode;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.content.notify.entity.SysMessageTemplate;
import com.rx.admin.modules.content.notify.entity.SysNotifyRecord;
import com.rx.admin.modules.content.notify.dto.MessageTemplateCreateDTO;
import com.rx.admin.modules.content.notify.dto.MessageTemplateUpdateDTO;
import com.rx.admin.modules.content.notify.dto.NotifySendDTO;
import com.rx.admin.modules.content.notify.service.MessageTemplateService;
import com.rx.admin.modules.content.notify.service.NotifyRecordService;
import com.rx.admin.modules.content.notify.convert.SysMessageTemplateConvert;
import com.rx.admin.modules.content.notify.convert.SysNotifyRecordConvert;
import com.rx.admin.modules.content.notify.vo.SysMessageTemplateVO;
import com.rx.admin.modules.content.notify.vo.SysNotifyRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "通知中心")
@RestController
@ApiVersion(1)
@RequestMapping("/notify-center")
@RequiredArgsConstructor
public class NotifyCenterController {

    private final MessageTemplateService templateService;
    private final NotifyRecordService recordService;
    private final SysMessageTemplateConvert templateConvert;
    private final SysNotifyRecordConvert recordConvert;

    @Operation(summary = "模板列表")
    @GetMapping("/templates/page")
    @SaCheckRole("admin")
    public Result<PageResult<SysMessageTemplateVO>> templatePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        PageResult<SysMessageTemplate> pr = templateService.pageQuery(page, size, name);
        return Result.ok(templateConvert.toPageResult(pr));
    }

    @Operation(summary = "新增模板")
    @PostMapping("/templates")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "新增模板")
    public Result<Void> addTemplate(@RequestBody @Valid MessageTemplateCreateDTO dto) {
        templateService.addTemplate(dto);
        return Result.ok();
    }

    @Operation(summary = "更新模板")
    @PutMapping("/templates")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "更新模板")
    public Result<Void> updateTemplate(@RequestBody @Valid MessageTemplateUpdateDTO dto) {
        templateService.updateTemplate(dto);
        return Result.ok();
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/templates/{id}")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "删除模板")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "发送通知")
    @PostMapping("/send")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "发送通知")
    public Result<Void> sendNotify(@RequestBody @Valid NotifySendDTO dto) {
        SysNotifyRecord record = new SysNotifyRecord();
        record.setTemplateId(dto.getTemplateId());
        record.setChannel(dto.getChannel() != null ? dto.getChannel() : "message");
        record.setReceiver(dto.getReceiver());
        record.setTitle(dto.getTitle());
        record.setContent(dto.getContent());
        record.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        record.setCreateTime(now);
        record.setSendTime(now);
        recordService.save(record);
        return Result.ok();
    }

    @Operation(summary = "发送记录列表")
    @GetMapping("/records/page")
    @SaCheckRole("admin")
    public Result<PageResult<SysNotifyRecordVO>> recordPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Integer status) {
        PageResult<SysNotifyRecord> pr = recordService.pageQuery(page, size, channel, status);
        return Result.ok(recordConvert.toPageResult(pr));
    }

    @Operation(summary = "删除记录")
    @DeleteMapping("/records/{id}")
    @SaCheckRole("admin")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        recordService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除记录")
    @DeleteMapping("/records/batch")
    @SaCheckRole("admin")
    public Result<Void> deleteRecords(@RequestBody List<Long> ids) {
        recordService.removeByIds(ids);
        return Result.ok();
    }

    @Operation(summary = "重发通知")
    @PostMapping("/records/{id}/retry")
    @SaCheckRole("admin")
    @OperateLog(module = "通知中心", operation = "重发通知")
    public Result<Void> retry(@PathVariable Long id) {
        SysNotifyRecord record = recordService.getById(id);
        if (record == null) return Result.fail(ErrorCode.NOT_FOUND, "记录不存在");
        record.setStatus(1);
        record.setRetryCount(record.getRetryCount() != null ? record.getRetryCount() + 1 : 1);
        record.setSendTime(LocalDateTime.now());
        record.setErrorMsg(null);
        recordService.updateById(record);
        return Result.ok();
    }
}
