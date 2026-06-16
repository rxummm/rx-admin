package com.rx.admin.modules.system.iprule.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.exception.ErrorCode;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.iprule.entity.SysIpRule;
import com.rx.admin.modules.system.iprule.dto.IpRuleCreateDTO;
import com.rx.admin.modules.system.iprule.dto.IpRuleUpdateDTO;
import com.rx.admin.modules.system.config.service.ISysConfigService;
import com.rx.admin.modules.system.iprule.service.SysIpRuleService;
import com.rx.admin.modules.system.iprule.convert.IpRuleConvert;
import com.rx.admin.modules.system.iprule.vo.IpRuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "IP黑白名单管理")
@RestController
@ApiVersion(1)
@RequestMapping("/system/ip-rule")
@RequiredArgsConstructor
public class SysIpRuleController {

    private final SysIpRuleService ipRuleService;
    private final ISysConfigService configService;
    private final IpRuleConvert ipRuleConvert;

    @GetMapping("/page")
    @SaCheckPermission(PermissionConstants.IpRule.LIST)
    @Operation(summary = "IP规则分页查询")
    public Result<PageResult<IpRuleVO>> page(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String ruleType) {
        PageResult<SysIpRule> pr = ipRuleService.pageQuery(page, size, keyword, ruleType);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), ipRuleConvert.toVOList(pr.getRecords())));
    }

    @Operation(summary = "根据ID查询IP规则")
    @GetMapping("/{id}")
    @SaCheckPermission(PermissionConstants.IpRule.LIST)
    public Result<IpRuleVO> getById(@PathVariable Long id) {
        return Result.ok(ipRuleConvert.toVO(ipRuleService.getById(id)));
    }

    @OperateLog(module = "IP黑白名单", operation = "新增IP规则")
    @Operation(summary = "新增IP规则")
    @PostMapping
    @SaCheckPermission(PermissionConstants.IpRule.ADD)
    public Result<Void> add(@RequestBody @Valid IpRuleCreateDTO dto) {
        ipRuleService.addIpRule(dto);
        return Result.ok();
    }

    @OperateLog(module = "IP黑白名单", operation = "修改IP规则")
    @Operation(summary = "修改IP规则")
    @PutMapping
    @SaCheckPermission(PermissionConstants.IpRule.EDIT)
    public Result<Void> update(@RequestBody @Valid IpRuleUpdateDTO dto) {
        ipRuleService.updateIpRule(dto);
        return Result.ok();
    }

    @OperateLog(module = "IP黑白名单", operation = "删除IP规则")
    @Operation(summary = "删除IP规则")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.IpRule.DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        ipRuleService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/mode")
    @SaCheckPermission(PermissionConstants.IpRule.LIST)
    @Operation(summary = "获取IP过滤模式")
    public Result<Map<String, String>> getMode() {
        String enabled = configService.getValue("ip.filter.mode");
        return Result.ok(Map.of("mode", enabled != null ? enabled : "OFF"));
    }

    @PutMapping("/mode")
    @SaCheckPermission(PermissionConstants.IpRule.EDIT)
    @Operation(summary = "设置IP过滤模式")
    @OperateLog(module = "IP黑白名单", operation = "设置过滤模式")
    public Result<Void> setMode(@RequestBody Map<String, String> body) {
        String mode = body.get("mode");
        if (mode == null || !mode.matches("BLACK|WHITE|OFF")) {
            return Result.fail(ErrorCode.BAD_REQUEST, "模式只能是 BLACK / WHITE / OFF");
        }
        configService.updateValue("ip.filter.mode", mode);
        return Result.ok();
    }
}
