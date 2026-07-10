package com.rx.admin.modules.workflow.task.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.workflow.task.dto.WfTaskApproveDTO;
import com.rx.admin.modules.workflow.task.dto.WfTaskQueryDTO;
import com.rx.admin.modules.workflow.task.dto.WfTaskTransferDTO;
import com.rx.admin.modules.workflow.task.service.WfTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "工作流任务")
@RestController
@ApiVersion(1)
@RequestMapping("/wf/task")
@RequiredArgsConstructor
public class WfTaskController {

    private final WfTaskService service;

    @SaCheckPermission("wf:task:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询任务")
    public Result<?> page(WfTaskQueryDTO query) {
        return Result.ok(service.queryPage(query));
    }

    @SaCheckPermission("wf:task:query")
    @GetMapping("/my")
    @Operation(summary = "我的待办任务")
    public Result<?> myTasks(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        WfTaskQueryDTO query = new WfTaskQueryDTO();
        query.setAssigneeId(StpUtil.getLoginIdAsLong());
        query.setStatus("PENDING");
        query.setPage(page);
        query.setSize(size);
        return Result.ok(service.queryPage(query));
    }

    @SaCheckPermission("wf:task:approve")
    @PutMapping("/approve")
    @Operation(summary = "审批任务")
    @OperateLog(module = "工作流任务", operation = "审批")
    public Result<Void> approve(@RequestBody @Valid WfTaskApproveDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        String userName = (String) StpUtil.getSession().get("username");
        service.approveTask(dto, userId, userName);
        return Result.ok();
    }

    @SaCheckPermission("wf:task:transfer")
    @PutMapping("/transfer")
    @Operation(summary = "转办任务")
    @OperateLog(module = "工作流任务", operation = "转办")
    public Result<Void> transfer(@RequestBody @Valid WfTaskTransferDTO dto) {
        service.transferTask(dto);
        return Result.ok();
    }
}
