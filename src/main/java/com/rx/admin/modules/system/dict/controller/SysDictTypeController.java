package com.rx.admin.modules.system.dict.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.dict.entity.SysDictType;
import com.rx.admin.modules.system.dict.dto.DictTypeCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictTypeUpdateDTO;
import com.rx.admin.modules.system.dict.service.ISysDictTypeService;
import com.rx.admin.modules.system.dict.convert.DictConvert;
import com.rx.admin.modules.system.dict.vo.DictTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "字典类型管理")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final ISysDictTypeService sysDictTypeService;
    private final DictConvert dictConvert;

    @Operation(summary = "分页查询字典类型")
    @GetMapping("/page")
    @SaCheckPermission(PermissionConstants.Dict.QUERY)
    public Result<PageResult<DictTypeVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<SysDictType> pr = sysDictTypeService.pageQuery(page, size, keyword);
        return Result.ok(dictConvert.toTypePageResult(pr));
    }

    @Operation(summary = "查询所有字典类型")
    @GetMapping("/list")
    @SaCheckPermission(PermissionConstants.Dict.QUERY)
    public Result<List<DictTypeVO>> list() {
        return Result.ok(dictConvert.toTypeVOList(sysDictTypeService.list()));
    }

    @Operation(summary = "新增字典类型")
    @PostMapping
    @SaCheckPermission(PermissionConstants.Dict.ADD)
    @OperateLog(module = "字典管理", operation = "新增字典类型")
    public Result<Void> add(@RequestBody @Valid DictTypeCreateDTO dto) {
        sysDictTypeService.addDictType(dto);
        return Result.ok();
    }

    @Operation(summary = "修改字典类型")
    @PutMapping
    @SaCheckPermission(PermissionConstants.Dict.EDIT)
    @OperateLog(module = "字典管理", operation = "修改字典类型")
    public Result<Void> update(@RequestBody @Valid DictTypeUpdateDTO dto) {
        sysDictTypeService.updateDictType(dto);
        return Result.ok();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Dict.DELETE)
    @OperateLog(module = "字典管理", operation = "删除字典类型")
    public Result<Void> delete(@PathVariable Long id) {
        sysDictTypeService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除字典类型")
    @DeleteMapping("/batch")
    @SaCheckPermission(PermissionConstants.Dict.DELETE)
    @OperateLog(module = "字典管理", operation = "批量删除")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysDictTypeService.deleteDictTypeBatch(ids);
        return Result.ok();
    }

    @Operation(summary = "根据ID查询字典类型")
    @GetMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Dict.QUERY)
    public Result<DictTypeVO> getById(@PathVariable Long id) {
        return Result.ok(dictConvert.toTypeVO(sysDictTypeService.getById(id)));
    }
}
