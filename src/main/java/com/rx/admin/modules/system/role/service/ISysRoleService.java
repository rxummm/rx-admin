package com.rx.admin.modules.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.system.role.entity.SysRole;
import com.rx.admin.modules.system.role.dto.RoleCreateDTO;
import com.rx.admin.modules.system.role.dto.RoleUpdateDTO;

import java.util.List;

public interface ISysRoleService extends IService<SysRole> {

    SysRole getByCode(String roleCode);

    List<SysRole> listAll();

    void addRole(RoleCreateDTO dto);

    void updateRole(RoleUpdateDTO dto);

    void deleteRole(Long id);

    void deleteRoleBatch(List<Long> ids);
}