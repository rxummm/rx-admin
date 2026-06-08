package com.rx.admin.service.impl;

import com.rx.admin.entity.SysExportConfig;
import com.rx.admin.mapper.SysExportConfigMapper;
import com.rx.admin.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    private final SysExportConfigMapper exportConfigMapper;

    public ExportServiceImpl(SysExportConfigMapper exportConfigMapper) {
        this.exportConfigMapper = exportConfigMapper;
    }

    // ──────────────────── Excel ────────────────────

    @Override
    public byte[] exportExcel(String title, List<Map<String, String>> columns, List<Map<String, Object>> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(trimSheetName(title));

            // ---- 标题行 ----
            CellStyle titleStyle = createTitleStyle(workbook);
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(28);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title + "  导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            titleCell.setCellStyle(titleStyle);
            if (columns.size() > 1) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.size() - 1));
            }

            // ---- 表头行 ----
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).get("label"));
                cell.setCellStyle(headerStyle);
            }

            // ---- 数据行 ----
            CellStyle dataCellStyle = createDataCellStyle(workbook);
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 2);
                Map<String, Object> rowData = data.get(i);
                for (int j = 0; j < columns.size(); j++) {
                    String field = columns.get(j).get("field");
                    Object value = rowData.get(field);
                    Cell cell = row.createCell(j);
                    if (value != null) {
                        cell.setCellValue(String.valueOf(value));
                    }
                    cell.setCellStyle(dataCellStyle);
                }
            }

            // ---- 自动列宽（限制最大 40 字符） ----
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
                int colWidth = sheet.getColumnWidth(i);
                if (colWidth > 40 * 256) {
                    sheet.setColumnWidth(i, 40 * 256);
                } else if (colWidth < 10 * 256) {
                    sheet.setColumnWidth(i, 10 * 256);
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            log.info("Excel导出成功 -> 标题: {}, 列数: {}, 行数: {}", title, columns.size(), data.size());
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage(), e);
        }
    }

    // ──────────────────── PDF ────────────────────

    @Override
    public byte[] exportPdf(String title, List<Map<String, String>> columns, List<Map<String, Object>> data) {
        try (PDDocument document = new PDDocument()) {
            PDType0Font font = loadChineseFont(document);
            float fontSize = 8f;
            float titleFontSize = 14f;
            float margin = 40f;
            float rowHeight = 18f;

            PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            float pageHeight = pageSize.getHeight();
            float usableWidth = pageSize.getWidth() - 2 * margin;
            float[] colWidths = calcColumnWidths(columns, usableWidth, font, fontSize);

            int totalRows = data.size();
            int rowIdx = 0;
            boolean firstPage = true;

            // 表头底部 Y（= 页面高度 - 上边距 - 标题区 - 表头行高）
            float headerY = pageHeight - margin - rowHeight;

            List<String> headerLabels = columns.stream().map(c -> c.get("label")).toList();

            while (rowIdx < totalRows) {
                PDPage page = new PDPage(pageSize);
                document.addPage(page);
                PDPageContentStream cs = new PDPageContentStream(document, page);

                // 表头 Y 坐标（倒数计算：距顶部 margin + 可选标题区 + 表头行高）
                float headerTop = pageHeight - margin - (firstPage ? 30f : 0f) - rowHeight;

                // 首页画标题
                if (firstPage) {
                    cs.beginText();
                    cs.setFont(font, titleFontSize);
                    cs.newLineAtOffset(margin, pageHeight - margin);
                    cs.showText(title + "  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    cs.endText();
                    firstPage = false;
                }

                // 画表头（边框 + 文字一起）
                drawRow(cs, margin, headerTop, colWidths, rowHeight, headerLabels, font, fontSize, true);

                // 计算本页可容纳行数
                int rowsThisPage = Math.min(totalRows - rowIdx,
                        (int) ((headerTop - margin) / rowHeight));
                int rowEnd = rowIdx + rowsThisPage;

                // 批量画所有数据边框
                cs.setLineWidth(0.5f);
                for (int r = rowIdx; r < rowEnd; r++) {
                    float ry = headerTop - (r - rowIdx + 1) * rowHeight;
                    float cx = margin;
                    for (int j = 0; j < columns.size(); j++) {
                        cs.addRect(cx, ry, colWidths[j], rowHeight);
                        cx += colWidths[j];
                    }
                }
                cs.stroke();

                // 批量写所有数据文字（一次 beginText）
                cs.beginText();
                cs.setFont(font, fontSize);
                for (int r = rowIdx; r < rowEnd; r++) {
                    float ry = headerTop - (r - rowIdx + 1) * rowHeight;
                    Map<String, Object> rowData = data.get(r);
                    float cx = margin;
                    for (int j = 0; j < columns.size(); j++) {
                        String field = columns.get(j).get("field");
                        Object value = rowData.get(field);
                        String text = value != null ? String.valueOf(value) : "";
                        cs.newLineAtOffset(cx + 3, ry + 4);
                        cs.showText(truncateText(text, font, fontSize, colWidths[j] - 6));
                        cs.newLineAtOffset(-(cx + 3), -(ry + 4));
                        cx += colWidths[j];
                    }
                }
                cs.endText();

                rowIdx = rowEnd;
                cs.close();
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            document.save(bos);
            log.info("PDF导出成功 -> 标题: {}, 列数: {}, 行数: {}", title, columns.size(), data.size());
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("PDF导出失败", e);
            throw new RuntimeException("PDF导出失败: " + e.getMessage(), e);
        }
    }

    // ──────────────────── 配置查询 ────────────────────

    @Override
    public List<String> getExportTypes(String menuPath) {
        SysExportConfig config = exportConfigMapper.selectByMenuPath(menuPath);
        if (config == null) return List.of();
        String types = config.getExportTypes();
        if (types == null || types.isBlank()) return List.of();
        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // ──────────────────── 私有方法 ────────────────────

    private String trimSheetName(String title) {
        if (title == null || title.isBlank()) return "Sheet1";
        // Excel sheet名最长31字符
        String safe = title.replaceAll("[\\\\/*?\\[\\]]", "");
        return safe.length() > 31 ? safe.substring(0, 31) : safe;
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataCellStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private PDType0Font loadChineseFont(PDDocument doc) throws Exception {
        // 尝试多个常见中文字体路径（按顺序：Windows → macOS → Linux）
        String[] fontPaths = {
                "C:/Windows/Fonts/simsun.ttc,0",       // 宋体 (Windows)
                "C:/Windows/Fonts/simhei.ttf",          // 黑体 (Windows)
                "C:/Windows/Fonts/msyh.ttc,0",          // 微软雅黑 (Windows)
                "/System/Library/Fonts/PingFang.ttc,0", // macOS
                "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc", // Linux
        };
        for (String pathEntry : fontPaths) {
            try {
                if (pathEntry.contains(",")) {
                    String[] parts = pathEntry.split(",");
                    return PDType0Font.load(doc, new java.io.File(parts[0]));
                } else {
                    return PDType0Font.load(doc, new java.io.File(pathEntry));
                }
            } catch (Exception ignored) { }
        }
        throw new RuntimeException("未找到中文字体，请确认系统中已安装宋体/黑体/微软雅黑等字体");
    }

    private float[] calcColumnWidths(List<Map<String, String>> columns, float totalWidth,
                                      PDType0Font font, float fontSize) throws Exception {
        float[] widths = new float[columns.size()];
        float totalEstim = 0;
        for (int i = 0; i < columns.size(); i++) {
            String label = columns.get(i).get("label");
            // 粗略按中文字符 * fontSize，英文字符 * fontSize * 0.55
            float w = 0;
            for (char c : label.toCharArray()) {
                w += (c > 127) ? fontSize : fontSize * 0.55f;
            }
            widths[i] = Math.max(w + 20, 60);
            totalEstim += widths[i];
        }
        // 按比例缩放至可用宽度
        if (totalEstim > 0) {
            float scale = totalWidth / totalEstim;
            for (int i = 0; i < widths.length; i++) {
                widths[i] *= scale;
            }
        }
        return widths;
    }

    private void drawRow(PDPageContentStream cs, float startX, float y, float[] colWidths, float rowHeight,
                          List<String> labels, PDType0Font font, float fontSize, boolean isHeader) throws Exception {
        // 边框
        cs.setLineWidth(0.5f);
        float x = startX;
        for (int j = 0; j < colWidths.length; j++) {
            cs.addRect(x, y, colWidths[j], rowHeight);
            x += colWidths[j];
        }
        cs.stroke();
        // 文字
        cs.beginText();
        cs.setFont(font, fontSize);
        x = startX;
        for (int j = 0; j < colWidths.length; j++) {
            String text = labels.size() > j ? labels.get(j) : "";
            cs.newLineAtOffset(x + 3, y + 4);
            cs.showText(truncateText(text, font, fontSize, colWidths[j] - 6));
            cs.newLineAtOffset(-(x + 3), -(y + 4));
            x += colWidths[j];
        }
        cs.endText();
    }

    private String truncateText(String text, PDType0Font font, float fontSize, float maxWidth) throws Exception {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        float w = 0;
        for (char c : text.toCharArray()) {
            float cw = (c > 127) ? fontSize : fontSize * 0.55f;
            if (w + cw > maxWidth) {
                sb.append("…");
                break;
            }
            sb.append(c);
            w += cw;
        }
        return sb.toString();
    }
}
