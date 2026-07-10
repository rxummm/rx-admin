package com.rx.admin.modules.monitor.job.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.job.dto.JobCreateDTO;
import com.rx.admin.modules.monitor.job.dto.JobUpdateDTO;
import com.rx.admin.modules.monitor.job.vo.JobVO;
import com.rx.admin.modules.monitor.job.entity.SysJob;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 定时任务对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobConvert {

    SysJob toEntity(JobCreateDTO dto);

    void updateEntity(JobUpdateDTO dto, @MappingTarget SysJob entity);

    JobVO toVO(SysJob entity);

    List<JobVO> toVOList(List<SysJob> list);

    default PageResult<JobVO> toPageResult(PageResult<SysJob> pageResult) {
        return pageResult.map(this::toVO);
    }
}
