package com.rx.admin.modules.tool.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.tool.archive.dto.SysArchiveConfigCreateDTO;
import com.rx.admin.modules.tool.archive.entity.SysArchiveConfig;

import java.util.List;

public interface SysArchiveConfigService extends IService<SysArchiveConfig> {
    List<SysArchiveConfig> listAll();
    void addEntity(SysArchiveConfigCreateDTO dto);
}
