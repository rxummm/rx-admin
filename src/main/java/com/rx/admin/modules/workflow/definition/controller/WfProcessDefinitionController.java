package com.rx.admin.modules.workflow.definition.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.workflow.definition.convert.WfProcessDefinitionConvert;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionCreateDTO;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionQueryDTO;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionUpdateDTO;
import com.rx.admin.modules.workflow.definition.entity.WfProcessDefinition;
import com.rx.admin.modules.workflow.definition.service.WfProcessDefinitionService;
import com.rx.admin.modules.workflow.definition.vo.WfProcessDefinitionVO;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "流程定义")
@RestController
@ApiVersion(1)
@RequestMapping("/wf/definition")
@RequiredArgsConstructor
public class WfProcessDefinitionController {

    private final WfProcessDefinitionService service;
    private final WfProcessDefinitionConvert convert;

    @SaCheckPermission("wf:definition:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询流程定义")
    public Result<PageResult<WfProcessDefinitionVO>> page(WfProcessDefinitionQueryDTO query) {
        PageResult<WfProcessDefinition> result = service.queryPage(query);
        return Result.ok(convert.toPageResult(result));
    }

    @SaCheckPermission("wf:definition:add")
    @PostMapping
    @Operation(summary = "新增流程定义")
    @OperateLog(module = "流程定义", operation = "新增")
    public Result<Void> add(@RequestBody @Valid WfProcessDefinitionCreateDTO dto) {
        service.addEntity(dto);
        return Result.ok();
    }

    @SaCheckPermission("wf:definition:edit")
    @PutMapping
    @Operation(summary = "修改流程定义")
    @OperateLog(module = "流程定义", operation = "修改")
    public Result<Void> update(@RequestBody @Valid WfProcessDefinitionUpdateDTO dto) {
        service.updateEntity(dto);
        return Result.ok();
    }

    @SaCheckPermission("wf:definition:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除流程定义")
    @OperateLog(module = "流程定义", operation = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
