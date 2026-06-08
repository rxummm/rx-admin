package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.OperateLog;
import com.rx.admin.common.Result;
import com.rx.admin.entity.SysDept;
import com.rx.admin.service.SysDeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;

    @GetMapping("/tree")
    @SaCheckPermission("sys:dept:query")
    public Result<List<SysDept>> tree() {
        return Result.ok(sysDeptService.getDeptTree());
    }

    @PostMapping
    @SaCheckPermission("sys:dept:add")
    @OperateLog(module = "部门管理", operation = "新增部门")
    public Result<Void> add(@RequestBody @Valid SysDept dept) {
        sysDeptService.addDept(dept);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("sys:dept:edit")
    @OperateLog(module = "部门管理", operation = "修改部门")
    public Result<Void> update(@RequestBody @Valid SysDept dept) {
        sysDeptService.updateDept(dept);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:dept:delete")
    @OperateLog(module = "部门管理", operation = "删除部门")
    public Result<Void> delete(@PathVariable Long id) {
        sysDeptService.deleteDept(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    @SaCheckPermission("sys:dept:query")
    public Result<SysDept> getById(@PathVariable Long id) {
        return Result.ok(sysDeptService.getById(id));
    }
}
