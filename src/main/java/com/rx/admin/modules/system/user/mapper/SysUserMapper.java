package com.rx.admin.modules.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.system.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT DISTINCT r.role_code FROM sys_user_role ur " +
            "JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT m.perms FROM sys_user_role ur " +
            "JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "JOIN sys_menu m ON rm.menu_id = m.id " +
            "WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms != '' " +
            "AND m.status = 1 AND m.deleted = 0")
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT perms FROM sys_menu WHERE perms IS NOT NULL AND perms != '' AND status = 1 AND deleted = 0")
    List<String> selectAllValidPerms();
}
