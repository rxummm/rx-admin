package com.rx.admin.modules.monitor.dataVersion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.monitor.dataVersion.entity.SysDataSnapshot;
import com.rx.admin.modules.monitor.dataVersion.mapper.SysDataSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据对比分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataComparisonService {

    private final SysDataSnapshotMapper snapshotMapper;

    /**
     * 对比两个时间点的数据差异
     */
    public Map<String, Object> compareData(String tableName, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 获取时间范围内的快照
        LambdaQueryWrapper<SysDataSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDataSnapshot::getTableName, tableName)
               .ge(SysDataSnapshot::getOperateTime, startTime)
               .le(SysDataSnapshot::getOperateTime, endTime)
               .orderByAsc(SysDataSnapshot::getOperateTime);
        
        List<SysDataSnapshot> snapshots = snapshotMapper.selectList(wrapper);
        
        // 统计操作类型
        Map<String, Long> operationStats = snapshots.stream()
                .collect(Collectors.groupingBy(SysDataSnapshot::getOperationType, Collectors.counting()));
        
        result.put("tableName", tableName);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("totalOperations", snapshots.size());
        result.put("operationStats", operationStats);
        
        // 获取变更的记录ID
        Set<Long> changedRecords = snapshots.stream()
                .map(SysDataSnapshot::getRecordId)
                .collect(Collectors.toSet());
        result.put("changedRecordCount", changedRecords.size());
        result.put("changedRecordIds", new ArrayList<>(changedRecords));
        
        return result;
    }

    /**
     * 获取数据变更趋势
     */
    public List<Map<String, Object>> getChangeTrend(String tableName, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<SysDataSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDataSnapshot::getTableName, tableName)
               .ge(SysDataSnapshot::getOperateTime, startDate)
               .orderByAsc(SysDataSnapshot::getOperateTime);
        
        List<SysDataSnapshot> snapshots = snapshotMapper.selectList(wrapper);
        
        // 按日期分组
        Map<String, List<SysDataSnapshot>> groupedByDate = snapshots.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getOperateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        
        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<String, List<SysDataSnapshot>> entry : groupedByDate.entrySet()) {
            Map<String, Object> dayData = new LinkedHashMap<>();
            dayData.put("date", entry.getKey());
            dayData.put("count", entry.getValue().size());
            
            // 按操作类型统计
            Map<String, Long> typeStats = entry.getValue().stream()
                    .collect(Collectors.groupingBy(SysDataSnapshot::getOperationType, Collectors.counting()));
            dayData.put("insertCount", typeStats.getOrDefault("INSERT", 0L));
            dayData.put("updateCount", typeStats.getOrDefault("UPDATE", 0L));
            dayData.put("deleteCount", typeStats.getOrDefault("DELETE", 0L));
            
            trend.add(dayData);
        }
        
        return trend;
    }
}
