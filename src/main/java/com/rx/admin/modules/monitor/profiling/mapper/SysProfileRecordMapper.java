package com.rx.admin.modules.monitor.profiling.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.monitor.profiling.entity.SysProfileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysProfileRecordMapper extends BaseMapper<SysProfileRecord> {

    @Select("SELECT className, methodName, COUNT(*) as callCount, AVG(executionTime) as avgTime, MAX(executionTime) as maxTime, SUM(executionTime) as totalTime FROM sys_profile_record GROUP BY className, methodName ORDER BY totalTime DESC LIMIT 20")
    List<Map<String, Object>> getSlowMethods();

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count, AVG(executionTime) as avgTime FROM sys_profile_record WHERE create_time >= #{startDate} GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> getDailyStats(String startDate);
}
