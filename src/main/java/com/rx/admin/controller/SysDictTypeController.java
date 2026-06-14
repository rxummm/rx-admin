package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysDictType;
import com.rx.admin.modules.system.dict.dto.DictTypeCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictTypeUpdateDTO;
import com.rx.admin.service.SysDictTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final SysDictTypeService sysDictTypeService;

    @GetMapping("/page")
    @SaCheckPermission("sys:dict:query")
    public Result<PageResult<SysDictType>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(sysDictTypeService.pageQuery(page, size, keyword));
    }

    @GetMapping("/list")
    @SaCheckPermission("sys:dict:query")
    public Result<?> list() {
        return Result.ok(sysDictTypeService.list());
    }

    @PostMapping
    @SaCheckPermission("sys:dict:add")
    @OperateLog(module = "字典管理", operation = "新增字典类型")
    public Result<Void> add(@RequestBody @Valid DictTypeCreateDTO dto) {
        sysDictTypeService.addDictType(dto);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("sys:dict:edit")
    @OperateLog(module = "字典管理", operation = "修改字典类型")
    public Result<Void> update(@RequestBody @Valid DictTypeUpdateDTO dto) {
        sysDictTypeService.updateDictType(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:dict:delete")
    @OperateLog(module = "字典管理", operation = "删除字典类型")
    public Result<Void> delete(@PathVariable Long id) {
        sysDictTypeService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    @SaCheckPermission("sys:dict:query")
    public Result<SysDictType> getById(@PathVariable Long id) {
        return Result.ok(sysDictTypeService.getById(id));
    }
}