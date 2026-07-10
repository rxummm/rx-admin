package com.rx.admin.modules.workflow.definition.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionCreateDTO;
import com.rx.admin.modules.workflow.definition.entity.WfProcessDefinition;
import com.rx.admin.modules.workflow.definition.vo.WfProcessDefinitionVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WfProcessDefinitionConvert {
    WfProcessDefinition toEntity(WfProcessDefinitionCreateDTO dto);
    WfProcessDefinitionVO toVO(WfProcessDefinition entity);
    List<WfProcessDefinitionVO> toVOList(List<WfProcessDefinition> list);

    default PageResult<WfProcessDefinitionVO> toPageResult(PageResult<WfProcessDefinition> pageResult) {
        return pageResult.map(this::toVO);
    }
}
