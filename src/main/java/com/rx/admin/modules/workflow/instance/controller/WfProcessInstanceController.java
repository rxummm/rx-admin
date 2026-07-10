package com.rx.admin.modules.workflow.instance.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.workflow.instance.dto.WfProcessInstanceCreateDTO;
import com.rx.admin.modules.workflow.instance.dto.WfProcessInstanceQueryDTO;
import com.rx.admin.modules.workflow.instance.service.WfProcessInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "流程实例")
@RestController
@ApiVersion(1)
@RequestMapping("/wf/instance")
@RequiredArgsConstructor
public class WfProcessInstanceController {

    private final WfProcessInstanceService service;

    @SaCheckPermission("wf:instance:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询流程实例")
    public Result<?> page(WfProcessInstanceQueryDTO query) {
        return Result.ok(service.queryPage(query));
    }

    @SaCheckPermission("wf:instance:start")
    @PostMapping("/start")
    @Operation(summary = "发起流程")
    @OperateLog(module = "流程实例", operation = "发起流程")
    public Result<Void> start(@RequestBody @Valid WfProcessInstanceCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        String userName = (String) StpUtil.getSession().get("username");
        service.startProcess(dto, userId, userName);
        return Result.ok();
    }

    @SaCheckPermission("wf:instance:cancel")
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消流程")
    @OperateLog(module = "流程实例", operation = "取消流程")
    public Result<Void> cancel(@PathVariable Long id) {
        service.cancelProcess(id);
        return Result.ok();
    }

    @SaCheckPermission("wf:instance:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除流程实例")
    @OperateLog(module = "流程实例", operation = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
