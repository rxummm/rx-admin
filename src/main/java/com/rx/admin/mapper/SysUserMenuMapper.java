package com.rx.admin.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户直接授权表 Mapper
 * 不通过角色，直接将菜单/按钮权限关联到用户，实现个性化权限控制
 */
@Mapper
public interface SysUserMenuMapper {

    @Insert("INSERT INTO sys_user_menu (user_id, menu_id) VALUES (#{userId}, #{menuId})")
    int insert(@Param("userId") Long userId, @Param("menuId") Long menuId);

    @Delete("DELETE FROM sys_user_menu WHERE user_id = #{userId} AND menu_id = #{menuId}")
    int deleteByUserIdAndMenuId(@Param("userId") Long userId, @Param("menuId") Long menuId);

    @Delete("DELETE FROM sys_user_menu WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Select("SELECT menu_id FROM sys_user_menu WHERE user_id = #{userId}")
    List<Long> selectMenuIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户直接授权的权限标识（perms）
     */
    @Select("SELECT DISTINCT m.perms FROM sys_user_menu um " +
            "JOIN sys_menu m ON um.menu_id = m.id " +
            "WHERE um.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms != '' " +
            "AND m.status = 1 AND m.deleted = 0")
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户直接授权的菜单（含祖先节点，用于构建菜单树）
     */
    @Select("<script>" +
            "WITH RECURSIVE menu_tree AS (" +
            "  SELECT m.* FROM sys_menu m " +
            "  JOIN sys_user_menu um ON m.id = um.menu_id " +
            "  WHERE um.user_id = #{userId} AND m.status = 1 AND m.deleted = 0 " +
            "  UNION " +
            "  SELECT p.* FROM sys_menu p " +
            "  JOIN menu_tree mt ON p.id = mt.parent_id " +
            "  WHERE p.status = 1 AND p.deleted = 0" +
            ") SELECT DISTINCT * FROM menu_tree ORDER BY sort ASC" +
            "</script>")
    List<com.rx.admin.entity.SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
