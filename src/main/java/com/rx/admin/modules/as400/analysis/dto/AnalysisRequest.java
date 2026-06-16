package com.rx.admin.modules.as400.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "AS400 代码分析请求")
public class AnalysisRequest {

    @NotBlank
    @Schema(description = "源码文本", example = "**free ...")
    private String sourceCode;

    @Schema(description = "源码类型: RPGLE / RPG3 / CL / DDS_PF / DDS_LF / DSPF / PRTF", example = "RPGLE")
    private String sourceType = "RPGLE";

    @Schema(description = "源码文件名（带扩展名）", example = "MYPGM.rpgle")
    private String fileName;
}
