package com.rx.admin.modules.tool.kanban.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.kanban.dto.KanbanBoardCreateDTO;
import com.rx.admin.modules.tool.kanban.dto.KanbanCardCreateDTO;
import com.rx.admin.modules.tool.kanban.entity.KanbanBoard;
import com.rx.admin.modules.tool.kanban.service.KanbanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "看板管理")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/kanban")
@RequiredArgsConstructor
public class KanbanController {

    private final KanbanService service;

    @SaCheckPermission("tool:kanban:query")
    @GetMapping("/list")
    @Operation(summary = "获取看板列表")
    public Result<List<KanbanBoard>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(service.listBoards(userId));
    }

    @SaCheckPermission("tool:kanban:query")
    @GetMapping("/{id}")
    @Operation(summary = "获取看板详情")
    public Result<KanbanBoard> getById(@PathVariable Long id) {
        return Result.ok(service.getBoardDetail(id));
    }

    @SaCheckPermission("tool:kanban:add")
    @PostMapping
    @Operation(summary = "创建看板")
    @OperateLog(module = "看板管理", operation = "创建")
    public Result<Void> create(@RequestBody @Valid KanbanBoardCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        service.createBoard(dto, userId);
        return Result.ok();
    }

    @SaCheckPermission("tool:kanban:add")
    @PostMapping("/card")
    @Operation(summary = "创建卡片")
    @OperateLog(module = "看板管理", operation = "创建卡片")
    public Result<Void> createCard(@RequestBody @Valid KanbanCardCreateDTO dto) {
        service.createCard(dto);
        return Result.ok();
    }

    @SaCheckPermission("tool:kanban:edit")
    @PutMapping("/card/{id}/move")
    @Operation(summary = "移动卡片")
    @OperateLog(module = "看板管理", operation = "移动卡片")
    public Result<Void> moveCard(@PathVariable Long id, @RequestParam Long columnId, @RequestParam(defaultValue = "0") Integer sortOrder) {
        service.moveCard(id, columnId, sortOrder);
        return Result.ok();
    }

    @SaCheckPermission("tool:kanban:delete")
    @DeleteMapping("/card/{id}")
    @Operation(summary = "删除卡片")
    @OperateLog(module = "看板管理", operation = "删除卡片")
    public Result<Void> deleteCard(@PathVariable Long id) {
        service.deleteCard(id);
        return Result.ok();
    }

    @SaCheckPermission("tool:kanban:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除看板")
    @OperateLog(module = "看板管理", operation = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
