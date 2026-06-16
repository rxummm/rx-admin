package com.rx.admin.modules.tool.region.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.constant.PermissionConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.region.entity.ChinaRegion;
import com.rx.admin.modules.tool.region.service.ChinaRegionService;
import com.rx.admin.modules.tool.region.convert.ChinaRegionConvert;
import com.rx.admin.modules.tool.region.vo.ChinaRegionVO;
import com.rx.admin.modules.tool.region.dto.ChinaRegionCreateDTO;
import com.rx.admin.modules.tool.region.dto.ChinaRegionUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "行政区划")
@RestController
@RequestMapping("/api/tool/region")
@RequiredArgsConstructor
public class ChinaRegionController {

    private final ChinaRegionService chinaRegionService;
    private final ChinaRegionConvert chinaRegionConvert;

    @Operation(summary = "分页查询行政区划")
    @GetMapping("/page")
    @SaCheckPermission(PermissionConstants.Tool.REGION_QUERY)
    public Result<PageResult<ChinaRegionVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String parentCode) {
        PageResult<ChinaRegion> pr = chinaRegionService.pageQuery(page, size, keyword, level, parentCode);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), chinaRegionConvert.toVOList(pr.getRecords())));
    }

    @Operation(summary = "查询下级行政区划")
    @GetMapping("/children")
    @SaCheckPermission(PermissionConstants.Tool.REGION_QUERY)
    public Result<List<ChinaRegionVO>> children(@RequestParam(required = false) String parentCode) {
        return Result.ok(chinaRegionConvert.toVOList(chinaRegionService.listByParentCode(parentCode)));
    }

    @Operation(summary = "搜索行政区划")
    @GetMapping("/search")
    @SaCheckPermission(PermissionConstants.Tool.REGION_QUERY)
    public Result<List<ChinaRegionVO>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer level) {
        return Result.ok(chinaRegionConvert.toVOList(chinaRegionService.search(keyword, level)));
    }

    @Operation(summary = "根据ID查询行政区划")
    @GetMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Tool.REGION_QUERY)
    public Result<ChinaRegionVO> getById(@PathVariable Long id) {
        return Result.ok(chinaRegionConvert.toVO(chinaRegionService.getById(id)));
    }

    @Operation(summary = "新增行政区划")
    @PostMapping
    @SaCheckPermission(PermissionConstants.Tool.REGION_ADD)
    public Result<Void> add(@RequestBody @Valid ChinaRegionCreateDTO dto) {
        chinaRegionService.save(chinaRegionConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "修改行政区划")
    @PutMapping
    @SaCheckPermission(PermissionConstants.Tool.REGION_EDIT)
    public Result<Void> update(@RequestBody @Valid ChinaRegionUpdateDTO dto) {
        ChinaRegion entity = chinaRegionService.getById(dto.getId());
        if (entity == null) return Result.fail("记录不存在");
        chinaRegionConvert.updateEntity(entity, dto);
        chinaRegionService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除行政区划")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Tool.REGION_DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        ChinaRegion region = chinaRegionService.getById(id);
        if (region != null) {
            long childCount = chinaRegionService.count(
                    new LambdaQueryWrapper<ChinaRegion>()
                            .eq(ChinaRegion::getParentCode, region.getCode())
            );
            if (childCount > 0) {
                return Result.fail("该行政区划下存在下级数据，无法删除");
            }
        }
        chinaRegionService.removeById(id);
        return Result.ok();
    }
}
