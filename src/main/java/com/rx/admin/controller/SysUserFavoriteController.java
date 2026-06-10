package com.rx.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysUserFavorite;
import com.rx.admin.service.SysUserFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "快捷收藏夹")
@RestController
@RequestMapping("/api/system/favorite")
@RequiredArgsConstructor
public class SysUserFavoriteController {

    private final SysUserFavoriteService favoriteService;

    @GetMapping("/list")
    @Operation(summary = "获取收藏列表")
    public Result<List<SysUserFavorite>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(favoriteService.getUserFavorites(userId));
    }

    @PostMapping
    @Operation(summary = "添加收藏")
    public Result<SysUserFavorite> add(@RequestBody SysUserFavorite fav) {
        Long userId = StpUtil.getLoginIdAsLong();
        fav.setUserId(userId);
        favoriteService.save(fav);
        return Result.ok(fav);
    }

    @PostMapping("/toggle")
    @Operation(summary = "切换收藏状态")
    public Result<Map<String, Object>> toggle(@RequestBody SysUserFavorite fav) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("toggle收藏: userId={}, name={}, path={}, icon={}, menuId={}", userId, fav.getName(), fav.getPath(), fav.getIcon(), fav.getMenuId());
        SysUserFavorite result = favoriteService.toggleFavorite(userId, fav.getName(), fav.getPath(), fav.getIcon(), fav.getMenuId());
        // Map.of() 不允许 null 值，取消收藏时 result 为 null 会导致 NPE，改用 HashMap
        Map<String, Object> data = new HashMap<>();
        data.put("collected", result != null);
        if (result != null) data.put("id", result.getId());
        return Result.ok(data);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消收藏")
    public Result<Void> delete(@PathVariable Long id) {
        favoriteService.removeById(id);
        return Result.ok();
    }

    @PutMapping("/sort")
    @Operation(summary = "排序收藏")
    public Result<Void> sort(@RequestBody Map<String, List<Long>> body) {
        favoriteService.updateSort(body.get("ids"));
        return Result.ok();
    }
}
