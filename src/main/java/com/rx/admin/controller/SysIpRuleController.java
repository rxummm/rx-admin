package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysIpRule;
import com.rx.admin.modules.system.iprule.dto.IpRuleCreateDTO;
import com.rx.admin.modules.system.iprule.dto.IpRuleUpdateDTO;
import com.rx.admin.service.SysConfigService;
import com.rx.admin.service.SysIpRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "IP黑白名单管理")
@RestController
@RequestMapping("/api/system/ip-rule")
@RequiredArgsConstructor
public class SysIpRuleController {

    private final SysIpRuleService ipRuleService;
    private final SysConfigService configService;

    @GetMapping("/page")
    @SaCheckPermission("system:ip-rule:list")
    @Operation(summary = "IP规则分页查询")
    public Result<PageResult<SysIpRule>> page(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String ruleType) {
        return Result.ok(ipRuleService.pageQuery(page, size, keyword, ruleType));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:ip-rule:list")
    public Result<SysIpRule> getById(@PathVariable Long id) {
        return Result.ok(ipRuleService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("system:ip-rule:add")
    public Result<Void> add(@RequestBody @Valid IpRuleCreateDTO dto) {
        ipRuleService.addIpRule(dto);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("system:ip-rule:edit")
    public Result<Void> update(@RequestBody @Valid IpRuleUpdateDTO dto) {
        ipRuleService.updateIpRule(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:ip-rule:delete")
    public Result<Void> delete(@PathVariable Long id) {
        ipRuleService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/mode")
    @SaCheckPermission("system:ip-rule:list")
    @Operation(summary = "获取IP过滤模式")
    public Result<Map<String, String>> getMode() {
        String enabled = configService.getValue("ip.filter.mode");
        return Result.ok(Map.of("mode", enabled != null ? enabled : "OFF"));
    }

    @PutMapping("/mode")
    @SaCheckPermission("system:ip-rule:edit")
    @Operation(summary = "设置IP过滤模式")
    public Result<Void> setMode(@RequestBody Map<String, String> body) {
        String mode = body.get("mode");
        if (mode == null || !mode.matches("BLACK|WHITE|OFF")) {
            return Result.fail("模式只能是 BLACK / WHITE / OFF");
        }
        configService.updateValue("ip.filter.mode", mode);
        return Result.ok();
    }
}