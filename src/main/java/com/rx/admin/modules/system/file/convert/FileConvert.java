package com.rx.admin.modules.system.file.convert;

import com.rx.admin.modules.system.file.vo.FileVO;
import com.rx.admin.entity.SysFile;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 文件对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileConvert {

    FileVO toVO(SysFile entity);
    List<FileVO> toVOList(List<SysFile> list);
}