package com.rx.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.entity.SysMenu;
import com.rx.admin.mapper.SysMenuMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    /**
     * 不可申请的顶级菜单ID（通过配置注入，默认：系统管理1、系统工具24、内容管理30、系统监控36）
     */
    @org.springframework.beans.factory.annotation.Value("${app.menu.excluded-top-ids:1,24,30,36}")
    private String excludedTopIdsStr;

    /**
     * 排除的权限申请菜单ID（通过配置注入，默认300）
     */
    @org.springframework.beans.factory.annotation.Value("${app.menu.excluded-permission-menu-id:300}")
    private Long excludedPermissionMenuId;

    private Set<Long> getExcludedTopIds() {
        return Arrays.stream(excludedTopIdsStr.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    /**
     * 获取当前用户的路由菜单（树形结构，Caffeine 缓存，按 userId 隔离）
     */
    @Cacheable(value = "menu", key = "'router_' + T(cn.dev33.satoken.stp.StpUtil).getLoginIdAsLong()")
    public List<SysMenu> getRouterMenus() {
        long userId = StpUtil.getLoginIdAsLong();
        List<SysMenu> menus;
        // admin 角色拥有所有菜单
        if (StpUtil.hasRole("admin")) {
            menus = list(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getStatus, 1)
                    .in(SysMenu::getMenuType, 1, 2)
                    .ne(SysMenu::getId, excludedPermissionMenuId)
                    .orderByAsc(SysMenu::getSort));
        } else {
            // 普通用户：查询关联的菜单及所有祖先节点，确保 buildTree 能构建完整树
            menus = baseMapper.selectMenusByUserId(userId)
                    .stream()
                    .filter(m -> m.getMenuType() == 1 || m.getMenuType() == 2)
                    .filter(m -> !m.getId().equals(excludedPermissionMenuId))
                    .collect(Collectors.toList());
        }
        return buildTree(menus, 0L);
    }

    /**
     * 获取所有菜单树（管理用，Caffeine 缓存）
     */
    @Cacheable(value = "menu", key = "'allTree'")
    public List<SysMenu> getAllMenuTree() {
        List<SysMenu> menus = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        return buildTree(menus, 0L);
    }

    /**
     * 获取普通用户可申请的菜单树（Caffeine 缓存，按 userId 隔离）
     * 排除：系统管理(1)、系统工具(24)、内容管理(30)、系统监控(36) 及其所有子菜单
     * 也排除用户已有权限的叶子菜单（但不排除目录节点，以便子节点仍可申请）
     * 包含按钮权限（type=3），让用户可以精确申请查看/编辑/删除等操作权限
     */
    @Cacheable(value = "menu", key = "'requestable_' + T(cn.dev33.satoken.stp.StpUtil).getLoginIdAsLong()")
    public List<SysMenu> getRequestableMenus() {
        long userId = StpUtil.getLoginIdAsLong();

        // 获取所有启用菜单
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));

        // 收集所有被排除的菜单ID（管理类顶级菜单及其子孙）
        Set<Long> excludedIds = new HashSet<>();
        for (Long topId : getExcludedTopIds()) {
            collectDescendantIds(allMenus, topId, excludedIds);
        }

        // 收集用户已有权限：只排除已拥有的按钮（type=3），
        // 保留菜单页面（type=2）和目录（type=1），以便用户看到已有菜单页下可额外申请的按钮权限
        Set<Long> ownedIds = new HashSet<>();
        if (!StpUtil.hasRole("admin")) {
            List<SysMenu> userMenus = baseMapper.selectMenusByUserId(userId);
            for (SysMenu m : userMenus) {
                // 只排除 type=3（按钮），保留 type=1（目录）、type=2（菜单页面）用于构建树
                if (m.getMenuType() == 3) {
                    ownedIds.add(m.getId());
                }
            }
        }

        // 过滤：排除管理类菜单、用户已有菜单（展示目录、菜单页面和按钮权限）
        List<SysMenu> requestable = allMenus.stream()
                .filter(m -> !excludedIds.contains(m.getId()))
                .filter(m -> !ownedIds.contains(m.getId()))
                .filter(m -> m.getMenuType() == 1 || m.getMenuType() == 2 || m.getMenuType() == 3)
                .collect(Collectors.toList());

        return buildTree(requestable, 0L);
    }

    /**
     * 递归收集指定菜单ID的所有子孙ID
     */
    private void collectDescendantIds(List<SysMenu> menus, Long parentId, Set<Long> result) {
        result.add(parentId);
        for (SysMenu m : menus) {
            if (m.getParentId().equals(parentId)) {
                collectDescendantIds(menus, m.getId(), result);
            }
        }
    }

    private List<SysMenu> buildTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                List<SysMenu> children = buildTree(menus, menu.getId());
                if (!children.isEmpty()) {
                    menu.setChildren(children);
                }
                tree.add(menu);
            }
        }
        return tree;
    }
}
