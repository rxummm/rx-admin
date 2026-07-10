package com.rx.admin.modules.system.dept.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.system.dept.entity.SysDept;
import com.rx.admin.modules.system.dept.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织架构树形管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptTreeService {

    private final SysDeptMapper deptMapper;

    /**
     * 获取部门树
     */
    public List<Map<String, Object>> getDeptTree() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getStatus, 1)
               .orderByAsc(SysDept::getSort);
        List<SysDept> allDepts = deptMapper.selectList(wrapper);
        
        return buildTree(allDepts, 0L);
    }

    /**
     * 构建树形结构
     */
    private List<Map<String, Object>> buildTree(List<SysDept> allDepts, Long parentId) {
        return allDepts.stream()
                .filter(dept -> parentId.equals(dept.getParentId()))
                .map(dept -> {
                    Map<String, Object> node = new LinkedHashMap<>();
                    node.put("id", dept.getId());
                    node.put("name", dept.getDeptName());
                    node.put("parentId", dept.getParentId());
                    node.put("sort", dept.getSort());
                    node.put("status", dept.getStatus());
                    node.put("createTime", dept.getCreateTime());
                    
                    List<Map<String, Object>> children = buildTree(allDepts, dept.getId());
                    if (!children.isEmpty()) {
                        node.put("children", children);
                    }
                    return node;
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新部门排序
     */
    public void updateSort(Long deptId, Integer newSort) {
        SysDept dept = deptMapper.selectById(deptId);
        if (dept == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        dept.setSort(newSort);
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.updateById(dept);
    }

    /**
     * 批量更新部门排序
     */
    public void batchUpdateSort(List<Map<String, Object>> sortData) {
        for (Map<String, Object> item : sortData) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer sort = Integer.valueOf(item.get("sort").toString());
            updateSort(id, sort);
        }
    }

    /**
     * 导出部门数据
     */
    public List<Map<String, Object>> exportDepts() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDept::getParentId, SysDept::getSort);
        List<SysDept> depts = deptMapper.selectList(wrapper);
        
        return depts.stream().map(dept -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", dept.getId());
            row.put("deptName", dept.getDeptName());
            row.put("parentId", dept.getParentId());
            row.put("sort", dept.getSort());
            row.put("status", dept.getStatus());
            row.put("createTime", dept.getCreateTime());
            return row;
        }).collect(Collectors.toList());
    }

    /**
     * 导入部门数据
     */
    public int importDepts(List<Map<String, Object>> deptData) {
        int count = 0;
        for (Map<String, Object> row : deptData) {
            SysDept dept = new SysDept();
            dept.setDeptName(row.get("deptName").toString());
            dept.setParentId(row.get("parentId") != null ? Long.valueOf(row.get("parentId").toString()) : 0L);
            dept.setSort(row.get("sort") != null ? Integer.valueOf(row.get("sort").toString()) : 0);
            dept.setStatus(row.get("status") != null ? Integer.valueOf(row.get("status").toString()) : 1);
            dept.setCreateTime(LocalDateTime.now());
            dept.setUpdateTime(LocalDateTime.now());
            deptMapper.insert(dept);
            count++;
        }
        log.info("导入部门数据: {} 条", count);
        return count;
    }
}
