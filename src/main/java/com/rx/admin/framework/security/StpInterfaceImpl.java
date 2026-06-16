package com.rx.admin.framework.security;

import cn.dev33.satoken.stp.StpInterface;
import com.rx.admin.modules.system.user.mapper.SysUserMapper;
import com.rx.admin.modules.system.user.mapper.SysUserMenuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Sa-Token 权限加载接口实现
 * 每次鉴权时自动调用，从数据库加载当前用户的角色和权限
 *
 * 权限来源 = 角色权限（sys_role_menu） ∪ 直接授权权限（sys_user_menu）
 * 角色来源 = 用户角色（sys_user_role）
 */
@Slf4j
@Component
@SuppressWarnings("null")
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper userMapper;
    private final SysUserMenuMapper sysUserMenuMapper;

    public StpInterfaceImpl(SysUserMapper userMapper, SysUserMenuMapper sysUserMenuMapper) {
        this.userMapper = userMapper;
        this.sysUserMenuMapper = sysUserMenuMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());

        // admin 角色直接拥有所有权限，无需逐个关联菜单
        try {
            List<String> roles = getRoleList(loginId, loginType);
            if (roles != null && roles.contains("admin")) {
                return List.of("*");
            }
        } catch (Exception e) {
            log.warn("获取角色列表失败: {}", e.getMessage());
        }

        // 角色权限（通过 sys_role_menu）
        List<String> rolePerms = userMapper.selectPermsByUserId(userId);

        // 直接授权权限（通过 sys_user_menu）
        List<String> directPerms = sysUserMenuMapper.selectPermsByUserId(userId);

        // 合并去重
        Set<String> allPerms = new LinkedHashSet<>();
        if (rolePerms != null) allPerms.addAll(rolePerms);
        if (directPerms != null) allPerms.addAll(directPerms);

        return new ArrayList<>(allPerms);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = userMapper.selectRoleCodesByUserId(Long.valueOf(loginId.toString()));
        return roles != null ? roles : new ArrayList<>();
    }
}