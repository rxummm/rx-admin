package com.rx.admin.modules.as400.controller;

import com.rx.admin.common.result.Result;
import com.rx.admin.modules.as400.service.IServiceCategoryService;
import com.rx.admin.modules.as400.convert.IServiceCategoryConvert;
import com.rx.admin.modules.as400.convert.IServiceItemConvert;
import com.rx.admin.modules.as400.vo.IServiceCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AS400服务目录")
@Slf4j
@RestController
@RequestMapping("/api/iservice")
@RequiredArgsConstructor
public class IServiceController {

    private final IServiceCategoryService categoryService;
    private final IServiceCategoryConvert categoryConvert;
    private final IServiceItemConvert itemConvert;

    @Operation(summary = "查询所有服务分类")
    @GetMapping("/categories")
    public Result<List<IServiceCategoryVO>> listCategories() {
        return Result.ok(categoryConvert.toVOList(categoryService.listWithItems()));
    }

    @Operation(summary = "根据编码查询服务分类")
    @GetMapping("/categories/{code}")
    public Result<IServiceCategoryVO> getCategory(@PathVariable String code) {
        var category = categoryService.getByCodeWithItems(code);
        if (category == null) {
            return Result.fail(404, "分类不存在: " + code);
        }
        return Result.ok(categoryConvert.toVO(category));
    }

    @Operation(summary = "查询服务项目详情")
    @GetMapping("/items/{id}")
    public Result<?> getItemDetail(@PathVariable Long id) {
        var item = categoryService.getItemDetail(id);
        if (item == null) {
            return Result.fail(404, "服务不存在: " + id);
        }
        return Result.ok(itemConvert.toVO(item));
    }
}
