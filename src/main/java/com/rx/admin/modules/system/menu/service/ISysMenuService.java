package com.rx.admin.modules.system.menu.service;

import com.rx.admin.modules.system.menu.dto.MenuCreateDTO;
import com.rx.admin.modules.system.menu.dto.MenuUpdateDTO;
import com.rx.admin.modules.system.menu.entity.SysMenu;

import java.util.List;

public interface ISysMenuService {

    List<SysMenu> getRouterMenus();

    List<SysMenu> getAllMenuTree();

    List<SysMenu> getRequestableMenus();

    void addMenu(MenuCreateDTO dto);

    void updateMenu(MenuUpdateDTO dto);

    void removeMenu(Long id);

    void deleteMenuBatch(List<Long> ids);
}
