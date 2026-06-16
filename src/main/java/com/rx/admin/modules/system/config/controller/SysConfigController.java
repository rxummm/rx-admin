package com.rx.admin.modules.system.config.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.config.entity.SysConfig;
import com.rx.admin.modules.system.config.dto.ConfigCreateDTO;
import com.rx.admin.modules.system.config.dto.ConfigUpdateDTO;
import com.rx.admin.modules.system.config.service.ISysConfigService;
import com.rx.admin.modules.system.config.convert.ConfigConvert;
import com.rx.admin.modules.system.config.vo.ConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "系统配置")
@RestController
@ApiVersion(1)
@RequestMapping("/system/config")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SysConfigController {

    private final ISysConfigService configService;
    private final ConfigConvert configConvert;

    @Operation(summary = "获取所有配置（按分组）")
    @GetMapping("/grouped")
    public Result<Map<String, List<ConfigVO>>> getGrouped() {
        Map<String, List<SysConfig>> grouped = configService.getGrouped();
        Map<String, List<ConfigVO>> result = grouped.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> configConvert.toVOList(e.getValue())));
        return Result.ok(result);
    }

    @Operation(summary = "批量获取配置值")
    @PostMapping("/values")
    public Result<Map<String, String>> getValues(@RequestBody List<String> keys) {
        return Result.ok(configService.getValues(keys));
    }

    @Operation(summary = "获取单个配置值")
    @GetMapping("/value/{key}")
    public Result<String> getValue(@PathVariable String key) {
        return Result.ok(configService.getValue(key));
    }

    @Operation(summary = "更新配置值")
    @PutMapping("/value/{key}")
    @SaCheckPermission(PermissionConstants.Config.EDIT)
    @OperateLog(module = "系统配置", operation = "更新配置")
    @CacheEvict(value = "config", allEntries = true)
    public Result<Void> updateValue(@PathVariable String key, @RequestBody Map<String, String> body) {
        configService.updateValue(key, body.get("value"));
        return Result.ok();
    }

    @Operation(summary = "新增配置")
    @PostMapping
    @SaCheckPermission(PermissionConstants.Config.ADD)
    @OperateLog(module = "系统配置", operation = "新增配置")
    @CacheEvict(value = "config", allEntries = true)
    public Result<Void> add(@RequestBody @Valid ConfigCreateDTO dto) {
        configService.addConfig(dto);
        return Result.ok();
    }

    @Operation(summary = "更新配置")
    @PutMapping
    @SaCheckPermission(PermissionConstants.Config.EDIT)
    @OperateLog(module = "系统配置", operation = "更新配置")
    @CacheEvict(value = "config", allEntries = true)
    public Result<Void> update(@RequestBody @Valid ConfigUpdateDTO dto) {
        configService.updateConfig(dto);
        return Result.ok();
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Config.DELETE)
    @OperateLog(module = "系统配置", operation = "删除配置")
    @CacheEvict(value = "config", allEntries = true)
    public Result<Void> delete(@PathVariable Long id) {
        configService.removeById(id);
        return Result.ok();
    }
}
