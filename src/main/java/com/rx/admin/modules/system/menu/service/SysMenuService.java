package com.rx.admin.modules.system.menu.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.exception.BusinessException;
import com.rx.admin.common.exception.ErrorCode;
import com.rx.admin.common.utils.TreeUtils;
import com.rx.admin.modules.system.menu.entity.SysMenu;
import com.rx.admin.modules.system.menu.mapper.SysMenuMapper;
import com.rx.admin.modules.system.menu.dto.MenuCreateDTO;
import com.rx.admin.modules.system.menu.dto.MenuUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final CacheManager cacheManager;

    @org.springframework.beans.factory.annotation.Value("${app.menu.excluded-top-ids:1,24,30,36}")
    private String excludedTopIdsStr;

    @org.springframework.beans.factory.annotation.Value("${app.menu.excluded-permission-menu-id:300}")
    private Long excludedPermissionMenuId;

    public SysMenuService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private Set<Long> getExcludedTopIds() {
        return Arrays.stream(excludedTopIdsStr.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    private void clearMenuCache() {
        Cache cache = cacheManager.getCache("menu");
        if (cache != null) {
            cache.clear();
            log.debug("Menu cache cleared");
        }
    }

    @Cacheable(value = "menu", key = "'router_' + T(cn.dev33.satoken.stp.StpUtil).getLoginIdAsLong()")
    public List<SysMenu> getRouterMenus() {
        long userId = StpUtil.getLoginIdAsLong();
        List<SysMenu> menus;
        if (StpUtil.hasRole("admin")) {
            menus = list(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getStatus, 1)
                    .in(SysMenu::getMenuType, 1, 2)
                    .ne(SysMenu::getId, excludedPermissionMenuId)
                    .orderByAsc(SysMenu::getSort));
        } else {
            menus = baseMapper.selectMenusByUserId(userId)
                    .stream()
                    .filter(m -> m.getMenuType() == 1 || m.getMenuType() == 2)
                    .filter(m -> !m.getId().equals(excludedPermissionMenuId))
                    .collect(Collectors.toList());
        }
        return TreeUtils.buildTree(
                menus,
                SysMenu::getId,
                SysMenu::getParentId,
                SysMenu::setChildren
        );
    }

    @Cacheable(value = "menu", key = "'allTree'")
    public List<SysMenu> getAllMenuTree() {
        List<SysMenu> menus = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        return TreeUtils.buildTree(
                menus,
                SysMenu::getId,
                SysMenu::getParentId,
                SysMenu::setChildren
        );
    }

    @Cacheable(value = "menu", key = "'requestable_' + T(cn.dev33.satoken.stp.StpUtil).getLoginIdAsLong()")
    public List<SysMenu> getRequestableMenus() {
        long userId = StpUtil.getLoginIdAsLong();

        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));

        Set<Long> excludedIds = new HashSet<>();
        for (Long topId : getExcludedTopIds()) {
            excludedIds.addAll(TreeUtils.collectDescendantIds(topId, allMenus, SysMenu::getId, SysMenu::getParentId));
        }

        Set<Long> ownedIds = new HashSet<>();
        if (!StpUtil.hasRole("admin")) {
            List<SysMenu> userMenus = baseMapper.selectMenusByUserId(userId);
            for (SysMenu m : userMenus) {
                if (m.getMenuType() == 3) {
                    ownedIds.add(m.getId());
                }
            }
        }

        List<SysMenu> requestable = allMenus.stream()
                .filter(m -> !excludedIds.contains(m.getId()))
                .filter(m -> !ownedIds.contains(m.getId()))
                .filter(m -> m.getMenuType() == 1 || m.getMenuType() == 2 || m.getMenuType() == 3)
                .collect(Collectors.toList());

        return TreeUtils.buildTree(
                requestable,
                SysMenu::getId,
                SysMenu::getParentId,
                SysMenu::setChildren
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void addMenu(MenuCreateDTO dto) {
        log.info("新增菜单，参数：menuName={}, parentId={}", dto.getMenuName(), dto.getParentId());
        try {
            SysMenu menu = new SysMenu();
            menu.setParentId(dto.getParentId());
            menu.setMenuName(dto.getMenuName());
            menu.setMenuType(dto.getMenuType());
            menu.setPath(dto.getPath());
            menu.setComponent(dto.getComponent());
            menu.setPerms(dto.getPerms());
            menu.setIcon(dto.getIcon());
            menu.setSort(dto.getSort());
            menu.setVisible(dto.getVisible());
            menu.setStatus(dto.getStatus());
            save(menu);
            clearMenuCache();
            log.info("新增菜单成功，菜单ID={}", menu.getId());
        } catch (Exception e) {
            log.error("新增菜单失败，参数：{}", dto, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuUpdateDTO dto) {
        log.info("更新菜单，参数：id={}", dto.getId());
        try {
            SysMenu menu = getById(dto.getId());
            if (menu == null) {
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }
            if (dto.getParentId() != null) menu.setParentId(dto.getParentId());
            if (StringUtils.hasText(dto.getMenuName())) menu.setMenuName(dto.getMenuName());
            if (dto.getMenuType() != null) menu.setMenuType(dto.getMenuType());
            if (StringUtils.hasText(dto.getPath())) menu.setPath(dto.getPath());
            if (StringUtils.hasText(dto.getComponent())) menu.setComponent(dto.getComponent());
            if (StringUtils.hasText(dto.getPerms())) menu.setPerms(dto.getPerms());
            if (StringUtils.hasText(dto.getIcon())) menu.setIcon(dto.getIcon());
            if (dto.getSort() != null) menu.setSort(dto.getSort());
            if (dto.getVisible() != null) menu.setVisible(dto.getVisible());
            if (dto.getStatus() != null) menu.setStatus(dto.getStatus());
            updateById(menu);
            clearMenuCache();
            log.info("更新菜单成功，菜单ID={}", dto.getId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新菜单失败，参数：{}", dto, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMenuBatch(List<Long> ids) {
        removeByIds(ids);
        clearMenuCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeMenu(Long id) {
        log.info("删除菜单，参数：id={}", id);
        try {
            SysMenu menu = getById(id);
            if (menu == null) {
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }

            List<SysMenu> children = list(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getParentId, id));
            if (!children.isEmpty()) {
                throw new BusinessException(ErrorCode.MENU_HAS_CHILDREN);
            }

            removeById(id);
            clearMenuCache();
            log.info("删除菜单成功，菜单ID={}", id);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除菜单失败，参数：id={}", id, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
