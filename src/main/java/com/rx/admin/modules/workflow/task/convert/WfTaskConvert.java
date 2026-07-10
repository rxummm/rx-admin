package com.rx.admin.modules.workflow.task.convert;

import com.rx.admin.modules.workflow.task.entity.WfTask;
import com.rx.admin.modules.workflow.task.vo.WfTaskVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WfTaskConvert {
    WfTaskVO toVO(WfTask entity);
    List<WfTaskVO> toVOList(List<WfTask> list);
}
