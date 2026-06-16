package com.rx.admin.modules.as400.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class IServiceItemVO {
    private Long id;
    private Long categoryId;
    private String serviceName;
    private String systemObjectName;
    private String serviceType;
    private String briefDescription;
    private String fullDescription;
    private String docUrl;
    private String earliestPossibleRelease;
    private Integer initialDb2GroupLevel;
    private Integer latestDb2GroupLevel;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<IServiceParameterVO> parameters;
    private List<IServiceColumnVO> columns;
    private List<IServiceExampleVO> examples;
    private List<IServiceAuthorityVO> authorities;
}