package com.rx.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.entity.SysDept;
import com.rx.admin.entity.SysRole;
import com.rx.admin.entity.SysUser;
import com.rx.admin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限服务
 * 提供获取当前用户可见部门ID列表的方法
 */
@Service
@RequiredArgsConstructor
public class DataScopeService {

    private final SysUserMapper userMapper;
    private final SysRoleService roleService;
    private final SysDeptService deptService;

    /**
     * 获取当前用户的数据权限范围
     * DATA_ALL=1  全部
     * DATA_DEPT=2 本部门
     * DATA_DEPT_TREE=3 本部门及下级
     * DATA_SELF=4 仅本人
     * DATA_CUSTOM=5 自定义部门
     */
    public List<Long> getVisibleDeptIds() {
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取用户角色列表
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(userId);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }

        int maxScope = 1; // 默认全部
        List<Long> customDeptIds = new ArrayList<>();
        
        for (String code : roleCodes) {
            SysRole role = roleService.getByCode(code);
            if (role == null) continue;
            Integer scope = role.getDataScope();
            if (scope == null) continue;
            
            if (scope == 1) return null; // 全部权限，不需要过滤
            if (scope > maxScope) maxScope = scope;
            if (scope == 5 && role.getDataDeptIds() != null) {
                customDeptIds.addAll(Arrays.stream(role.getDataDeptIds().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .map(Long::parseLong).collect(Collectors.toList()));
            }
        }

        return switch (maxScope) {
            case 2 -> { // 本部门
                SysUser user = userMapper.selectById(userId);
                yield user != null && user.getDeptId() != null ? List.of(user.getDeptId()) : List.of();
            }
            case 3 -> { // 本部门及下级
                SysUser user = userMapper.selectById(userId);
                if (user != null && user.getDeptId() != null) {
                    yield deptService.getChildrenDeptIds(user.getDeptId());
                }
                yield List.of();
            }
            case 4 -> List.of(); // 仅本人（通过userColumn过滤）
            case 5 -> customDeptIds;
            default -> null;
        };
    }
}
