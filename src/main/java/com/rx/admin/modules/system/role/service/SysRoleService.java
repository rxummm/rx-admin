package com.rx.admin.modules.system.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.system.role.entity.SysRole;
import com.rx.admin.modules.system.role.mapper.SysRoleMapper;
import com.rx.admin.modules.system.role.mapper.SysRoleMenuMapper;
import com.rx.admin.modules.system.role.dto.RoleCreateDTO;
import com.rx.admin.modules.system.role.dto.RoleUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@SuppressWarnings("null")
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final CacheManager cacheManager;

    public SysRoleService(SysRoleMenuMapper roleMenuMapper, CacheManager cacheManager) {
        this.roleMenuMapper = roleMenuMapper;
        this.cacheManager = cacheManager;
    }

    /**
     * 根据角色编码获取角色
     */
    public SysRole getByCode(String roleCode) {
        return getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
    }

    public List<SysRole> listAll() {
        List<SysRole> roles = list(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSort));
        // 填充每个角色的菜单ID列表
        for (SysRole role : roles) {
            role.setMenuIds(roleMenuMapper.selectMenuIdsByRoleId(role.getId()));
        }
        return roles;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleCreateDTO dto) {
        long count = count(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode()));
        if (count > 0) {
            throw new IllegalArgumentException("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setSort(dto.getSort());
        role.setStatus(dto.getStatus());
        role.setDataScope(dto.getDataScope());
        role.setDataDeptIds(dto.getDataDeptIds());
        save(role);
        List<Long> menuIds = dto.getMenuIds();
        if (menuIds != null && !menuIds.isEmpty()) {
            menuIds.forEach(menuId -> roleMenuMapper.insert(role.getId(), menuId));
        }
        evictAllRouterCaches();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateDTO dto) {
        SysRole role = new SysRole();
        role.setId(dto.getId());
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setSort(dto.getSort());
        role.setStatus(dto.getStatus());
        role.setDataScope(dto.getDataScope());
        role.setDataDeptIds(dto.getDataDeptIds());
        updateById(role);
        // 先删除旧关联，再插入新关联（menuIds 为空则清空该角色的所有权限）
        roleMenuMapper.deleteByRoleId(role.getId());
        List<Long> menuIds = dto.getMenuIds();
        if (menuIds != null && !menuIds.isEmpty()) {
            menuIds.forEach(menuId -> roleMenuMapper.insert(role.getId(), menuId));
        }
        evictAllRouterCaches();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        roleMenuMapper.deleteByRoleId(id);
        removeById(id);
        evictAllRouterCaches();
    }

    /**
     * 清除所有用户的路由菜单缓存
     * 角色菜单变更会影响所有拥有该角色的用户，无法精确到单个用户，
     * 因此清除整个 menu 缓存命名空间，下次请求时重新加载
     */
    private void evictAllRouterCaches() {
        try {
            var cache = cacheManager.getCache("menu");
            if (cache != null) {
                cache.clear();
                log.info("已清除所有用户菜单缓存（角色菜单变更）");
            }
        } catch (Exception e) {
            log.warn("清除菜单缓存失败（不影响主流程）", e);
        }
    }

    @Override
    public void deleteRoleBatch(List<Long> ids) {
        for (Long id : ids) {
            deleteRole(id);
        }
    }
}
