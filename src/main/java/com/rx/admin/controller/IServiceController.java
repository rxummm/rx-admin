package com.rx.admin.controller;

import com.rx.admin.common.result.Result;
import com.rx.admin.entity.IServiceCategory;
import com.rx.admin.entity.IServiceItem;
import com.rx.admin.service.IServiceCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * i-Service 接口
 * IBM i 系统服务（Application Services / IFS Services）
 */
@Slf4j
@RestController
@RequestMapping("/api/iservice")
@RequiredArgsConstructor
public class IServiceController {

    private final IServiceCategoryService categoryService;

    /**
     * 获取所有分类及其服务列表（树形结构）
     */
    @GetMapping("/categories")
    public Result<List<IServiceCategory>> listCategories() {
        List<IServiceCategory> categories = categoryService.listWithItems();
        return Result.ok(categories);
    }

    /**
     * 根据分类编码获取服务列表
     */
    @GetMapping("/categories/{code}")
    public Result<IServiceCategory> getCategory(@PathVariable String code) {
        IServiceCategory category = categoryService.getByCodeWithItems(code);
        if (category == null) {
            return Result.fail(404, "分类不存在: " + code);
        }
        return Result.ok(category);
    }

    /**
     * 获取单个服务的完整详情（含参数/列/示例/权限）
     */
    @GetMapping("/items/{id}")
    public Result<IServiceItem> getItemDetail(@PathVariable Long id) {
        IServiceItem item = categoryService.getItemDetail(id);
        if (item == null) {
            return Result.fail(404, "服务不存在: " + id);
        }
        return Result.ok(item);
    }
}
