package com.rx.admin.modules.tool.emailTemplate.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.emailTemplate.convert.SysEmailTemplateConvert;
import com.rx.admin.modules.tool.emailTemplate.dto.SysEmailTemplateCreateDTO;
import com.rx.admin.modules.tool.emailTemplate.entity.SysEmailTemplate;
import com.rx.admin.modules.tool.emailTemplate.service.SysEmailTemplateService;
import com.rx.admin.modules.tool.emailTemplate.vo.SysEmailTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "邮件模板")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/email-template")
@RequiredArgsConstructor
public class SysEmailTemplateController {

    private final SysEmailTemplateService service;
    private final SysEmailTemplateConvert convert;

    @SaCheckPermission("tool:email-template:query")
    @GetMapping("/list")
    @Operation(summary = "获取邮件模板列表")
    public Result<List<SysEmailTemplateVO>> list() {
        return Result.ok(convert.toVOList(service.list()));
    }

    @SaCheckPermission("tool:email-template:query")
    @GetMapping("/{id}")
    @Operation(summary = "获取邮件模板详情")
    public Result<SysEmailTemplateVO> getById(@PathVariable Long id) {
        SysEmailTemplate template = service.getById(id);
        return Result.ok(template != null ? convert.toVO(template) : null);
    }

    @SaCheckPermission("tool:email-template:add")
    @PostMapping
    @Operation(summary = "新增邮件模板")
    @OperateLog(module = "邮件模板", operation = "新增")
    public Result<Void> add(@RequestBody @Valid SysEmailTemplateCreateDTO dto) {
        service.addEntity(dto);
        return Result.ok();
    }

    @SaCheckPermission("tool:email-template:edit")
    @PutMapping
    @Operation(summary = "修改邮件模板")
    @OperateLog(module = "邮件模板", operation = "修改")
    public Result<Void> update(@RequestBody SysEmailTemplate template) {
        service.updateById(template);
        return Result.ok();
    }

    @SaCheckPermission("tool:email-template:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除邮件模板")
    @OperateLog(module = "邮件模板", operation = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
