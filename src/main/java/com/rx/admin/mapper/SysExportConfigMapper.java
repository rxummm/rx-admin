package com.rx.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.entity.SysExportConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 导出配置 Mapper
 */
@Mapper
public interface SysExportConfigMapper extends BaseMapper<SysExportConfig> {

    /**
     * 根据菜单路径查询导出配置
     */
    @Select("SELECT c.* FROM sys_export_config c " +
            "JOIN sys_menu m ON c.menu_id = m.id " +
            "WHERE m.path = #{path} AND c.enabled = 1 AND c.deleted = 0 AND m.deleted = 0 " +
            "LIMIT 1")
    SysExportConfig selectByMenuPath(@Param("path") String path);
}
