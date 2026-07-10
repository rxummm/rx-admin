package com.rx.admin.modules.system.dept.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.system.dept.entity.SysDept;
import com.rx.admin.modules.system.dept.dto.DeptCreateDTO;
import com.rx.admin.modules.system.dept.dto.DeptUpdateDTO;

import java.util.List;

public interface ISysDeptService extends IService<SysDept> {

    List<SysDept> getDeptTree();

    void addDept(DeptCreateDTO dto);

    void updateDept(DeptUpdateDTO dto);

    List<Long> getChildrenDeptIds(Long deptId);

    void deleteDept(Long id);

    void deleteDeptBatch(List<Long> ids);
}