package com.rx.admin.modules.system.dict.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.dict.dto.DictDataCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictDataUpdateDTO;
import com.rx.admin.modules.system.dict.service.ISysDictDataService;
import com.rx.admin.modules.system.dict.convert.DictConvert;
import com.rx.admin.modules.system.dict.vo.DictDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典数据管理")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {

    private final ISysDictDataService sysDictDataService;
    private final DictConvert dictConvert;

    @Operation(summary = "根据字典类型ID查询数据")
    @GetMapping("/list/{typeId}")
    @SaCheckPermission(PermissionConstants.Dict.QUERY)
    public Result<List<DictDataVO>> listByTypeId(@PathVariable Long typeId) {
        return Result.ok(dictConvert.toDataVOList(sysDictDataService.listByTypeId(typeId)));
    }

    @Operation(summary = "根据字典类型编码查询数据")
    @GetMapping("/type/{dictType}")
    public Result<List<DictDataVO>> getByDictType(@PathVariable String dictType) {
        return Result.ok(dictConvert.toDataVOList(sysDictDataService.getByDictType(dictType)));
    }

    @Operation(summary = "新增字典数据")
    @PostMapping
    @SaCheckPermission(PermissionConstants.Dict.ADD)
    @OperateLog(module = "字典管理", operation = "新增字典数据")
    public Result<Void> add(@RequestBody @Valid DictDataCreateDTO dto) {
        sysDictDataService.addDictData(dto);
        return Result.ok();
    }

    @Operation(summary = "修改字典数据")
    @PutMapping
    @SaCheckPermission(PermissionConstants.Dict.EDIT)
    @OperateLog(module = "字典管理", operation = "修改字典数据")
    public Result<Void> update(@RequestBody @Valid DictDataUpdateDTO dto) {
        sysDictDataService.updateDictData(dto);
        return Result.ok();
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Dict.DELETE)
    @OperateLog(module = "字典管理", operation = "删除字典数据")
    public Result<Void> delete(@PathVariable Long id) {
        sysDictDataService.deleteDictData(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除字典数据")
    @DeleteMapping("/batch")
    @SaCheckPermission(PermissionConstants.Dict.DELETE)
    @OperateLog(module = "字典管理", operation = "批量删除")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysDictDataService.deleteDictDataBatch(ids);
        return Result.ok();
    }
}
