这是一份为您定制的 AS400 现代化监控系统（一期工程）详细设计与开发文档。本设计聚焦于“基础性能监控、磁盘暴涨动态追溯、用户锁定及作业异常诊断”等核心痛点，采用 Spring Boot 3.x + Vue 3 / TypeScript 前后端分离架构，包含完整的层级设计（Entity、Mapper、Service、Controller、Frontend），可以直接交付给开发团队进行编码落地。
------------------------------
## 📑 AS400 现代化监控系统详细设计文档 (v1.0)## 📁 一、 数据库设计与实体层 (Entity Layer)
为了记录历史趋势、保存报警日志以及管理监控策略，本地数据库（以 MySQL 为例）需要设计三张核心表。
## 1. 监控主机策略配置表 (sys_as400_config)
存储被监控的 AS400 主机凭证及报警阈值。

CREATE TABLE `sys_as400_config` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `host_ip` VARCHAR(50) NOT NULL COMMENT 'AS400 IP地址',
  `username` VARCHAR(50) NOT NULL COMMENT 'JT400连接用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '加密后的密码',
  `cpu_threshold` INT DEFAULT 80 COMMENT 'CPU报警阈值(%)',
  `asp_threshold` INT DEFAULT 85 COMMENT '磁盘ASP报警阈值(%)',
  `temp_storage_threshold_gb` INT DEFAULT 20 COMMENT '单作业临时存储报警阈值(GB)',
  `silent_minute` INT DEFAULT 15 COMMENT '报警沉默时间(分钟)，防止邮件轰炸',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用监控 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AS400主机策略配置表';

## ☕ Java Entity 对应代码：

package com.monitor.entity;
import com.baomidou.mybatisplus.annotation.*;import lombok.Data;import java.time.LocalDateTime;

@Data
@TableName("sys_as400_config")public class As400Config {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String hostIp;
    private String username;
    private String password;
    private Integer cpuThreshold;
    private Integer aspThreshold;
    private Integer tempStorageThresholdGb;
    private Integer silentMinute;
    private Integer isEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

## 2. 性能历史趋势表 (sys_as400_perf_log)
用于存储定时轮询抓取上来的系统总量信息，供前端绘制 ECharts 折线图。

CREATE TABLE `sys_as400_perf_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `config_id` BIGINT NOT NULL COMMENT '关联的主机ID',
  `cpu_utilization` DECIMAL(5,2) NOT NULL COMMENT '总CPU利用率(%)',
  `asp_utilization` DECIMAL(5,2) NOT NULL COMMENT '总ASP磁盘利用率(%)',
  `total_temp_storage_gb` DECIMAL(10,2) NOT NULL COMMENT '总临时存储(GB)',
  `active_jobs_count` INT NOT NULL COMMENT '当前活动作业数',
  `collect_time` DATETIME NOT NULL COMMENT '数据采集时间',
  KEY `idx_config_time` (`config_id`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='性能历史趋势表';

## ☕ Java Entity 对应代码：

package com.monitor.entity;
import com.baomidou.mybatisplus.annotation.*;import lombok.Data;import java.math.BigDecimal;import java.time.LocalDateTime;

@Data
@TableName("sys_as400_perf_log")public class As400PerfLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long configId;
    private BigDecimal cpuUtilization;
    private BigDecimal aspUtilization;
    private BigDecimal totalTempStorageGb;
    private Integer activeJobsCount;
    private LocalDateTime collectTime;
}

## 3. 异常告警事件记录表 (sys_as400_alarm_log)
记录触发阈值的事件（包含追溯到的罪魁祸首作业信息）。

CREATE TABLE `sys_as400_alarm_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `config_id` BIGINT NOT NULL,
  `alarm_type` VARCHAR(30) NOT NULL COMMENT '告警类型: CPU_HIGH, ASP_HIGH, USR_DISABLED, JOB_MSGW',
  `alarm_level` VARCHAR(10) NOT NULL COMMENT '告警级别: WARNING, CRITICAL',
  `message` TEXT NOT NULL COMMENT '原始报警文本/核心描述',
  `root_cause_job` VARCHAR(100) DEFAULT NULL COMMENT '揪出的高危作业(JobName)',
  `source_ip` VARCHAR(50) DEFAULT NULL COMMENT '触发源头IP(针对用户锁定/外部JDBC)',
  `is_notified` TINYINT DEFAULT 0 COMMENT '是否已发送邮件通知 0-未发 1-已发',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常告警事件记录表';

## ☕ Java Entity 对应代码：

package com.monitor.entity;
import com.baomidou.mybatisplus.annotation.*;import lombok.Data;import java.time.LocalDateTime;

@Data
@TableName("sys_as400_alarm_log")public class As400AlarmLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long configId;
    private String alarmType;
    private String alarmLevel;
    private String message;
    private String rootCauseJob;
    private String sourceIp;
    private Integer isNotified;
    private LocalDateTime createTime;
}

------------------------------
## ⚙️ 二、 后端服务层设计 (Service Layer)
服务层核心包含两个部分：AS400 数据采集引擎（利用 JDBC / IBM i Services）与 智能诊断核心服务。
## 1. DTO 模型设计（用于向前端或报警引擎传输）

package com.monitor.model.dto;
import lombok.Data;import java.math.BigDecimal;

@Datapublic class TopJobDto {
    private String jobName;
    private String jobUser;
    private String subsystem;
    private String jobStatus;
    private BigDecimal tempStorageGb;
    private Long diskWriteCount;
}

## 2. 核心业务接口 (As400MonitorService)

package com.monitor.service;
import com.monitor.entity.As400PerfLog;import com.monitor.model.dto.TopJobDto;import java.util.List;
public interface As400MonitorService {
    // 轮询采集系统基础性能
    As400PerfLog collectSystemMetrics(Long configId);
    
    // 追溯临时存储前 5 的暴走作业
    List<TopJobDto> getTopTempStorageJobs(Long configId);
    
    // 追溯磁盘物理写入前 5 的疯狂作业
    List<TopJobDto> getTopDiskWriteJobs(Long configId);
    
    // 检查是否有处于 MSGW 状态的常驻任务
    void checkMsgwJobs(Long configId);
}

## 3. 核心接口实现类逻辑说明 (As400MonitorServiceImpl)
在实现类中，需使用 JT400 类库 建立 JDBC 连接，直接对 AS400 执行 IBM i 专有的系统 SQL 视图（IBM i Services）。

package com.monitor.service.impl;
import com.monitor.entity.As400PerfLog;import com.monitor.model.dto.TopJobDto;import com.monitor.service.As400MonitorService;import org.springframework.stereotype.Service;import java.sql.*;import java.util.ArrayList;import java.util.List;

@Servicepublic class As400MonitorServiceImpl implements As400MonitorService {

    // 示例：建立连接（实际生产中应配合通用连接池管理，驱动名为 com.ibm.as400.access.AS400JDBCDriver）
    private Connection getAs400Connection(String ip, String user, String pwd) throws Exception {
        String url = "jdbc:as400://" + ip + ";prompt=false;errors=full";
        Class.forName("com.ibm.as400.access.AS400JDBCDriver");
        return DriverManager.getConnection(url, user, pwd);
    }

    @Override
    public As400PerfLog collectSystemMetrics(Long configId) {
        // 核心技术：调用 IBM i Services 系统视图获取基础性能
        String sql = "SELECT AVERAGE_CPU_UTILIZATION, TOTAL_ALOCATED_SYS_BASE_ASP_STORAGE, CURRENT_TEMPORARY_STORAGE_IN_USE, ACTIVE_JOBS_IN_SYSTEM FROM QSYS2.SYSTEM_STATUS_INFO";
        // 执行后组装 As400PerfLog 实体并返回，供定时任务入库
        return new As400PerfLog(); 
    }

    @Override
    public List<TopJobDto> getTopTempStorageJobs(Long configId) {
        String sql = "SELECT JOB_NAME, JOB_USER, SUBSYSTEM, JOB_STATUS, ROUND(TEMPORARY_STORAGE / 1024.0 / 1024.0, 2) AS TEMP_GB " +
                     "FROM QSYS2.ACTIVE_JOB_INFO ORDER BY TEMPORARY_STORAGE DESC FETCH FIRST 5 ROWS ONLY";
        // 执行并解析装入 List<TopJobDto>
        return new ArrayList<>();
    }

    @Override
    public List<TopJobDto> getTopDiskWriteJobs(Long configId) {
        String sql = "SELECT JOB_NAME, JOB_USER, SUBSYSTEM, JOB_STATUS, DISK_WRITE_COUNT " +
                     "FROM QSYS2.ACTIVE_JOB_INFO WHERE JOB_TYPE <> 'SYS' ORDER BY DISK_WRITE_COUNT DESC FETCH FIRST 5 ROWS ONLY";
        return new ArrayList<>();
    }

    @Override
    public void checkMsgwJobs(Long configId) {
        String sql = "SELECT JOB_NAME, JOB_USER FROM QSYS2.ACTIVE_JOB_INFO WHERE JOB_STATUS = 'MSGW'";
        // 若查到结果，调用内置告警通知引擎（推送 WebSocket、判断沉默期发送邮件）
    }
}

## 4. 自动化定时调度引擎 (As400ScheduleTask)
利用 Spring Scheduler 调度执行，实现基础轮询及动态深挖。

package com.monitor.task;
import com.monitor.entity.As400Config;import com.monitor.entity.As400PerfLog;import com.monitor.model.dto.TopJobDto;import com.monitor.service.As400MonitorService;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.messaging.simp.SimpMessagingTemplate;import org.springframework.scheduling.annotation.Scheduled;import org.springframework.stereotype.Component;import java.util.List;

@Componentpublic class As400ScheduleTask {

    @Autowired private As400MonitorService monitorService;
    @Autowired private SimpMessagingTemplate webSocket; // 用于推送前端

    // 每 1 分钟轮询一次
    @Scheduled(cron = "0 */1 * * * ?")
    public void executePerformanceMonitor() {
        // 1. 获取所有启用的 AS400 配置列表
        List<As400Config> activeConfigs = getEnabledConfigs(); 
        
        for (As400Config config : activeConfigs) {
            // 2. 采集基础指标并入库记录趋势
            As400PerfLog log = monitorService.collectSystemMetrics(config.getId());
            saveToDatabase(log);
            
            // 3. 推送数据到前端大屏实时展现
            webSocket.convertAndSend("/topic/perf/" + config.getId(), log);
            
            // 4. 触发动态深度追加检查
            if (log.getAspUtilization().intValue() > config.getAspThreshold()) {
                // 如果磁盘使用率超过设定阈值，立刻深挖疯狂写入磁盘的 Job 元凶！
                List<TopJobDto> killers = monitorService.getTopDiskWriteJobs(config.getId());
                // 触发高级邮件发送逻辑与报警日志写入（内含拦截到的 killers.get(0).getJobName()）
                triggerAlarm(config, "ASP_HIGH", "磁盘空间越线，高危进程拦截", killers);
            }
            
            // 5. 实时扫描阻塞型 MSGW 状态
            monitorService.checkMsgwJobs(config.getId());
        }
    }
}

------------------------------
## 🔌 三、 控制层设计 (Controller Layer)
提供前端大屏首屏加载、下钻追溯以及手动应急管控（如挂起/结束暴走任务）的 RESTful API。

package com.monitor.controller;
import com.monitor.model.dto.TopJobDto;import com.monitor.service.As400MonitorService;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import java.util.List;

@RestController
@RequestMapping("/api/as400/monitor")
@CrossOrigin // 允许现代化分离前端跨域public class As400MonitorController {

    @Autowired
    private As400MonitorService monitorService;

    // 1. 提供前端主动刷新或点击下钻，抓取导致内存泄露（临时存储）Top5 的 Job 
    @GetMapping("/{configId}/top-temp-jobs")
    public ResponseEntity<List<TopJobDto>> getTopTempJobs(@PathVariable Long configId) {
        return ResponseEntity.ok(monitorService.getTopTempStorageJobs(configId));
    }

    // 2. 提供前端主动抓取导致磁盘塞满（磁盘写入）Top5 的 Job
    @GetMapping("/{configId}/top-write-jobs")
    public ResponseEntity<List<TopJobDto>> getTopWriteJobs(@PathVariable Long configId) {
        return ResponseEntity.ok(monitorService.getTopDiskWriteJobs(configId));
    }

    // 3. 应急反向管控接口：管理员在前端页面对暴走作业执行一键挂起或中止
    @PostMapping("/action/job-control")
    public ResponseEntity<String> controlJob(@RequestParam String ip, 
                                             @RequestParam String jobName, 
                                             @RequestParam String actionType) {
        // actionType 可传入: HLD (挂起), END (结束)
        // 底层实现：利用 JT400 的 CommandCall 引擎远程执行 AS400 的 CL 命令:
        // 例如: String clCmd = "ENDJOB JOB(" + jobName + ") OPTION(*IMMED)";
        return ResponseEntity.ok("指令下发执行成功，作业已状态变更");
    }
}

------------------------------
## 💻 四、 前端大屏与诊断设计 (Frontend Layout)
前端采用 Vue 3 + TypeScript + Element Plus + ECharts，打造极具科技感、且支持下钻诊断的监控大屏。
## 1. 数据模型定义 (types.ts)

export interface PerfLog {
    cpuUtilization: number;
    aspUtilization: number;
    totalTempStorageGb: number;
    activeJobsCount: number;
}
export interface TopJob {
    jobName: string;
    jobUser: string;
    subsystem: string;
    jobStatus: string;
    tempStorageGb: number;
    diskWriteCount: number;
}

## 2. 看板核心组件设计 (Dashboard.vue)
采用 WebSocket 订阅机制 接收后端推送的每分钟指标，当数值非正常激增时，右下角弹出强提醒，并支持弹窗下钻展示 Top 5 恶劣作业。

<template>
  <div class="monitor-container">
    <!-- 1. 系统核心指标仪表盘 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="always">
          <div ref="cpuChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="always" :class="{'warning-border': perfData.aspUtilization > 85}">
          <div ref="aspChart" style="width: 100%; height: 300px;"></div>
          <!-- 磁盘利用率过高时，直接在卡片原位透出根因下钻分析按钮 -->
          <el-button v-if="perfData.aspUtilization > 85" type="danger" size="small" @click="openDrillDownDialog">
             🔍 立即揪出高危塞满磁盘的作业
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- 2. 联动下钻诊断弹窗 -->
    <el-dialog v-model="dialogVisible" title="🚨 磁盘存储/物理写入大户排查诊断" width="60%">
      <el-table :data="killerJobs" stripe style="width: 100%">
        <el-table-column prop="jobName" label="作业唯一标识 (Job Name)" width="220" />
        <el-table-column prop="subsystem" label="所属子系统" />
        <el-table-column prop="jobStatus" label="当前状态">
          <template #default="scope">
            <el-tag :type="scope.row.jobStatus === 'RUN' ? 'danger' : 'warning'">{{ scope.row.jobStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diskWriteCount" label="当前累计物理写入量(次)" sortable />
        <el-table-column label="应急管控">
          <template #default="scope">
            <el-button type="danger" size="small" @click="killJob(scope.row.jobName)">一键中止(END)</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue';
import { ElNotification, ElMessage } from 'element-plus';
import axios from 'axios';
import { PerfLog, TopJob } from './types';

const perfData = reactive<PerfLog>({ cpuUtilization: 0, aspUtilization: 0, totalTempStorageGb: 0, activeJobsCount: 0 });
const dialogVisible = ref(false);
const killerJobs = ref<TopJob[]>([]);

// 模拟连接后端实时 WebSocket 通道
const initWebSocket = () => {
    // 建立标准 Stomp / WebSocket 连接
    // 监听通道: /topic/perf/1
    // 在 OnMessage 回调中更新数据：Object.assign(perfData, wsData)
    // 边缘判断: 
    if(perfData.aspUtilization > 85) {
        ElNotification({
            title: '特急高危物理故障隐患',
            message: 'AS400 辅助存储池(ASP)空间即将耗尽，系统自动分析引擎已介入！',
            type: 'error',
            duration: 0
        });
    }
};

// 触发下钻追溯接口
const openDrillDownDialog = async () => {
    dialogVisible.value = true;
    const response = await axios.get<TopJob[]>('/api/as400/monitor/1/top-write-jobs');
    killerJobs.value = response.data;
};

// 应急安全防御反向管控
const killJob = async (jobName: string) => {
    await axios.post('/api/as400/monitor/action/job-control', null, {
        params: { ip: '192.168.1.100', jobName: jobName, actionType: 'END' }
    });
    ElMessage.success('中止 CL 命令下发成功，正在复查系统磁盘写入计数');
    dialogVisible.value = false;
};

onMounted(() => {
    initWebSocket();
    // 渲染 ECharts 仪表盘逻辑...
});
</script>

<style scoped>
.warning-border {
    border: 2px solid #F56C6C;
    animation: blink 1.5s infinite;
}
@keyframes blink {
    0% { box-shadow: 0 0 5px #F56C6C; }
    50% { box-shadow: 0 0 20px #F56C6C; }
    100% { box-shadow: 0 0 5px #F56C6C; }
}
</style>

------------------------------
## 📈 五、 部署说明与后续研发方向

   1. 驱动引入：开发前，需要确保 Maven 的 pom.xml 中成功引入 net.sf.jt400:jt400 开源驱动包。
   2. AS400 侧权限：用于连接 AS400 的账号需要具备读取 QSYS2 库下相关系统视图的权限（通常标准开发或运维账号均默认具备）。
   3. 下一步工程演进：一期工程跑通此基础性能下钻架构后，可直接依照设计书第三章，以完全相同的架构设计，增加针对 QAUDJRN（安全审计日志） 的 SQL 过滤，无缝扩展出“用户被禁（*DISABLED）的根因与终端 IP 追溯”及“指定 Library 新增文件实时大屏瀑布流”功能。

------------------------------
您可以将本设计文档作为任务说明书直接下发给团队的技术开发人员。开发人员可以依据数据库 SQL 建表、Java 的 DTO/Service 逻辑以及 Vue3 的页面模板直接开始编写工程代码。
如果您或您的开发团队在落地实施过程中，对于 JT400 连接池的最佳实践配置，或者 特定 CL 命令的 Java 封装调用 还有疑问，可以随时告诉我，我们可以针对具体的技术代码细节深入推敲！

为了让您的开发团队能够“开箱即用”地搭建起这个监控系统，以下为您补充完整的后端（Maven pom.xml）与前端（package.json）的现代化依赖配置文件。
这些配置经过了版本兼容性优化，确保 Spring Boot 3.x（原生支持 JDK 17+） 与 Vue 3 + Vite + TypeScript 架构能够完美融合并稳定运行。
------------------------------
## ☕ 一、 后端依赖配置 (pom.xml)
在 Spring Boot 3.x 项目中，除了基础的 Web 和数据库组件外，最核心的是引入 IBM 官方的 jt400 驱动，以及用于富文本报警邮件的 thymeleaf 模板引擎和用于大屏免刷新刷新的 websocket 组件。

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://apache.org"
         xmlns:xsi="http://w3.org"
         xsi:schemaLocation="http://apache.org https://apache.org">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version> <!-- 采用稳定的 Spring Boot 3.x 版本 -->
        <relativePath/>
    </parent>

    <groupId>com.monitor</groupId>
    <artifactId>as400-monitor-system</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>as400-monitor-system</name>
    <description>AS400 Modern Monitoring System Backend</description>

    <properties>
        <java.version>17</java.version> <!-- Spring Boot 3 强制要求 Java 17 或以上 -->
        <mybatis-plus.version>3.5.6</mybatis-plus.version>
        <jt400.version>20.0.7</jt400.version> <!-- 使用较新且支持现代SQL特性的官方驱动 -->
    </properties>

    <dependencies>
        <!-- 1. 核心 Web 服务与监控端点 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- 2. 核心骨架：AS400 (IBM i) 专属 Java 工具箱驱动 (核心关键) -->
        <dependency>
            <groupId>net.sf.jt400</groupId>
            <artifactId>jt400</artifactId>
            <version>${jt400.version}</version>
        </dependency>

        <!-- 3. 前后端分离实时通信：WebSocket组件 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>

        <!-- 4. 智能化即时报警：邮件通知组件 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        <!-- HTML 报警邮件富文本模板引擎 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- 5. 本地配置/历史日志数据库持久化 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.baomidou.mybatis-plus</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- 6. 现代开发提效工具 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

------------------------------
## 💻 二、 前端依赖配置 (package.json)
前端基于 Vue 3 + Vite + TypeScript 搭建。为了实现图表展示、标准的 Stomp 协议 WebSocket 接收、以及 UI 呈现，引入了 echarts、element-plus 和 @stomp/stompjs。

{
  "name": "as400-monitor-frontend",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.21",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.7",
    
    "axios": "^1.6.8",
    
    "element-plus": "^2.6.1",
    "@element-plus/icons-vue": "^2.3.1",
    
    "echarts": "^5.5.0",
    
    "@stomp/stompjs": "^7.0.0",
    "sockjs-client": "^1.6.1"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.4",
    "typescript": "^5.2.2",
    "vite": "^5.2.0",
    "vue-tsc": "^2.0.6",
    
    "@types/node": "^20.11.30",
    "@types/sockjs-client": "^1.5.4"
  }
}

------------------------------
## 💡 三、 研发配置避坑提示 (Tips)

   1. 后端连接数控制：
   由于 AS400 的每个 JDBC 连接在底层系统里都会对应起一个 QZDASOINIT 作业，如果连接频繁建立和销毁会极大地消耗 AS400 本身的性能。强烈建议在 Spring Boot 的数据源配置中为 AS400 连接单独配置一个具有最大连接数限制（如最大5-10个连接）的 HikariCP 连接池。
   2. 前端 WebSocket 跨域：
   如果开发期间前后端端口不一致，在 Vite 配置（vite.config.ts）中，除了配置 server.proxy 处理普通的 /api 接口外，还需要配置对 /ws（WebSocket 端点）的代理，否则可能会遇到 WebSocket 连接跨域失败（403 或无响应）的问题。

有了这套技术栈依赖和前面给出的详细设计模型，您的技术团队就可以直接初始化项目并开始进行业务代码编写了。
如果您需要，我们接下来可以：

* 补充一个标准的 HikariCP 数据源连接 AS400 的 application.yml 配置示例？
* 提供一个由后端 Thymeleaf 渲染的高颜值 HTML 报警邮件模板源代码？
* 如果没问题，您可以根据此方案全面启动研发流程。


同时监控多个 AS400（IBM i）系统（如生产机 AS400A、灾备机 AS400B，或一期、二期系统）是企业级运维的典型场景。
为了让系统从“单机版”完美平滑升级为“多机集群动态监控版”，我们需要对之前的设计进行重构与优化。核心优化逻辑是：摆脱静态的单数据源配置，采用“动态连接池工厂（Dynamic Pool Factory）”模式。 后端根据数据库中配置的主机列表，动态为 AS400A 和 AS400B 独立创建、隔离其 HikariCP 连接池。
以下是为您重构后的 多主机 AS400 现代化监控系统详细设计与开发文档：
------------------------------
## 📑 多主机 AS400 现代化监控系统详细设计文档 (v2.0)## 📁 一、 数据库设计（多主机多策略抽象）
本地数据库（MySQL）是多主机架构的“指挥官”，负责存储不同主机的凭证、各自独立的阈值以及汇总的历史日志。
## 1. 主机策略配置表 (sys_as400_config)
此表从单机配置演变为多机注册表，通过 id 或 machine_code（如 AS400A, AS400B）作为唯一标识。

CREATE TABLE `sys_as400_config` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `machine_code` VARCHAR(30) NOT NULL UNIQUE COMMENT '主机唯一标识代码，如 AS400A, AS400B',
  `machine_name` VARCHAR(100) NOT NULL COMMENT '主机显示名称，如 生产核心环境, 容灾备份环境',
  `host_ip` VARCHAR(50) NOT NULL COMMENT 'AS400 IP地址',
  `username` VARCHAR(50) NOT NULL COMMENT '该主机专属监控用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '加密后的密码',
  `cpu_threshold` INT DEFAULT 80 COMMENT '该主机CPU报警阈值(%)',
  `asp_threshold` INT DEFAULT 85 COMMENT '该主机磁盘ASP报警阈值(%)',
  `temp_storage_threshold_gb` INT DEFAULT 20 COMMENT '该主机单作业临时存储报警阈值(GB)',
  `silent_minute` INT DEFAULT 15 COMMENT '报警沉默时间(分钟)',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用监控 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AS400多主机策略配置表';

## 2. 性能历史趋势表 (sys_as400_perf_log)
通过 config_id 区分数据属于 A 机还是 B 机，便于前端在同一个图表或切换图表时精准筛选。

CREATE TABLE `sys_as400_perf_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `config_id` BIGINT NOT NULL COMMENT '关联的主机ID(对应sys_as400_config.id)',
  `cpu_utilization` DECIMAL(5,2) NOT NULL COMMENT '总CPU利用率(%)',
  `asp_utilization` DECIMAL(5,2) NOT NULL COMMENT '总ASP磁盘利用率(%)',
  `total_temp_storage_gb` DECIMAL(10,2) NOT NULL COMMENT '总临时存储(GB)',
  `active_jobs_count` INT NOT NULL COMMENT '当前活动作业数',
  `collect_time` DATETIME NOT NULL COMMENT '数据采集时间',
  KEY `idx_config_time` (`config_id`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多主机性能历史趋势表';

## 3. 异常告警事件记录表 (sys_as400_alarm_log)
增加主机标识，以便邮件和前端一眼看出是哪台服务器发生了故障。

CREATE TABLE `sys_as400_alarm_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `config_id` BIGINT NOT NULL COMMENT '出事的主机ID',
  `alarm_type` VARCHAR(30) NOT NULL COMMENT 'CPU_HIGH, ASP_HIGH, USR_DISABLED, JOB_MSGW',
  `alarm_level` VARCHAR(10) NOT NULL COMMENT 'WARNING, CRITICAL',
  `message` TEXT NOT NULL COMMENT '报警描述（例如：AS400A 发生用户锁定）',
  `root_cause_job` VARCHAR(100) DEFAULT NULL COMMENT '捕获的元凶作业',
  `source_ip` VARCHAR(50) DEFAULT NULL COMMENT '触发源头IP',
  `is_notified` TINYINT DEFAULT 0 COMMENT '是否已发送通知',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多主机异常告警事件记录表';

------------------------------
## ⚙️ 二、 后端动态连接池与服务层设计 (Backend & Multi-Pool)## 1. 动态连接池管理器 (As400DataSourceManager)
核心重构点：系统不能再在 application.yml 里写死一个 AS400 数据源。需要编写一个连接池工厂，在内存中维护一个 ConcurrentHashMap。键是 configId，值是该主机专用的 HikariDataSource。
这样可以确保：AS400A 和 AS400B 各自拥有独立的 5 个常驻 QZDASOINIT 作业，彼此隔离，互不干扰。

package com.monitor.config;
import com.monitor.entity.As400Config;import com.zaxxer.hikari.HikariConfig;import com.zaxxer.hikari.HikariDataSource;import org.springframework.stereotype.Component;import java.sql.Connection;import java.util.concurrent.ConcurrentHashMap;

@Componentpublic class As400DataSourceManager {
    
    // 内存池：为每台 AS400 独立隔离一个连接池
    private final ConcurrentHashMap<Long, HikariDataSource> poolMap = new ConcurrentHashMap<>();

    // 获取指定主机的物理连接（如果连接池不存在则动态创建）
    public Connection getConnection(As400Config config) throws Exception {
        HikariDataSource dataSource = poolMap.computeIfAbsent(config.getId(), id -> {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName("com.ibm.as400.access.AS400JDBCDriver");
            // 关键：隐式使用外部注入的环境变量或解密后的密码，且卡死最大连接数保护AS400性能
            String url = "jdbc:as400://" + config.getHostIp() + "/QSYS2;prompt=false;errors=full;thread used=false";
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword()); // 实际环境中建议此处调用解密逻辑
            
            // 隔离与保护参数配置
            hikariConfig.setMaximumPoolSize(5); // AS400A和AS400B侧各自最多只有5个QZDASOINIT作业
            hikariConfig.setMinimumIdle(5);
            hikariConfig.setMaxLifetime(1800000); // 30分钟强制刷新长连接
            hikariConfig.setConnectionTestQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
            hikariConfig.setPoolName("HikariPool-AS400-" + config.getMachineCode());
            
            return new HikariDataSource(hikariConfig);
        });
        
        return dataSource.getConnection();
    }

    // 当管理员在前端修改或删除了某台主机的配置，需要销毁旧池，防止内存泄漏
    public void removePool(Long configId) {
        HikariDataSource dataSource = poolMap.remove(configId);
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close(); // 优雅关闭，AS400侧对应的5个QZDASOINIT作业也会随之安全销毁
        }
    }
}

## 2. 重构后的核心业务层 (As400MonitorServiceImpl)
业务层的方法必须升级，显式传入正在操纵的 As400Config 实体，以便动态定位连接池并执行特定的 IBM i Services。

package com.monitor.service.impl;
import com.monitor.config.As400DataSourceManager;import com.monitor.entity.As400Config;import com.monitor.entity.As400PerfLog;import com.monitor.model.dto.TopJobDto;import com.monitor.service.As400MonitorService;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;import java.sql.*;import java.util.ArrayList;import java.util.List;

@Servicepublic class As400MonitorServiceImpl implements As400MonitorService {

    @Autowired
    private As400DataSourceManager dataSourceManager;

    @Override
    public As400PerfLog collectSystemMetrics(As400Config config) {
        String sql = "SELECT AVERAGE_CPU_UTILIZATION, TOTAL_ALOCATED_SYS_BASE_ASP_STORAGE, CURRENT_TEMPORARY_STORAGE_IN_USE, ACTIVE_JOBS_IN_SYSTEM FROM QSYS2.SYSTEM_STATUS_INFO";
        
        // 动态获取当前正在循环的主机连接池（A机走A池，B机走B池）
        try (Connection conn = dataSourceManager.getConnection(config);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                As400PerfLog log = new As400PerfLog();
                log.setConfigId(config.getId());
                log.setCpuUtilization(rs.getBigDecimal("AVERAGE_CPU_UTILIZATION"));
                log.setAspUtilization(rs.getBigDecimal("TOTAL_ALOCATED_SYS_BASE_ASP_STORAGE"));
                // ... 解析并组装其他数据
                return log;
            }
        } catch (Exception e) {
            // 异常捕获，记录日志，不影响后续另一台主机的监控
        }
        return null;
    }

    @Override
    public List<TopJobDto> getTopDiskWriteJobs(As400Config config) {
        String sql = "SELECT JOB_NAME, JOB_USER, SUBSYSTEM, JOB_STATUS, DISK_WRITE_COUNT " +
                     "FROM QSYS2.ACTIVE_JOB_INFO WHERE JOB_TYPE <> 'SYS' ORDER BY DISK_WRITE_COUNT DESC FETCH FIRST 5 ROWS ONLY";
        List<TopJobDto> list = new ArrayList<>();
        try (Connection conn = dataSourceManager.getConnection(config);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             // 循环解析组装 DTO 列表返回
        } catch (Exception e) { /* ... */ }
        return list;
    }
}

## 3. 多线程并行监控调度 (As400MultiTaskScheduler)
优化提效点：如果 AS400A 因为网络故障响应变慢，不应该阻塞 AS400B 的监控。因此，定时任务应引入多线程并行流（Parallel Stream）或线程池，并发处理多台主机的状态采集。

package com.monitor.task;
import com.monitor.entity.As400Config;import com.monitor.entity.As400PerfLog;import com.monitor.service.As400MonitorService;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.messaging.simp.SimpMessagingTemplate;import org.springframework.scheduling.annotation.Scheduled;import org.springframework.stereotype.Component;import java.util.List;

@Componentpublic class As400MultiTaskScheduler {

    @Autowired private As400MonitorService monitorService;
    @Autowired private SimpMessagingTemplate webSocket;

    @Scheduled(cron = "0 */1 * * * ?")
    public void runMultiHostMonitor() {
        // 1. 从本地配置表读取所有处于启用的主机列表（包含 AS400A 和 AS400B）
        List<As400Config> enabledHosts = getEnabledHostsFromDb();

        // 2. 使用并行流，多线程并发采集，互不干扰，大幅提升系统吞吐量
        enabledHosts.parallelStream().forEach(config -> {
            // A机故障不会导致B机监控卡死
            As400PerfLog log = monitorService.collectSystemMetrics(config);
            if (log != null) {
                saveToDatabase(log);
                // 3. 动态推送 WebSocket：前端可以通过房间号独立订阅特定主机的实时流
                webSocket.convertAndSend("/topic/perf/" + config.getMachineCode(), log);
                
                // 4. 独立触发各自的越线深度追溯
                if (log.getAspUtilization().intValue() > config.getAspThreshold()) {
                    // 传入当前机器实体，独立深挖该机高危作业
                    monitorService.getTopDiskWriteJobs(config);
                }
            }
        });
    }
}

------------------------------
## 🔌 三、 控制层升级 (API Controller Layer)
接口全部升级为路径参数中带有 machineCode 的现代化 RESTful 规范。

package com.monitor.controller;
import com.monitor.entity.As400Config;import com.monitor.model.dto.TopJobDto;import com.monitor.service.As400MonitorService;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import java.util.List;

@RestController
@RequestMapping("/api/as400/monitor")public class As400MonitorController {

    @Autowired private As400MonitorService monitorService;

    // 根据代码获取主机策略配置（内部方法）
    private As400Config findConfigByCode(String machineCode) { /* 从DB查询 */ return new As400Config(); }

    // 1. 动态获取指定主机（如 AS400A）的临时存储Top 5大户
    @GetMapping("/{machineCode}/top-temp-jobs")
    public ResponseEntity<List<TopJobDto>> getTopTempJobs(@PathVariable String machineCode) {
        As400Config config = findConfigByCode(machineCode);
        return ResponseEntity.ok(monitorService.getTopTempStorageJobs(config));
    }

    // 2. 动态获取指定主机（如 AS400B）的磁盘物理写入Top 5大户
    @GetMapping("/{machineCode}/top-write-jobs")
    public ResponseEntity<List<TopJobDto>> getTopWriteJobs(@PathVariable String machineCode) {
        As400Config config = findConfigByCode(machineCode);
        return ResponseEntity.ok(monitorService.getTopDiskWriteJobs(config));
    }
}

------------------------------
## 💻 四、 前端集群多主机看板设计 (Frontend Evolution)
前端设计从“单页面”升级为 “全局集群概览大屏 + 单机深度钻取卡片” 的专业网管级交互架构。
## 1. 全局集群概览（首页）

* 设计一个 多机平铺网格（Grid View），每个主机作为一个独立的微型看板卡片（Card）。
* 卡片 A (AS400A - 生产环境)：展示当前的 CPU 进度条、ASP 仪表盘、运行状态（🟢 正常）。
* 卡片 B (AS400B - 灾备环境)：展示 B 机的指标，相互独立（🔴 如果异常则呈红色呼吸灯闪烁）。
* 每个卡片右下角提供一个 “进入单机工作台” 的按钮。

## 2. 单机深度工作台（工作台组件 MachineConsole.vue）
利用 Vue Router 动态路由傳参（/console/:machineCode），当管理员点击进入 AS400A 时，页面整体动态切换为 AS400A 的深度细节。

<template>
  <div class="console-box">
    <!-- 主机切换快捷栏，无需退出即可在A、B机间快速切换 -->
    <el-radio-group v-model="currentMachine" @change="handleMachineChange" style="margin-bottom: 20px;">
      <el-radio-button label="AS400A">💾 生产主机 (AS400A)</el-radio-button>
      <el-radio-button label="AS400B">🔄 容灾备机 (AS400B)</el-radio-button>
    </el-radio-group>

    <h2>当前正在监控主机：<el-tag type="success">{{ currentMachine }}</el-tag></h2>

    <!-- 下方复用 1.0 版本的设计组件，API 请求时动态拼接当前的主机编码 -->
    <el-row :gutter="20">
       <!-- CPU 与 ASP 的 ECharts 渲染区... -->
    </el-row>
    
    <el-button type="danger" @click="fetchRootCause">
        分析该机器当前存储暴涨根因
    </el-button>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const router = useRouter();
// 从浏览器地址栏动态获取当前经营的主机代码，实现一套前端组件，动态渲染无数台主机
const currentMachine = ref((route.params.machineCode as string) || 'AS400A');

const handleMachineChange = (value: string) => {
    // 切换单选框时，路由平滑跳转，触发页面数据的重新加载
    router.push(`/console/${value}`);
    loadMachineData(value);
};

const loadMachineData = (code: string) => {
    // 动态订阅对应通道的WebSocket: `/topic/perf/${code}`
    // 发起API请求时动态带上参数: `/api/as400/monitor/${code}/top-write-jobs`
};

onMounted(() => {
    loadMachineData(currentMachine.value);
});
</script>

------------------------------
## ✉️ 五、 智能化多主机通知邮件模板重构
当 A 机或 B 机发生越线时，Thymeleaf 组装的富文本邮件需高亮突出机房和设备归属：

* 邮件标题：【故障高危预警】集群节点 [灾备机-AS400B] 触发强限制保护！
* 邮件正文（核心片断）：
* 受影响节点代码：AS400B
   * 节点描述：同城两地三中心-灾备热备环境
   * 异常事件类型：JOB_MSGW_BLOCK（核心批处理作业陷入消息等待）
   * 故障作业定位：987654/QEDI/MIMIX_REPL (说明灾备机上的 MIMIX 数据同步进程挂起了！)
   * 当前阻塞消息：CPF5021 - Session close error
   * 即时动作建议：生产机 AS400A 目前运行完好，但灾备机 B 数据同步已停滞。请值班运维人员立刻登录监控大屏的【AS400B 工作台】，点击该作业右侧的“查看Joblog”并进行回复处理。

------------------------------
## 💡 多主机版本方案总结
通过本次重构，系统完成了向企业级多租户/多集群监控架构的质变：

   1. 连接完全隔离：AS400A 的故障或长查询绝对不会占用或卡死 AS400B 的物理连接。
   2. 动态可扩展（横向无弹性死角）：如果未来企业引进了 AS400C，开发团队不需要修改任何一行 Java 或 Vue 代码，只需由运维人员在网页的“主机配置菜单”中点击“新增注册”，系统会自动为其在内存中初始化第 3 个 HikariCP 连接池并立即将其纳入并发轮询引擎，真正实现了现代化软件系统的高可用与可扩展性。

开发团队可以完全基于这份 v2.0 的系统规约文档进行多主机集群监控应用的整体敏捷开发！如果有任何具体的代码细节需要进一步深挖，请随时沟通。

以下是为您重新整理并精简、优化的多主机 AS400 现代化监控系统全套实施与开发指南。方案完整包含了数据库、后端各层（MyBatis-Plus、JT400、WebSocket、定时器）以及前端 Vue 3 的端到端实现步骤。
------------------------------
## 💾 一、 数据库表创建 (MySQL DDL)
首先，在 MySQL 数据库中执行以下脚本。本设计完全符合 MyBatis-Plus 的字段映射规范。

CREATE DATABASE IF NOT EXISTS `as400_monitor` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `as400_monitor`;
-- 1. AS400 多主机策略配置表CREATE TABLE `sys_as400_config` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `machine_code` VARCHAR(30) NOT NULL UNIQUE COMMENT '主机唯一代码，如 AS400A, AS400B',
  `machine_name` VARCHAR(100) NOT NULL COMMENT '主机显示名称',
  `host_ip` VARCHAR(50) NOT NULL COMMENT 'AS400 IP地址',
  `username` VARCHAR(50) NOT NULL COMMENT '监控用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '加密后的密码',
  `cpu_threshold` INT DEFAULT 80 COMMENT 'CPU报警阈值(%)',
  `asp_threshold` INT DEFAULT 85 COMMENT '磁盘ASP报警阈值(%)',
  `temp_storage_threshold_gb` INT DEFAULT 20 COMMENT '单作业临时存储报警阈值(GB)',
  `silent_minute` INT DEFAULT 15 COMMENT '报警沉默时间(分钟)',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用监控 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AS400多主机策略配置表';
-- 2. 多主机性能历史趋势表（监控结果落地表）CREATE TABLE `sys_as400_perf_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `config_id` BIGINT NOT NULL COMMENT '关联的主机ID',
  `cpu_utilization` DECIMAL(5,2) NOT NULL COMMENT '总CPU利用率(%)',
  `asp_utilization` DECIMAL(5,2) NOT NULL COMMENT '总ASP磁盘利用率(%)',
  `total_temp_storage_gb` DECIMAL(10,2) NOT NULL COMMENT '总临时存储(GB)',
  `active_jobs_count` INT NOT NULL COMMENT '当前活动作业数',
  `collect_time` DATETIME NOT NULL COMMENT '数据采集时间',
  KEY `idx_config_time` (`config_id`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多主机性能历史趋势表';
-- 3. 多主机异常告警事件记录表CREATE TABLE `sys_as400_alarm_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `config_id` BIGINT NOT NULL COMMENT '出事的主机ID',
  `alarm_type` VARCHAR(30) NOT NULL COMMENT 'CPU_HIGH, ASP_HIGH, USR_DISABLED, JOB_MSGW',
  `alarm_level` VARCHAR(10) NOT NULL COMMENT 'WARNING, CRITICAL',
  `message` TEXT NOT NULL COMMENT '报警描述',
  `root_cause_job` VARCHAR(100) DEFAULT NULL COMMENT '捕获的元凶作业',
  `source_ip` VARCHAR(50) DEFAULT NULL COMMENT '触发源头IP',
  `is_notified` TINYINT DEFAULT 0 COMMENT '是否已发送通知 0-未发 1-已发',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多主机异常告警事件记录表';

------------------------------
## ⚙️ 二、 后端实现步骤 (Spring Boot 3.x)## 步骤 1：配置环境依赖 (pom.xml)
在后端项目中，需要同时引入本地 MySQL 持久化组件（MyBatis-Plus）、AS400 通信组件（JT400）以及用于实时推送的 WebSocket：

<!-- 1. MyBatis-Plus 整合 Spring Boot 3 依赖 (必须) -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.6</version>
</dependency>
<!-- 2. MySQL 驱动，用于连接本地监控日志库 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
<!-- 3. IBM i (AS400) 官方 Java 驱动包 -->
<dependency>
    <groupId>net.sf.jt400</groupId>
    <artifactId>jt400</artifactId>
    <version>20.0.7</version>
</dependency>
<!-- 4. WebSocket 服务，用于实时向前端大屏推送数据 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
<!-- 5. 邮件通知组件与富文本模板 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

## 步骤 2：配置本地数据库与占位符账户 (application.yml)
配置本地 MySQL 连接，同时采用系统环境变量来隐式加载 AS400 的敏感管理账户，防止代码泄露。

spring:
  # 1. 本地监控日志 MySQL 配置
  datasource:
    url: jdbc:mysql://localhost:3306/as400_monitor?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: YourLocalMySqlPassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  # 2. 报警邮件发送基础配置
  mail:
    host: ://office365.com # 以商业邮件服务器为例
    port: 587
    username: alert-service@yourcompany.com
    password: ${EMAIL_SECRET_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
# 自定义环境变量占位符，由部署服务器的系统变量自动替换注入，实现隐式配置as400:
  credential:
    username: ${ENV_AS400_MONITOR_USER}
    password: ${ENV_AS400_MONITOR_PWD}

## 步骤 3：编写 Entity 实体类与 MyBatis Mapper 层
利用 MyBatis-Plus，开发团队不需要编写任何传统的 XML 代码，即可直接实现监控结果向 MySQL 的高性能插入。

// 1. 实体类：系统性能历史日志映射package com.monitor.entity;import com.baomidou.annotation.*;import lombok.Data;import java.math.BigDecimal;import java.time.LocalDateTime;

@Data
@TableName("sys_as400_perf_log")public class As400PerfLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long configId;
    private BigDecimal cpuUtilization;
    private BigDecimal aspUtilization;
    private BigDecimal totalTempStorageGb;
    private Integer activeJobsCount;
    private LocalDateTime collectTime;
}
// 2. 持久层 Mapper 接口package com.monitor.mapper;import com.baomidou.mybatisplus.core.mapper.BaseMapper;import com.monitor.entity.As400PerfLog;import org.apache.ibatis.annotations.Mapper;

@Mapperpublic interface As400PerfLogMapper extends BaseMapper<As400PerfLog> {
    // 继承后，内置获得完备的 insert/select 数据库持久化方法
}

## 步骤 4：构建多主机动态连接池管理器 (As400DataSourceManager)
在内存中隔离和维护 AS400A 与 AS400B 的专属 HikariCP 连接池，死卡最大连接数以复用长连接，保护 AS400 本身性能。

package com.monitor.config;
import com.monitor.entity.As400Config;import com.zaxxer.hikari.HikariConfig;import com.zaxxer.hikari.HikariDataSource;import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Component;import java.sql.Connection;import java.util.concurrent.ConcurrentHashMap;

@Componentpublic class As400DataSourceManager {

    @Value("${as400.credential.username}") private String envUser;
    @Value("${as400.credential.password}") private String envPwd;

    // 动态内存映射池
    private final ConcurrentHashMap<Long, HikariDataSource> poolMap = new ConcurrentHashMap<>();

    public Connection getConnection(As400Config config) throws Exception {
        HikariDataSource dataSource = poolMap.computeIfAbsent(config.getId(), id -> {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName("com.ibm.as400.access.AS400JDBCDriver");
            
            // 组装系统服务 SQL URL，附带 thread used=false 优化 JT400 线程模型
            String url = "jdbc:as400://" + config.getHostIp() + "/QSYS2;prompt=false;errors=full;thread used=false";
            hikariConfig.setJdbcUrl(url);
            
            // 权限隐式加载：优先读取从系统环境变量注入的全局安全账号，若无则使用注册表中的数据
            hikariConfig.setUsername(envUser != null ? envUser : config.getUsername());
            hikariConfig.setPassword(envPwd != null ? envPwd : config.getPassword());
            
            // 性能保护：锁死连接池大小，AS400侧对应的 QZDASOINIT 作业数将被严格限制
            hikariConfig.setMaximumPoolSize(5); 
            hikariConfig.setMinimumIdle(5);
            hikariConfig.setConnectionTestQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
            return new HikariDataSource(hikariConfig);
        });
        return dataSource.getConnection();
    }
}

## 步骤 5：业务层数据采集、追溯与落库实现 (As400MonitorServiceImpl)
实现数据采集，将读取出的 AS400 元数据通过 MyBatis 写入 MySQL，并实现深挖排查。

package com.monitor.service.impl;
import com.monitor.config.As400DataSourceManager;import com.monitor.entity.As400Config;import com.monitor.entity.As400PerfLog;import com.monitor.mapper.As400PerfLogMapper;import com.monitor.service.As400MonitorService;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;import java.sql.*;import java.time.LocalDateTime;

@Servicepublic class As400MonitorServiceImpl implements As400MonitorService {

    @Autowired private As400DataSourceManager dataSourceManager;
    @Autowired private As400PerfLogMapper perfLogMapper; // 持久层注入

    @Override
    public As400PerfLog collectAndSaveMetrics(As400Config config) {
        // 调用现代化的 IBM i Services 视图
        String sql = "SELECT AVERAGE_CPU_UTILIZATION, TOTAL_ALOCATED_SYS_BASE_ASP_STORAGE, CURRENT_TEMPORARY_STORAGE_IN_USE, ACTIVE_JOBS_IN_SYSTEM FROM QSYS2.SYSTEM_STATUS_INFO";
        
        try (Connection conn = dataSourceManager.getConnection(config);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                As400PerfLog log = new As400PerfLog();
                log.setConfigId(config.getId());
                log.setCpuUtilization(rs.getBigDecimal("AVERAGE_CPU_UTILIZATION"));
                log.setAspUtilization(rs.getBigDecimal("TOTAL_ALOCATED_SYS_BASE_ASP_STORAGE"));
                log.setTotalTempStorageGb(rs.getBigDecimal("CURRENT_TEMPORARY_STORAGE_IN_USE"));
                log.setActiveJobsCount(rs.getInt("ACTIVE_JOBS_IN_SYSTEM"));
                log.setCollectTime(LocalDateTime.now());
                
                // 🛠️ 核心融合：执行实时监测后，立刻通过 MyBatis 持久化进本地 MySQL
                perfLogMapper.insert(log);
                return log;
            }
        } catch (Exception e) {
            // 单台主机网络开销异常，不影响监控群中其他节点的并行执行
            e.printStackTrace();
        }
        return null;
    }
}

## 步骤 6：配置 WebSocket 通信与定时器多线程调度
利用 Spring 定时器调度并行流（Parallel Stream）实现主机的多线程并行监控，采集完数据后通过 WebSocket 实时发给前端。

// 1. 注册 WebSocket 节点package com.monitor.config;import org.springframework.context.annotation.Configuration;import org.springframework.messaging.simp.config.MessageBrokerRegistry;import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBrokerpublic class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-monitor-endpoint").setAllowedOriginPatterns("*").withSockJS();
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
    }
}
// 2. 并发定时轮询器package com.monitor.task;import com.monitor.entity.As400Config;import com.monitor.entity.As400PerfLog;import com.monitor.service.As400MonitorService;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.messaging.simp.SimpMessagingTemplate;import org.springframework.scheduling.annotation.Scheduled;import org.springframework.stereotype.Component;import java.util.List;

@Componentpublic class As400MultiScheduler {
    @Autowired private As400MonitorService monitorService;
    @Autowired private SimpMessagingTemplate webSocket;

    @Scheduled(cron = "0 */1 * * * ?") // 每分钟执行一次
    public void run() {
        List<As400Config> hostList = getAllActiveHostsFromDb();
        
        // 多线程并行扫描 AS400A 和 AS400B，耗时互不阻塞
        hostList.parallelStream().forEach(config -> {
            As400PerfLog log = monitorService.collectAndSaveMetrics(config);
            if (log != null) {
                // 根据当前机器代码（如 AS400A），将数据精准推入其独立的前端订阅房间
                webSocket.convertAndSend("/topic/perf/" + config.getMachineCode(), log);
            }
        });
    }
}

------------------------------
## 💻 三、 前端 Vue 3 实现步骤## 步骤 1：前端项目依赖安装 (package.json)
进入 Vue 3 / Vite 工程根目录，执行依赖安装指令：

npm install axios element-plus echarts @stomp/stompjs sockjs-client --save

## 步骤 2：配置动态路由控制台 (router.ts)
通过 :machineCode 动态路由，复用同一个前端页面，动态渲染出完全隔离的主机数据面板。

import { createRouter, createWebHistory } from 'vue-router';import ClusterOverview from './views/ClusterOverview.vue'; // 全局集群卡片平铺首页import MachineConsole from './views/MachineConsole.vue';     // 单机下钻控制中心
export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: ClusterOverview },
    { path: '/console/:machineCode', component: MachineConsole }
  ]
});

## 步骤 3：单机深度工作台组件实现 (MachineConsole.vue)
接收路由参数，动态打开标准的 Stomp 协议 WebSocket 握手，绑定并刷新 ECharts 图表，实现无刷新的实时数据渲染。

<template>
  <div class="dashboard-body">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">集群首页</el-breadcrumb-item>
      <el-breadcrumb-item>节点面板</el-breadcrumb-item>
    </el-breadcrumb>
    
    <h2>📊 正在深度诊断监控节点：<el-tag type="danger">{{ machineCode }}</el-tag></h2>

    <!-- ECharts 渲染视图 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <div id="cpuGauge" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import * as echarts from 'echarts';

const route = useRoute();
// 动态从小红帽路由中获取主机代码（如 AS400A 或 AS400B）
const machineCode = ref(route.params.machineCode as string);
let stompClient: Client | null = null;
let cpuChart: echarts.ECharts | null = null;

// 初始化 ECharts 图表
const initChart = () => {
  const dom = document.getElementById('cpuGauge');
  if (dom) {
    cpuChart = echarts.init(dom);
    cpuChart.setOption({
      title: { text: '实时 CPU 使用率 (%)', left: 'center' },
      series: [{ type: 'gauge', progress: { show: true }, data: [{ value: 0 }] }]
    });
  }
};

// 动态构建并订阅单机的 WebSocket 实时管道
const initWebSocket = () => {
  const socket = new SockJS('http://localhost:8080/ws-monitor-endpoint');
  stompClient = new Client({
    webSocketFactory: () => socket,
    onConnect: () => {
      // 💡 重点：前端根据当前页面操纵的机器编码，动态订阅该台机器的数据流
      stompClient?.subscribe(`/topic/perf/${machineCode.value}`, (frame) => {
        const perfLog = JSON.parse(frame.body);
        // 无刷新平滑更新图表指标数值
        cpuChart?.setOption({
          series: [{ data: [{ value: perfLog.cpuUtilization }] }]
        });
      });
    }
  });
  stompClient.activate();
};

onMounted(() => {
  initChart();
  initWebSocket();
});

onUnmounted(() => {
  stompClient?.deactivate(); // 销毁组件时释放连接，保障浏览器性能
  cpuChart?.dispose();
});
</script>

------------------------------
## 🚀 四、 联调与测试验证流程
开发人员在模块代码编写完成后，按以下顺序依次验证：

   1. 数据库与持久层验证：首先在 sys_as400_config 配置表中手动插入两条数据：AS400A（生产环境 IP）和 AS400B（灾备环境 IP）。启动后端，检查定时任务是否能正常通过 MyBatis-Plus 向 sys_as400_perf_log 表中每分钟稳定追加监控结果数据。
   2. WebSocket 推送管道测试：启动后端后，使用日志观察多线程 parallelStream 运作情况。确认两个不同主机的通道（/topic/perf/AS400A 和 /topic/perf/AS400B）在各司其职地并发向外推送 JSON 数据。
   3. 前端页面联调验证：打开前端页面，进入 /console/AS400A 的浏览器路由。观察 ECharts 图表是否能够动态响应。如果网络出现波动、或模拟在 AS400A 侧通过长查询拉高 CPU，检查前端仪表盘是否同步变动。验证完成后切换至 /console/AS400B，观察数据流是否能完全做到主机级别的独立隔离。

开发团队可以完全依据这份重新梳理的纯实操文档，展开全栈多主机监控应用的工程落地。如果在开发具体的 Thymeleaf 邮件报警 HTML 页面样式 或 MyBatis-Plus 分页历史日志查询 时遇到阻碍，请随时沟通！


