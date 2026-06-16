package com.rx.admin.modules.tool.export.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ExportRequestDTO {
    private String title;
    private List<Map<String, String>> columns;
    private List<Map<String, Object>> data;
}
