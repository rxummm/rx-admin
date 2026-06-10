package com.rx.admin.modules.system.file.convert;

import com.rx.admin.modules.system.file.vo.FileVO;
import com.rx.admin.entity.SysFile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 文件对象转换器 */
@Mapper(componentModel = "spring")
public interface FileConvert {
    FileConvert INSTANCE = Mappers.getMapper(FileConvert.class);

    FileVO toVO(SysFile entity);
    List<FileVO> toVOList(List<SysFile> list);
}
