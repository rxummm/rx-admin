package com.rx.admin.modules.monitor.dataVersion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rx.admin.modules.monitor.dataVersion.entity.SysDataSnapshot;
import com.rx.admin.modules.monitor.dataVersion.mapper.SysDataSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据快照服务
 * 支持数据变更前快照和一键回滚
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSnapshotService {

    private final SysDataSnapshotMapper snapshotMapper;
    private final ObjectMapper objectMapper;

    /**
     * 保存数据快照
     */
    public void saveSnapshot(String tableName, Long recordId, String operationType,
                            Map<String, Object> beforeData, Map<String, Object> afterData,
                            Long operatorId, String operatorName) {
        try {
            SysDataSnapshot snapshot = new SysDataSnapshot();
            snapshot.setTableName(tableName);
            snapshot.setRecordId(recordId);
            snapshot.setOperationType(operationType);
            snapshot.setBeforeData(beforeData != null ? objectMapper.writeValueAsString(beforeData) : null);
            snapshot.setAfterData(afterData != null ? objectMapper.writeValueAsString(afterData) : null);
            snapshot.setOperatorId(operatorId);
            snapshot.setOperatorName(operatorName);
            snapshot.setOperateTime(LocalDateTime.now());
            snapshot.setRolledBack(0);
            
            snapshotMapper.insert(snapshot);
            log.info("保存数据快照: tableName={}, recordId={}, operation={}", tableName, recordId, operationType);
        } catch (Exception e) {
            log.error("保存数据快照失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取数据快照列表
     */
    public java.util.List<SysDataSnapshot> getSnapshots(String tableName, Long recordId) {
        LambdaQueryWrapper<SysDataSnapshot> wrapper = new LambdaQueryWrapper<>();
        if (tableName != null) {
            wrapper.eq(SysDataSnapshot::getTableName, tableName);
        }
        if (recordId != null) {
            wrapper.eq(SysDataSnapshot::getRecordId, recordId);
        }
        wrapper.orderByDesc(SysDataSnapshot::getOperateTime);
        return snapshotMapper.selectList(wrapper);
    }

    /**
     * 获取回滚数据
     */
    public Map<String, Object> getRollbackData(Long snapshotId) {
        SysDataSnapshot snapshot = snapshotMapper.selectById(snapshotId);
        if (snapshot == null) {
            throw new IllegalArgumentException("快照不存在");
        }
        
        try {
            if (snapshot.getBeforeData() != null) {
                return objectMapper.readValue(snapshot.getBeforeData(), Map.class);
            }
        } catch (Exception e) {
            log.error("解析回滚数据失败: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 标记为已回滚
     */
    public void markAsRolledBack(Long snapshotId) {
        SysDataSnapshot snapshot = snapshotMapper.selectById(snapshotId);
        if (snapshot != null) {
            snapshot.setRolledBack(1);
            snapshot.setRollbackTime(LocalDateTime.now());
            snapshotMapper.updateById(snapshot);
        }
    }
}
