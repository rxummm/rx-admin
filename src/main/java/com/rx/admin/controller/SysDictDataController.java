package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysDictData;
import com.rx.admin.modules.system.dict.dto.DictDataCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictDataUpdateDTO;
import com.rx.admin.service.SysDictDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final SysDictDataService sysDictDataService;

    @GetMapping("/list/{typeId}")
    @SaCheckPermission("sys:dict:query")
    public Result<List<SysDictData>> listByTypeId(@PathVariable Long typeId) {
        return Result.ok(sysDictDataService.listByTypeId(typeId));
    }

    @GetMapping("/type/{dictType}")
    public Result<List<SysDictData>> getByDictType(@PathVariable String dictType) {
        return Result.ok(sysDictDataService.getByDictType(dictType));
    }

    @PostMapping
    @SaCheckPermission("sys:dict:add")
    @OperateLog(module = "字典管理", operation = "新增字典数据")
    public Result<Void> add(@RequestBody @Valid DictDataCreateDTO dto) {
        sysDictDataService.addDictData(dto);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("sys:dict:edit")
    @OperateLog(module = "字典管理", operation = "修改字典数据")
    public Result<Void> update(@RequestBody @Valid DictDataUpdateDTO dto) {
        sysDictDataService.updateDictData(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:dict:delete")
    @OperateLog(module = "字典管理", operation = "删除字典数据")
    public Result<Void> delete(@PathVariable Long id) {
        sysDictDataService.removeById(id);
        return Result.ok();
    }
}