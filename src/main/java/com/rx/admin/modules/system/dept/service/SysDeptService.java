package com.rx.admin.modules.system.dept.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.utils.TreeUtils;
import com.rx.admin.modules.system.dept.entity.SysDept;
import com.rx.admin.modules.system.dept.mapper.SysDeptMapper;
import com.rx.admin.modules.system.dept.dto.DeptCreateDTO;
import com.rx.admin.modules.system.dept.dto.DeptUpdateDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@SuppressWarnings("null")
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    public List<SysDept> getDeptTree() {
        List<SysDept> all = list(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSort));
        return TreeUtils.buildTree(
                all,
                SysDept::getId,
                SysDept::getParentId,
                SysDept::setChildren
        );
    }

    public void addDept(DeptCreateDTO dto) {
        SysDept dept = new SysDept();
        dept.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        dept.setDeptName(dto.getDeptName());
        dept.setLeader(dto.getLeader());
        dept.setPhone(dto.getPhone());
        dept.setEmail(dto.getEmail());
        dept.setSort(dto.getSort());
        dept.setStatus(dto.getStatus());
        save(dept);
    }

    public void updateDept(DeptUpdateDTO dto) {
        SysDept dept = getById(dto.getId());
        if (dept == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        if (dto.getParentId() != null) dept.setParentId(dto.getParentId());
        if (StringUtils.hasText(dto.getDeptName())) dept.setDeptName(dto.getDeptName());
        if (StringUtils.hasText(dto.getLeader())) dept.setLeader(dto.getLeader());
        if (StringUtils.hasText(dto.getPhone())) dept.setPhone(dto.getPhone());
        if (StringUtils.hasText(dto.getEmail())) dept.setEmail(dto.getEmail());
        if (dto.getSort() != null) dept.setSort(dto.getSort());
        if (dto.getStatus() != null) dept.setStatus(dto.getStatus());
        updateById(dept);
    }

    /**
     * 获取指定部门及其所有子部门的ID列表
     */
    public List<Long> getChildrenDeptIds(Long deptId) {
        List<SysDept> all = list();
        return TreeUtils.collectDescendantIds(
                deptId,
                all,
                SysDept::getId,
                SysDept::getParentId
        );
    }

    public void deleteDept(Long id) {
        // 检查是否有子部门
        long count = count(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (count > 0) {
            throw new IllegalArgumentException("存在子部门，无法删除");
        }
        removeById(id);
    }

    @Override
    public void deleteDeptBatch(List<Long> ids) {
        for (Long id : ids) {
            deleteDept(id);
        }
    }
}
