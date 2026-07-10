package com.rx.admin.modules.tool.wiki.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.wiki.convert.WikiConvert;
import com.rx.admin.modules.tool.wiki.dto.WikiPageCreateDTO;
import com.rx.admin.modules.tool.wiki.dto.WikiSpaceCreateDTO;
import com.rx.admin.modules.tool.wiki.entity.WikiPage;
import com.rx.admin.modules.tool.wiki.service.WikiService;
import com.rx.admin.modules.tool.wiki.vo.WikiPageVO;
import com.rx.admin.modules.tool.wiki.vo.WikiSpaceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "知识库")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/wiki")
@RequiredArgsConstructor
public class WikiController {

    private final WikiService service;
    private final WikiConvert convert;

    @SaCheckPermission("tool:wiki:query")
    @GetMapping("/spaces")
    @Operation(summary = "获取知识库空间列表")
    public Result<List<WikiSpaceVO>> listSpaces() {
        return Result.ok(convert.toSpaceVOList(service.listSpaces()));
    }

    @SaCheckPermission("tool:wiki:query")
    @GetMapping("/spaces/{id}/pages")
    @Operation(summary = "获取空间页面列表")
    public Result<List<WikiPageVO>> listPages(@PathVariable Long id) {
        return Result.ok(convert.toPageVOList(service.listPages(id)));
    }

    @SaCheckPermission("tool:wiki:query")
    @GetMapping("/pages/{id}")
    @Operation(summary = "获取页面内容")
    public Result<WikiPageVO> getPage(@PathVariable Long id) {
        WikiPage page = service.getPage(id);
        return Result.ok(page != null ? convert.toPageVO(page) : null);
    }

    @SaCheckPermission("tool:wiki:add")
    @PostMapping("/spaces")
    @Operation(summary = "创建知识库空间")
    @OperateLog(module = "知识库", operation = "创建空间")
    public Result<Void> createSpace(@RequestBody @Valid WikiSpaceCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        service.createSpace(dto, userId);
        return Result.ok();
    }

    @SaCheckPermission("tool:wiki:add")
    @PostMapping("/pages")
    @Operation(summary = "创建知识库页面")
    @OperateLog(module = "知识库", operation = "创建页面")
    public Result<Void> createPage(@RequestBody @Valid WikiPageCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        String userName = (String) StpUtil.getSession().get("username");
        service.createPage(dto, userId, userName);
        return Result.ok();
    }

    @SaCheckPermission("tool:wiki:edit")
    @PutMapping("/pages/{id}")
    @Operation(summary = "更新知识库页面")
    @OperateLog(module = "知识库", operation = "更新页面")
    public Result<Void> updatePage(@PathVariable Long id, @RequestBody WikiPage page) {
        service.updatePage(id, page.getTitle(), page.getContent(), page.getIsPublished());
        return Result.ok();
    }

    @SaCheckPermission("tool:wiki:delete")
    @DeleteMapping("/spaces/{id}")
    @Operation(summary = "删除知识库空间")
    @OperateLog(module = "知识库", operation = "删除空间")
    public Result<Void> deleteSpace(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
