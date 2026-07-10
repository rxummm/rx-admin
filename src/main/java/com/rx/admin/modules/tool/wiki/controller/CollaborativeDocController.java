package com.rx.admin.modules.tool.wiki.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.wiki.entity.SysCollaborativeDoc;
import com.rx.admin.modules.tool.wiki.entity.SysCollaborativeDocVersion;
import com.rx.admin.modules.tool.wiki.service.CollaborativeDocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 协作文档控制器
 */
@Tag(name = "协作文档")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/collaborative-doc")
@RequiredArgsConstructor
public class CollaborativeDocController {

    private final CollaborativeDocService docService;

    @Operation(summary = "分页查询文档")
    @GetMapping("/page")
    public Result<PageResult<SysCollaborativeDoc>> page(
            @RequestParam(required = false) Long spaceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(docService.pageQuery(spaceId, page, size));
    }

    @Operation(summary = "获取文档详情")
    @GetMapping("/{id}")
    public Result<SysCollaborativeDoc> getDoc(@PathVariable Long id) {
        return Result.ok(docService.getDoc(id));
    }

    @Operation(summary = "创建文档")
    @PostMapping
    @OperateLog(module = "协作文档", operation = "创建文档")
    public Result<SysCollaborativeDoc> createDoc(@RequestBody SysCollaborativeDoc doc) {
        // TODO: 从 Sa-Token 获取当前用户信息
        doc.setCreatorId(1L);
        doc.setCreatorName("admin");
        doc.setLastEditorId(1L);
        doc.setLastEditorName("admin");
        return Result.ok(docService.createDoc(doc));
    }

    @Operation(summary = "更新文档")
    @PutMapping("/{id}")
    @OperateLog(module = "协作文档", operation = "更新文档")
    public Result<Void> updateDoc(@PathVariable Long id, @RequestBody Map<String, String> params) {
        // TODO: 从 Sa-Token 获取当前用户信息
        Long editorId = 1L;
        String editorName = "admin";
        docService.updateDoc(id, params.get("title"), params.get("content"), editorId, editorName);
        return Result.ok();
    }

    @Operation(summary = "获取文档版本历史")
    @GetMapping("/{id}/versions")
    public Result<List<SysCollaborativeDocVersion>> getVersionHistory(@PathVariable Long id) {
        return Result.ok(docService.getVersionHistory(id));
    }

    @Operation(summary = "获取指定版本")
    @GetMapping("/{id}/version/{versionNumber}")
    public Result<SysCollaborativeDocVersion> getVersion(
            @PathVariable Long id, 
            @PathVariable Integer versionNumber) {
        return Result.ok(docService.getVersion(id, versionNumber));
    }

    @Operation(summary = "锁定文档")
    @PostMapping("/{id}/lock")
    public Result<Boolean> lockDoc(@PathVariable Long id) {
        // TODO: 从 Sa-Token 获取当前用户ID
        Long userId = 1L;
        return Result.ok(docService.lockDoc(id, userId));
    }

    @Operation(summary = "解锁文档")
    @PostMapping("/{id}/unlock")
    public Result<Void> unlockDoc(@PathVariable Long id) {
        // TODO: 从 Sa-Token 获取当前用户ID
        Long userId = 1L;
        docService.unlockDoc(id, userId);
        return Result.ok();
    }
}
