package com.rx.admin.modules.system.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.system.dict.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    @Select("SELECT d.* FROM sys_dict_data d LEFT JOIN sys_dict_type t ON d.type_id = t.id WHERE t.dict_type = #{dictType} AND d.status = 1 AND d.deleted = 0 ORDER BY d.sort")
    List<SysDictData> selectByDictType(String dictType);
}
