package com.rx.admin.modules.monitor.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.monitor.log.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as error_count " +
            "FROM sys_log WHERE create_time >= #{startDate} AND create_time < #{endDate} " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> countByDateRange(String startDate, String endDate);

    @Select("SELECT COUNT(*) as total, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as error_count, " +
            "COUNT(DISTINCT username) as active_users " +
            "FROM sys_log WHERE create_time >= #{startDate}")
    Map<String, Object> summarySince(String startDate);

    @Select("SELECT HOUR(create_time) as hour, COUNT(*) as count " +
            "FROM sys_log WHERE create_time >= #{startDate} " +
            "GROUP BY HOUR(create_time) ORDER BY hour")
    List<Map<String, Object>> countByHour(String startDate);

    @Select("SELECT operation as type, COUNT(*) as count " +
            "FROM sys_log WHERE operation IS NOT NULL " +
            "GROUP BY operation ORDER BY count DESC")
    List<Map<String, Object>> countByOperation();
}
