package com.rx.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询用户拥有的菜单（含祖先节点，用于构建菜单树）
     * 权限来源 = 角色权限（sys_role_menu）∪ 直接授权权限（sys_user_menu）
     */
    @Select("<script>" +
            "WITH RECURSIVE menu_tree AS (" +
            // 角色权限
            "  SELECT m.* FROM sys_menu m " +
            "  JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "  JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "  WHERE ur.user_id = #{userId} AND m.status = 1 AND m.deleted = 0 " +
            "  UNION " +
            // 直接授权权限
            "  SELECT m.* FROM sys_menu m " +
            "  JOIN sys_user_menu um ON m.id = um.menu_id " +
            "  WHERE um.user_id = #{userId} AND m.status = 1 AND m.deleted = 0 " +
            "  UNION " +
            // 向上递归查找所有祖先节点
            "  SELECT p.* FROM sys_menu p " +
            "  JOIN menu_tree mt ON p.id = mt.parent_id " +
            "  WHERE p.status = 1 AND p.deleted = 0" +
            ") SELECT DISTINCT * FROM menu_tree ORDER BY sort ASC" +
            "</script>")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
