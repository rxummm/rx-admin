package com.rx.admin.modules.tool.archive.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.archive.convert.SysArchiveConfigConvert;
import com.rx.admin.modules.tool.archive.entity.SysArchiveConfig;
import com.rx.admin.modules.tool.archive.dto.SysArchiveConfigCreateDTO;
import com.rx.admin.modules.tool.archive.service.SysArchiveConfigService;
import com.rx.admin.modules.tool.archive.vo.SysArchiveConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据归档")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/archive")
@RequiredArgsConstructor
public class SysArchiveConfigController {

    private final SysArchiveConfigService service;
    private final SysArchiveConfigConvert convert;

    @SaCheckPermission("tool:archive:query")
    @GetMapping("/list")
    @Operation(summary = "获取归档配置列表")
    public Result<List<SysArchiveConfigVO>> list() {
        return Result.ok(convert.toVOList(service.listAll()));
    }

    @SaCheckPermission("tool:archive:add")
    @PostMapping
    @Operation(summary = "新增归档配置")
    @OperateLog(module = "数据归档", operation = "新增配置")
    public Result<Void> add(@RequestBody @Valid SysArchiveConfigCreateDTO dto) {
        service.addEntity(dto);
        return Result.ok();
    }

    @SaCheckPermission("tool:archive:edit")
    @PutMapping
    @Operation(summary = "修改归档配置")
    @OperateLog(module = "数据归档", operation = "修改配置")
    public Result<Void> update(@RequestBody SysArchiveConfig config) {
        service.updateById(config);
        return Result.ok();
    }

    @SaCheckPermission("tool:archive:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除归档配置")
    @OperateLog(module = "数据归档", operation = "删除配置")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
