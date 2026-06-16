package com.rx.admin.modules.system.dept.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.dept.dto.DeptCreateDTO;
import com.rx.admin.modules.system.dept.dto.DeptUpdateDTO;
import com.rx.admin.modules.system.dept.service.SysDeptService;
import com.rx.admin.modules.system.dept.convert.DeptConvert;
import com.rx.admin.modules.system.dept.vo.DeptVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;
    private final DeptConvert deptConvert;

    @Operation(summary = "查询部门树")
    @GetMapping("/tree")
    @SaCheckPermission("sys:dept:query")
    public Result<List<DeptVO>> tree() {
        return Result.ok(deptConvert.toVOList(sysDeptService.getDeptTree()));
    }

    @Operation(summary = "新增部门")
    @PostMapping
    @SaCheckPermission("sys:dept:add")
    @OperateLog(module = "部门管理", operation = "新增部门")
    public Result<Void> add(@RequestBody @Valid DeptCreateDTO dto) {
        sysDeptService.addDept(dto);
        return Result.ok();
    }

    @Operation(summary = "修改部门")
    @PutMapping
    @SaCheckPermission("sys:dept:edit")
    @OperateLog(module = "部门管理", operation = "修改部门")
    public Result<Void> update(@RequestBody @Valid DeptUpdateDTO dto) {
        sysDeptService.updateDept(dto);
        return Result.ok();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:dept:delete")
    @OperateLog(module = "部门管理", operation = "删除部门")
    public Result<Void> delete(@PathVariable Long id) {
        sysDeptService.deleteDept(id);
        return Result.ok();
    }

    @Operation(summary = "根据ID查询部门")
    @GetMapping("/{id}")
    @SaCheckPermission("sys:dept:query")
    public Result<DeptVO> getById(@PathVariable Long id) {
        return Result.ok(deptConvert.toVO(sysDeptService.getById(id)));
    }
}
