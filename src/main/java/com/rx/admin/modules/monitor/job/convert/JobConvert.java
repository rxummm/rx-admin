package com.rx.admin.modules.monitor.job.convert;

import com.rx.admin.modules.monitor.job.dto.JobCreateDTO;
import com.rx.admin.modules.monitor.job.dto.JobUpdateDTO;
import com.rx.admin.modules.monitor.job.vo.JobVO;
import com.rx.admin.entity.SysJob;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 定时任务对象转换器 */
@Mapper(componentModel = "spring")
public interface JobConvert {
    JobConvert INSTANCE = Mappers.getMapper(JobConvert.class);

    SysJob toEntity(JobCreateDTO dto);
    void updateEntity(JobUpdateDTO dto, @MappingTarget SysJob entity);
    JobVO toVO(SysJob entity);
    List<JobVO> toVOList(List<SysJob> list);
}
