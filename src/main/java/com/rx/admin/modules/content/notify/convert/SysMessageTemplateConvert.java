package com.rx.admin.modules.content.notify.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.notify.entity.SysMessageTemplate;
import com.rx.admin.modules.content.notify.vo.SysMessageTemplateVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysMessageTemplateConvert {
    SysMessageTemplateVO toVO(SysMessageTemplate entity);
    List<SysMessageTemplateVO> toVOList(List<SysMessageTemplate> list);

    default PageResult<SysMessageTemplateVO> toPageResult(PageResult<SysMessageTemplate> pageResult) {
        return pageResult.map(this::toVO);
    }
}
