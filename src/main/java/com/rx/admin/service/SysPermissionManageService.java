package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.entity.SysMenu;
import com.rx.admin.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户权限管理 Service
 * 支持 admin 主动管理任意用户的菜单权限（增/删/查）
 *
 * 权限数据存储策略：
 * - 公共权限（仪表盘等）→ 通过 user 角色（role_id=2）控制
 * - 个性化权限 → 通过 sys_user_menu 表直接关联用户和菜单
 * - 查询时合并两部分（角色权限 + 直接授权权限）
 *
 * 不再为每个用户创建独立角色 user_{userId}，避免角色冗余
 */
@Service
public class SysPermissionManageService {

    private static final Logger log = LoggerFactory.getLogger(SysPermissionManageService.class);

    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysUserMenuMapper sysUserMenuMapper;
    private final CacheManager cacheManager;

    public SysPermissionManageService(SysRoleMenuMapper sysRoleMenuMapper,
                                       SysUserRoleMapper sysUserRoleMapper,
                                       SysMenuMapper sysMenuMapper,
                                       SysUserMenuMapper sysUserMenuMapper,
                                       CacheManager cacheManager) {
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysUserMenuMapper = sysUserMenuMapper;
        this.cacheManager = cacheManager;
    }

    /**
     * 获取用户已有的菜单ID列表（角色权限 + 直接授权权限）
     */
    public Set<Long> getUserMenuIds(Long userId) {
        Set<Long> menuIds = new HashSet<>();

        // 角色权限
        List<Long> roleIds = sysUserRoleMapper.selectRoleIdsByUserId(userId);
        for (Long roleId : roleIds) {
            menuIds.addAll(sysRoleMenuMapper.selectMenuIdsByRoleId(roleId));
        }

        // 直接授权权限
        menuIds.addAll(sysUserMenuMapper.selectMenuIdsByUserId(userId));

        return menuIds;
    }

    /**
     * 获取用户直接授权的菜单ID列表（仅 sys_user_menu，不含角色权限）
     */
    public Set<Long> getUserDirectMenuIds(Long userId) {
        return new HashSet<>(sysUserMenuMapper.selectMenuIdsByUserId(userId));
    }

    /**
     * 给用户添加菜单/按钮权限（写入 sys_user_menu 表）
     * 精确写入 admin 勾选的每个节点（支持菜单 type=2 和按钮 type=3 的任意组合）
     * 不再自动追加子孙按钮，由 admin 在前端树中精确勾选需要的权限
     *
     * 典型用法：
     * - 勾选"红楼诗词"菜单 + "查询"按钮 → 用户只能查看该页面
     * - 勾选"红楼诗词"菜单 + "查询"按钮 + "编辑"按钮 → 用户可查看和编辑
     */
    @Transactional
    public void addUserMenus(Long userId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return;

        for (Long menuId : menuIds) {
            try {
                sysUserMenuMapper.insert(userId, menuId);
            } catch (Exception ignored) {
                // 忽略重复插入（唯一约束 uk_user_menu）
            }
        }
        evictUserMenuCache(userId);
    }

    /**
     * 设置用户菜单权限（替换模式）
     * 清空用户所有直接授权，然后写入勾选的菜单ID
     * 只保留选中的权限，取消勾选的会被移除
     * 注意：不会影响通过角色获得的权限
     */
    @Transactional
    public void setUserMenus(Long userId, List<Long> menuIds) {
        // 先清空所有直接授权
        sysUserMenuMapper.deleteByUserId(userId);
        // 再写入勾选的
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                try {
                    sysUserMenuMapper.insert(userId, menuId);
                } catch (Exception ignored) {
                    // 忽略重复插入
                }
            }
        }
        evictUserMenuCache(userId);
    }

    /**
     * 移除用户的指定菜单权限（从 sys_user_menu 中删除）
     * 对目录（type=1）和菜单页面（type=2）会自动移除其所有子孙权限
     * 对按钮（type=3）只移除自身，避免误删兄弟节点的权限
     * 注意：不会删除通过角色获得的权限
     */
    @Transactional
    public void removeUserMenus(Long userId, List<Long> menuIds) {
        List<SysMenu> allMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1));
        Set<Long> toRemove = new HashSet<>();
        for (Long menuId : menuIds) {
            toRemove.add(menuId);
            // 对目录(type=1)和菜单页面(type=2)才递归删除子孙，按钮(type=3)只删自身
            SysMenu menu = allMenus.stream().filter(m -> m.getId().equals(menuId)).findFirst().orElse(null);
            if (menu == null || menu.getMenuType() == 1 || menu.getMenuType() == 2) {
                collectDescendantIds(allMenus, menuId, toRemove);
            }
        }

        for (Long menuId : toRemove) {
            sysUserMenuMapper.deleteByUserIdAndMenuId(userId, menuId);
        }
        evictUserMenuCache(userId);
    }

    /**
     * 清除指定用户的菜单缓存，使权限变更立即生效
     */
    private void evictUserMenuCache(Long userId) {
        try {
            var cache = cacheManager.getCache("menu");
            if (cache != null) {
                cache.evict("router_" + userId);
                cache.evict("requestable_" + userId);
                log.debug("已清除用户 {} 的菜单缓存", userId);
            }
        } catch (Exception ignored) {
            // 缓存清除失败不影响主流程
        }
    }

    /**
     * 递归收集子孙菜单ID
     */
    private void collectDescendantIds(List<SysMenu> allMenus, Long parentId, Set<Long> result) {
        for (SysMenu m : allMenus) {
            if (m.getParentId().equals(parentId)) {
                result.add(m.getId());
                collectDescendantIds(allMenus, m.getId(), result);
            }
        }
    }


    /**
     * 获取用户可分配的权限树（包含按钮权限）
     * 排除系统管理、工具、内容管理、监控等管理类菜单
     * 保留已拥有的目录节点（type=1）作为树结构占位，使其下的按钮（type=3）仍可被分配
     * 已拥有的菜单页面（type=2）保持隐藏
     */
    public List<SysMenu> getManageableMenuTree(Long userId) {
        // 用户已有菜单ID（角色 + 直接授权）
        Set<Long> ownedIds = getUserMenuIds(userId);

        // 排除的管理类顶级菜单
        Set<Long> excludedTopIds = Set.of(1L, 24L, 30L, 36L);
        Set<Long> excludedIds = new HashSet<>();
        List<SysMenu> allMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));

        for (Long topId : excludedTopIds) {
            collectDescendantIds(allMenus, topId, excludedIds);
        }

        // 过滤规则：
        // 1. 排除管理类菜单
        // 2. 排除权限申请菜单(300)
        // 3. 目录(type=1)始终保留——即使已拥有，作为树节点占位，使其子按钮仍可分配
        // 4. 按钮(type=3)始终保留——前端通过 setCheckedKeys 标记已选中
        // 5. 排除已拥有的菜单页面(type=2)
        List<SysMenu> available = allMenus.stream()
                .filter(m -> !excludedIds.contains(m.getId()))
                .filter(m -> m.getId() != 300L)
                .filter(m -> {
                    if (m.getMenuType() == 1) return true; // 目录始终保留
                    if (m.getMenuType() == 3) return true; // 按钮始终保留
                    return !ownedIds.contains(m.getId());  // 只排除已拥有的菜单页面
                })
                .collect(Collectors.toList());

        return buildTree(available, 0L);
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
