package com.rx.admin.modules.system.role.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface SysRoleMenuMapper {

    @Insert("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (#{roleId}, #{menuId})")
    int insert(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    java.util.List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    @Select("<script>SELECT role_id, menu_id FROM sys_role_menu WHERE role_id IN "
            + "<foreach item='id' collection='roleIds' open='(' separator=',' close=')'>#{id}</foreach></script>")
    java.util.List<com.rx.admin.modules.system.role.entity.RoleMenuPair> selectMenuIdsByRoleIds(@Param("roleIds") java.util.List<Long> roleIds);

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId}")
    int deleteByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    @Insert("<script>INSERT INTO sys_role_menu (role_id, menu_id) VALUES "
            + "<foreach item='menuId' collection='menuIds' separator=','>(#{roleId}, #{menuId})</foreach></script>")
    int insertBatch(@Param("roleId") Long roleId, @Param("menuIds") java.util.List<Long> menuIds);

    @Delete("<script>DELETE FROM sys_role_menu WHERE role_id IN "
            + "<foreach item='id' collection='roleIds' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int deleteByRoleIds(@Param("roleIds") java.util.List<Long> roleIds);
}
