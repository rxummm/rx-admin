package com.rx.admin.modules.system.i18n.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.i18n.dto.SysI18nKeyCreateDTO;
import com.rx.admin.modules.system.i18n.dto.SysI18nTranslationDTO;
import com.rx.admin.modules.system.i18n.entity.SysI18nKey;
import com.rx.admin.modules.system.i18n.entity.SysI18nLocale;
import com.rx.admin.modules.system.i18n.service.SysI18nService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "多语言管理")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/i18n")
@RequiredArgsConstructor
public class SysI18nController {

    private final SysI18nService service;

    @SaCheckPermission("sys:i18n:query")
    @GetMapping("/locales")
    @Operation(summary = "获取语言列表")
    public Result<List<SysI18nLocale>> listLocales() {
        return Result.ok(service.listLocales());
    }

    @SaCheckPermission("sys:i18n:query")
    @GetMapping("/keys")
    @Operation(summary = "获取翻译键列表")
    public Result<List<SysI18nKey>> listKeys(@RequestParam(required = false) String module) {
        return Result.ok(service.listKeys(module));
    }

    @SaCheckPermission("sys:i18n:query")
    @GetMapping("/translations/{localeCode}")
    @Operation(summary = "获取指定语言的翻译")
    public Result<Map<String, String>> getTranslations(@PathVariable String localeCode) {
        return Result.ok(service.getTranslations(localeCode));
    }

    @SaCheckPermission("sys:i18n:add")
    @PostMapping("/keys")
    @Operation(summary = "新增翻译键")
    @OperateLog(module = "多语言", operation = "新增翻译键")
    public Result<Void> addKey(@RequestBody @Valid SysI18nKeyCreateDTO dto) {
        service.addKey(dto);
        return Result.ok();
    }

    @SaCheckPermission("sys:i18n:edit")
    @PostMapping("/translations")
    @Operation(summary = "保存翻译")
    @OperateLog(module = "多语言", operation = "保存翻译")
    public Result<Void> saveTranslation(@RequestBody @Valid SysI18nTranslationDTO dto) {
        service.saveTranslation(dto);
        return Result.ok();
    }

    @SaCheckPermission("sys:i18n:delete")
    @DeleteMapping("/keys/{id}")
    @Operation(summary = "删除翻译键")
    @OperateLog(module = "多语言", operation = "删除翻译键")
    public Result<Void> deleteKey(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
