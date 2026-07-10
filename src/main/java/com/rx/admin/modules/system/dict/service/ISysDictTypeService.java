package com.rx.admin.modules.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.system.dict.entity.SysDictType;
import com.rx.admin.modules.system.dict.dto.DictTypeCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictTypeUpdateDTO;

import java.util.List;

public interface ISysDictTypeService extends IService<SysDictType> {

    PageResult<SysDictType> pageQuery(int page, int size, String keyword);

    void addDictType(DictTypeCreateDTO dto);

    void updateDictType(DictTypeUpdateDTO dto);

    void deleteDictTypeBatch(List<Long> ids);
}