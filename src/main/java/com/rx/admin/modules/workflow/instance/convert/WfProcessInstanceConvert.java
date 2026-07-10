package com.rx.admin.modules.workflow.instance.convert;

import com.rx.admin.modules.workflow.instance.dto.WfProcessInstanceCreateDTO;
import com.rx.admin.modules.workflow.instance.entity.WfProcessInstance;
import com.rx.admin.modules.workflow.instance.vo.WfProcessInstanceVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WfProcessInstanceConvert {
    WfProcessInstance toEntity(WfProcessInstanceCreateDTO dto);
    WfProcessInstanceVO toVO(WfProcessInstance entity);
    List<WfProcessInstanceVO> toVOList(List<WfProcessInstance> list);
}
