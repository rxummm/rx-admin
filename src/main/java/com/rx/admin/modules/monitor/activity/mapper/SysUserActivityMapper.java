package com.rx.admin.modules.monitor.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.monitor.activity.entity.SysUserActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserActivityMapper extends BaseMapper<SysUserActivity> {

    @Select("SELECT activity_date as activityDate, hour, COUNT(*) as count FROM sys_user_activity WHERE activity_date BETWEEN #{startDate} AND #{endDate} GROUP BY activity_date, hour ORDER BY activity_date, hour")
    List<Map<String, Object>> getHeatmapData(LocalDate startDate, LocalDate endDate);

    @Select("SELECT activity_type as activityType, COUNT(*) as count FROM sys_user_activity WHERE activity_date BETWEEN #{startDate} AND #{endDate} GROUP BY activity_type")
    List<Map<String, Object>> getActivityTypeStats(LocalDate startDate, LocalDate endDate);

    @Select("SELECT user_id as userId, COUNT(*) as count FROM sys_user_activity WHERE activity_date BETWEEN #{startDate} AND #{endDate} GROUP BY user_id ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> getTopUsers(LocalDate startDate, LocalDate endDate);
}
