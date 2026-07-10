package com.rx.admin.modules.system.user.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface SysUserRoleMapper {

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insert(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    java.util.List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT user_id FROM sys_user_role WHERE role_id = #{roleId}")
    java.util.List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    @Insert("<script>INSERT INTO sys_user_role (user_id, role_id) VALUES "
            + "<foreach item='roleId' collection='roleIds' separator=','>(#{userId}, #{roleId})</foreach></script>")
    int insertBatch(@Param("userId") Long userId, @Param("roleIds") java.util.List<Long> roleIds);

    @Delete("<script>DELETE FROM sys_user_role WHERE user_id IN "
            + "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int deleteByUserIds(@Param("userIds") java.util.List<Long> userIds);

    @Delete("<script>DELETE FROM sys_user_role WHERE role_id IN "
            + "<foreach item='id' collection='roleIds' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int deleteByRoleIds(@Param("roleIds") java.util.List<Long> roleIds);
}
