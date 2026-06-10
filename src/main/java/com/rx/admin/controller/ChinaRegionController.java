package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.classics.ChinaRegion;
import com.rx.admin.service.classics.ChinaRegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 行政区划管理
 */
@RestController
@RequestMapping("/api/tool/region")
@RequiredArgsConstructor
public class ChinaRegionController {

    private final ChinaRegionService chinaRegionService;

    /**
     * 分页查询行政区划
     */
    @GetMapping("/page")
    @SaCheckPermission("tool:region:query")
    public Result<PageResult<ChinaRegion>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String parentCode) {
        return Result.ok(chinaRegionService.pageQuery(page, size, keyword, level, parentCode));
    }

    /**
     * 查询下级行政区划列表（联动下拉）
     */
    @GetMapping("/children")
    @SaCheckPermission("tool:region:query")
    public Result<List<ChinaRegion>> children(@RequestParam(required = false) String parentCode) {
        return Result.ok(chinaRegionService.listByParentCode(parentCode));
    }

    /**
     * 搜索行政区划（级联选择器搜索用）
     */
    @GetMapping("/search")
    @SaCheckPermission("tool:region:query")
    public Result<List<ChinaRegion>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer level) {
        return Result.ok(chinaRegionService.search(keyword, level));
    }

    /**
     * 根据ID查询详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("tool:region:query")
    public Result<ChinaRegion> getById(@PathVariable Long id) {
        return Result.ok(chinaRegionService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    @SaCheckPermission("tool:region:add")
    public Result<Void> add(@RequestBody ChinaRegion region) {
        chinaRegionService.save(region);
        return Result.ok();
    }

    /**
     * 修改
     */
    @PutMapping
    @SaCheckPermission("tool:region:edit")
    public Result<Void> update(@RequestBody ChinaRegion region) {
        chinaRegionService.updateById(region);
        return Result.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("tool:region:delete")
    public Result<Void> delete(@PathVariable Long id) {
        // 检查是否有下级
        ChinaRegion region = chinaRegionService.getById(id);
        if (region != null) {
            long childCount = chinaRegionService.count(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChinaRegion>()
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
