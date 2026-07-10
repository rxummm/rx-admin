package com.rx.admin.modules.tool.apiKey.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.apiKey.convert.SysApiKeyConvert;
import com.rx.admin.modules.tool.apiKey.dto.SysApiKeyCreateDTO;
import com.rx.admin.modules.tool.apiKey.entity.SysApiKey;
import com.rx.admin.modules.tool.apiKey.service.SysApiKeyService;
import com.rx.admin.modules.tool.apiKey.vo.SysApiKeyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "API密钥管理")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/api-key")
@RequiredArgsConstructor
public class SysApiKeyController {

    private final SysApiKeyService service;
    private final SysApiKeyConvert convert;

    @SaCheckPermission("tool:api-key:query")
    @GetMapping("/list")
    @Operation(summary = "获取API密钥列表")
    public Result<List<SysApiKeyVO>> list() {
        return Result.ok(convert.toVOList(service.list()));
    }

    @SaCheckPermission("tool:api-key:add")
    @PostMapping
    @Operation(summary = "生成API密钥")
    @OperateLog(module = "API密钥", operation = "生成密钥")
    public Result<Map<String, String>> generate(@RequestBody @Valid SysApiKeyCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(service.generateApiKey(dto, userId));
    }

    @SaCheckPermission("tool:api-key:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除API密钥")
    @OperateLog(module = "API密钥", operation = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }

    @SaCheckPermission("tool:api-key:toggle")
    @PutMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用API密钥")
    @OperateLog(module = "API密钥", operation = "切换状态")
    public Result<Void> toggle(@PathVariable Long id) {
        SysApiKey key = service.getById(id);
        if (key != null) {
            key.setStatus(key.getStatus() == 1 ? 0 : 1);
            service.updateById(key);
        }
        return Result.ok();
    }
}
