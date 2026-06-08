package com.rx.admin.task;

import com.rx.admin.entity.SysJobLog;
import com.rx.admin.service.JobLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 示例定时任务Bean
 * 这是一个纯演示用的定时任务，用于展示任务执行日志功能
 * 使用方法：在定时任务管理页面创建一个任务，
 *   Bean名称: sampleJobBean
 *   方法名称: execute
 *   Cron表达式: 0 0/5 * * * ?  (每5分钟执行一次)
 */
@Slf4j
@Component("sampleJobBean")
@RequiredArgsConstructor
public class SampleJobBean {

    private final JobLogService jobLogService;

    /**
     * 示例方法 - 清理过期日志
     */
    public void cleanupOldLogs() {
        log.info("SampleJobBean.cleanupOldLogs 被执行");
        // 这里可以添加实际的清理逻辑
        log.info("清理完成 - 当前时间: {}", LocalDateTime.now());
    }

    /**
     * 示例方法 - 系统健康检查
     */
    public void healthCheck() {
        log.info("SampleJobBean.healthCheck 被执行");
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        log.info("内存状态 - 空闲: {}MB, 总分配: {}MB", freeMemory, totalMemory);
    }

    /**
     * 调用此方法会记录执行日志到 sys_job_log
     */
    public void executeWithLog(Long jobId, String jobName) {
        LocalDateTime startTime = LocalDateTime.now();
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobId(jobId);
        jobLog.setJobName(jobName);
        jobLog.setStartTime(startTime);
        try {
            this.cleanupOldLogs();
            LocalDateTime endTime = LocalDateTime.now();
            jobLog.setEndTime(endTime);
            jobLog.setDurationMs(java.time.Duration.between(startTime, endTime).toMillis());
            jobLog.setStatus(1);
            jobLog.setResultMsg("执行成功");
        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            jobLog.setEndTime(endTime);
            jobLog.setDurationMs(java.time.Duration.between(startTime, endTime).toMillis());
            jobLog.setStatus(0);
            jobLog.setErrorMessage(e.getMessage());
        }
        jobLog.setCreateTime(LocalDateTime.now());
        jobLogService.save(jobLog);
    }
}
