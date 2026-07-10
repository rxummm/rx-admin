package com.rx.admin.modules.system.file.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.system.file.entity.SysFile;
import com.rx.admin.modules.system.file.vo.FileVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 文件对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileConvert {

    FileVO toVO(SysFile entity);

    List<FileVO> toVOList(List<SysFile> list);

    default PageResult<FileVO> toPageResult(PageResult<SysFile> pageResult) {
        return pageResult.map(this::toVO);
    }
}
