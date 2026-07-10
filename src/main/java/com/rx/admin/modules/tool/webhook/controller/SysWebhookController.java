package com.rx.admin.modules.tool.webhook.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.webhook.entity.SysWebhook;
import com.rx.admin.modules.tool.webhook.dto.SysWebhookCreateDTO;
import com.rx.admin.modules.tool.webhook.dto.SysWebhookQueryDTO;
import com.rx.admin.modules.tool.webhook.service.SysWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Webhook管理")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/webhook")
@RequiredArgsConstructor
public class SysWebhookController {

    private final SysWebhookService service;

    @SaCheckPermission("tool:webhook:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询Webhook")
    public Result<?> page(SysWebhookQueryDTO query) {
        return Result.ok(service.queryPage(query));
    }

    @SaCheckPermission("tool:webhook:add")
    @PostMapping
    @Operation(summary = "新增Webhook")
    @OperateLog(module = "Webhook管理", operation = "新增")
    public Result<Void> add(@RequestBody @Valid SysWebhookCreateDTO dto) {
        service.addEntity(dto);
        return Result.ok();
    }

    @SaCheckPermission("tool:webhook:edit")
    @PutMapping
    @Operation(summary = "修改Webhook")
    @OperateLog(module = "Webhook管理", operation = "修改")
    public Result<Void> update(@RequestBody SysWebhook webhook) {
        service.updateById(webhook);
        return Result.ok();
    }

    @SaCheckPermission("tool:webhook:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除Webhook")
    @OperateLog(module = "Webhook管理", operation = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }

    @SaCheckPermission("tool:webhook:toggle")
    @PutMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用Webhook")
    @OperateLog(module = "Webhook管理", operation = "切换状态")
    public Result<Void> toggle(@PathVariable Long id) {
        SysWebhook webhook = service.getById(id);
        if (webhook != null) {
            webhook.setStatus(webhook.getStatus() == 1 ? 0 : 1);
            service.updateById(webhook);
        }
        return Result.ok();
    }
}
