package com.rx.admin.modules.tool.commonTools.service;

import com.rx.admin.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * 数据导入增强服务
 * 支持 Excel/CSV 导入预览和错误提示
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportService {

    /**
     * 预览 Excel 文件
     */
    public Map<String, Object> previewExcel(MultipartFile file) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows();
            
            // 获取表头
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                    Cell cell = headerRow.getCell(i);
                    headers.add(cell != null ? cell.toString() : "");
                }
            }
            
            // 预览前10行数据
            List<List<String>> previewData = new ArrayList<>();
            int previewRows = Math.min(10, totalRows - 1);
            for (int i = 1; i <= previewRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                List<String> rowData = new ArrayList<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.add(cell != null ? cell.toString() : "");
                }
                previewData.add(rowData);
            }
            
            result.put("headers", headers);
            result.put("previewData", previewData);
            result.put("totalRows", totalRows - 1); // 减去表头
            result.put("fileName", file.getOriginalFilename());
            
            return result;
        }
    }

    /**
     * 导入数据（带错误处理）
     */
    public Map<String, Object> importData(MultipartFile file, Map<String, String> fieldMapping) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> successRecords = new ArrayList<>();
        List<Map<String, Object>> errorRecords = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            // 获取表头
            List<String> headers = new ArrayList<>();
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                    Cell cell = headerRow.getCell(i);
                    headers.add(cell != null ? cell.toString() : "");
                }
            }
            
            // 导入数据
            int totalRows = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Map<String, String> record = new LinkedHashMap<>();
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        String value = cell != null ? cell.toString() : "";
                        String fieldName = fieldMapping.getOrDefault(headers.get(j), headers.get(j));
                        record.put(fieldName, value);
                    }
                    successRecords.add(record);
                } catch (Exception e) {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("row", i + 1);
                    error.put("error", e.getMessage());
                    errorRecords.add(error);
                }
            }
            
            result.put("successCount", successRecords.size());
            result.put("errorCount", errorRecords.size());
            result.put("successRecords", successRecords);
            result.put("errorRecords", errorRecords);
            
            return result;
        }
    }

    /**
     * 验证导入数据
     */
    public Map<String, Object> validateImportData(List<Map<String, String>> data, Map<String, String> validationRules) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        
        for (int i = 0; i < data.size(); i++) {
            Map<String, String> record = data.get(i);
            Map<String, String> recordErrors = new LinkedHashMap<>();
            
            for (Map.Entry<String, String> rule : validationRules.entrySet()) {
                String field = rule.getKey();
                String ruleStr = rule.getValue();
                String value = record.get(field);
                
                if (value == null || value.isEmpty()) {
                    if (ruleStr.contains("required")) {
                        recordErrors.put(field, "字段不能为空");
                    }
                } else {
                    if (ruleStr.contains("email") && !value.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                        recordErrors.put(field, "邮箱格式不正确");
                    }
                    if (ruleStr.contains("phone") && !value.matches("^1[3-9]\\d{9}$")) {
                        recordErrors.put(field, "手机号格式不正确");
                    }
                }
            }
            
            if (!recordErrors.isEmpty()) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("row", i + 1);
                error.put("errors", recordErrors);
                errors.add(error);
            }
        }
        
        result.put("valid", errors.isEmpty());
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        
        return result;
    }
}
