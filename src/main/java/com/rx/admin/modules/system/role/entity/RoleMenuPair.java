package com.rx.admin.modules.system.role.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuPair {
    private Long roleId;
    private Long menuId;
}
