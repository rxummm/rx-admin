package com.rx.admin.modules.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.system.dict.entity.SysDictData;
import com.rx.admin.modules.system.dict.dto.DictDataCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictDataUpdateDTO;

import java.util.List;

public interface ISysDictDataService extends IService<SysDictData> {

    List<SysDictData> listByTypeId(Long typeId);

    List<SysDictData> getByDictType(String dictType);

    void addDictData(DictDataCreateDTO dto);

    void updateDictData(DictDataUpdateDTO dto);
}