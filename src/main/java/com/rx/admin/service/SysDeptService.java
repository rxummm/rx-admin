package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.entity.SysDept;
import com.rx.admin.mapper.SysDeptMapper;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> {

    public List<SysDept> getDeptTree() {
        List<SysDept> all = list(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSort));
        return buildTree(all, 0L);
    }

    private List<SysDept> buildTree(List<SysDept> all, Long parentId) {
        List<SysDept> tree = new ArrayList<>();
        for (SysDept dept : all) {
            if (dept.getParentId().equals(parentId)) {
                dept.setChildren(buildTree(all, dept.getId()));
                tree.add(dept);
            }
        }
        return tree;
    }

    public void addDept(SysDept dept) {
        if (dept.getParentId() == null) dept.setParentId(0L);
        save(dept);
    }

    public void updateDept(SysDept dept) {
        updateById(dept);
    }

    /**
     * 获取指定部门及其所有子部门的ID列表
     */
    public List<Long> getChildrenDeptIds(Long deptId) {
        List<Long> ids = new ArrayList<>();
        ids.add(deptId);
        collectChildrenIds(deptId, ids);
        return ids;
    }

    private void collectChildrenIds(Long parentId, List<Long> ids) {
        List<SysDept> children = list(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, parentId));
        for (SysDept child : children) {
            ids.add(child.getId());
            collectChildrenIds(child.getId(), ids);
        }
    }

    public void deleteDept(Long id) {
        // 检查是否有子部门
        long count = count(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (count > 0) {
            throw new IllegalArgumentException("存在子部门，无法删除");
        }
        removeById(id);
    }
}
