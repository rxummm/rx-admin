package com.rx.admin.modules.monitor.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.job.entity.SysJob;
import com.rx.admin.modules.monitor.job.mapper.SysJobMapper;
import com.rx.admin.modules.monitor.job.dto.JobCreateDTO;
import com.rx.admin.modules.monitor.job.dto.JobUpdateDTO;
import org.springframework.stereotype.Service;

@Service
public class SysJobService extends ServiceImpl<SysJobMapper, SysJob> {

    public PageResult<SysJob> pageQuery(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysJob::getJobName, keyword)
                    .or().like(SysJob::getBeanName, keyword)
                    .or().like(SysJob::getRemark, keyword);
        }
        if (status != null) {
            wrapper.eq(SysJob::getStatus, status);
        }
        wrapper.orderByDesc(SysJob::getCreateTime);

        IPage<SysJob> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), iPage.getRecords());
    }

    /**
     * 新增定时任务：从 DTO 收口，避免前端直接传 Entity 篡改 createBy/createTime 等字段
     */
    public void addJob(JobCreateDTO dto) {
        SysJob job = new SysJob();
        job.setJobName(dto.getJobName());
        job.setBeanName(dto.getBeanName());
        job.setMethodName(dto.getMethodName());
        job.setCronExpression(dto.getCronExpression());
        job.setParams(dto.getParams());
        job.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        job.setRemark(dto.getRemark());
        save(job);
    }

    /**
     * 修改定时任务：只更新 DTO 携带的字段，避免 createTime 等被前端覆盖
     */
    public void updateJob(JobUpdateDTO dto) {
        SysJob job = new SysJob();
        job.setId(dto.getId());
        job.setJobName(dto.getJobName());
        job.setBeanName(dto.getBeanName());
        job.setMethodName(dto.getMethodName());
        job.setCronExpression(dto.getCronExpression());
        job.setParams(dto.getParams());
        job.setStatus(dto.getStatus());
        job.setRemark(dto.getRemark());
        updateById(job);
    }

    /**
     * 切换状态：service 层收口
     */
    public void toggleStatus(Long id) {
        SysJob job = getById(id);
        if (job != null) {
            job.setStatus(job.getStatus() != null && job.getStatus() == 1 ? 0 : 1);
            updateById(job);
        }
    }
}
