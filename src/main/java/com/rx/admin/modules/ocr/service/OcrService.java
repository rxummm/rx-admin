package com.rx.admin.modules.ocr.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.config.AppConfig;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.ocr.convert.OcrConvert;
import com.rx.admin.modules.ocr.dto.OcrQueryDTO;
import com.rx.admin.modules.ocr.engine.PdfOcrExtractor;
import com.rx.admin.modules.ocr.engine.TesseractEngine;
import com.rx.admin.modules.ocr.entity.OcrRecognition;
import com.rx.admin.modules.ocr.mapper.OcrRecognitionMapper;
import com.rx.admin.modules.ocr.vo.OcrRecognitionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OcrService implements IOcrService {

    private final OcrRecognitionMapper ocrMapper;
    private final TesseractEngine tesseractEngine;
    private final PdfOcrExtractor pdfExtractor;
    private final OcrConvert ocrConvert;
    private final AppConfig appConfig;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".bmp", ".tiff", ".tif", ".gif");
    private static final Set<String> TEXT_EXTENSIONS = Set.of(
        ".txt", ".md", ".html", ".htm", ".xml", ".csv", ".json", ".yaml", ".yml",
        ".properties", ".sql", ".java", ".js", ".ts", ".py", ".go", ".rs",
        ".css", ".scss", ".less", ".sh", ".bat", ".log", ".ini", ".cfg", ".conf"
    );
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    @Override
    @Transactional
    public OcrRecognitionVO recognize(File file, String originalName, String language) {
        String fileType = getFileExtension(originalName);
        String effectiveLanguage = (language != null && !language.isEmpty()) ? language : "chi_sim+eng";

        OcrRecognition record = new OcrRecognition();
        record.setFileName(originalName);
        record.setFilePath(file.getAbsolutePath());
        record.setFileType(fileType);
        record.setFileSize(file.length());
        record.setLanguage(effectiveLanguage);
        record.setStatus(2);
        ocrMapper.insert(record);

        try {
            long start = System.currentTimeMillis();
            String text;
            String engine;
            float confidence = 0f;
            int pageCount = 1;

            if ("pdf".equals(fileType)) {
                text = pdfExtractor.extractText(file);
                engine = "pdfbox+tesseract";
            } else if ("docx".equals(fileType)) {
                text = extractDocxText(file);
                engine = "poi+tesseract";
            } else if ("xls".equals(fileType) || "xlsx".equals(fileType)) {
                text = extractExcelText(file);
                engine = "poi+tesseract";
            } else if (IMAGE_EXTENSIONS.contains("." + fileType)) {
                TesseractEngine.OcrResult result = tesseractEngine.recognizeImage(file);
                text = result.getText();
                confidence = (float) result.getConfidence();
                engine = "tesseract";
            } else if (TEXT_EXTENSIONS.contains("." + fileType) || "doc".equals(fileType)) {
                text = readPlainText(file);
                engine = "text-reader";
            } else {
                throw new RuntimeException("不支持的文件格式: " + fileType);
            }

            if (text == null) text = "";

            record.setResultText(text);
            record.setCharCount(text.length());
            record.setOcrEngine(engine);
            record.setConfidence(confidence);
            record.setPageCount(pageCount);
            record.setStatus(1);
            record.setDurationMs(System.currentTimeMillis() - start);
            ocrMapper.updateById(record);

            log.info("OCR 识别完成: file={}, engine={}, chars={}, duration={}ms",
                    originalName, engine, text.length(), record.getDurationMs());
            return ocrConvert.toVO(record);

        } catch (Exception e) {
            log.error("OCR 识别失败", e);
            record.setStatus(0);
            record.setErrorMessage(e.getMessage());
            ocrMapper.updateById(record);
            throw new RuntimeException("OCR 识别失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OcrRecognitionVO uploadAndRecognize(MultipartFile multipartFile, String language) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        if (multipartFile.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小超过50MB限制");
        }

        String originalName = multipartFile.getOriginalFilename();
        if (originalName == null) originalName = "unknown";

        Path tempDir = getTempDir();
        String safeName = System.currentTimeMillis() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path tempFile = tempDir.resolve(safeName);

        try {
            Files.createDirectories(tempDir);
            multipartFile.transferTo(tempFile.toFile());
            return recognize(tempFile.toFile(), originalName, language);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }
    }

    @Override
    public OcrRecognitionVO getById(Long id) {
        OcrRecognition record = ocrMapper.selectById(id);
        if (record == null) return null;
        return ocrConvert.toVO(record);
    }

    @Override
    public PageResult<OcrRecognitionVO> pageQuery(OcrQueryDTO query) {
        LambdaQueryWrapper<OcrRecognition> wrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(OcrRecognition::getFileName, query.getKeyword());
        }
        if (query.getFileType() != null && !query.getFileType().isEmpty()) {
            wrapper.eq(OcrRecognition::getFileType, query.getFileType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(OcrRecognition::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(OcrRecognition::getCreateTime);

        IPage<OcrRecognition> page = new Page<>(query.getPage(), query.getSize());
        page = ocrMapper.selectPage(page, wrapper);

        List<OcrRecognitionVO> voList = page.getRecords().stream()
            .map(ocrConvert::toVO)
            .toList();

        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        OcrRecognition record = ocrMapper.selectById(id);
        if (record != null && record.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(record.getFilePath()));
            } catch (IOException e) {
                log.warn("删除文件失败: {}", record.getFilePath(), e);
            }
        }
        ocrMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Override
    public String generateResultTxt(Long id) {
        OcrRecognition record = ocrMapper.selectById(id);
        if (record == null || record.getResultText() == null) {
            return "";
        }
        return record.getResultText();
    }

    private String extractDocxText(File docxFile) {
        StringBuilder text = new StringBuilder();
        Path tempDir = null;
        try (var fis = new java.io.FileInputStream(docxFile);
             XWPFDocument docx = new XWPFDocument(fis)) {

            // 1. 提取文字
            for (XWPFParagraph para : docx.getParagraphs()) {
                String line = para.getText();
                if (line != null && !line.isEmpty()) {
                    text.append(line).append("\n");
                }
            }

            // 2. 提取图片并 OCR
            tempDir = Files.createTempDirectory("ocr-docx-");
            var images = docx.getAllPictures();
            if (images != null && !images.isEmpty()) {
                log.info("Word 文档含 {} 张图片，开始 OCR", images.size());
                for (int i = 0; i < images.size(); i++) {
                    var picture = images.get(i);
                    try {
                        byte[] data = picture.getData();
                        if (data == null || data.length == 0) continue;

                        String fileName = "img_" + (i + 1) + ".png";
                        Path imgPath = tempDir.resolve(fileName);
                        Files.write(imgPath, data);

                        TesseractEngine.OcrResult ocrResult = tesseractEngine.recognizeImage(imgPath.toFile());
                        String ocrText = ocrResult.getText();
                        if (ocrText != null && !ocrText.isEmpty()) {
                            text.append("\n[图片 ").append(i + 1).append(" 识别结果]\n");
                            text.append(ocrText).append("\n");
                        }

                        Files.deleteIfExists(imgPath);
                    } catch (Exception e) {
                        log.warn("Word 图片 {} OCR 失败: {}", i + 1, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Word 文档解析失败: " + e.getMessage(), e);
        } finally {
            if (tempDir != null) {
                try { Files.deleteIfExists(tempDir); } catch (Exception ignored) {}
            }
        }
        return text.toString().trim();
    }

    private String readPlainText(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("文本文件读取失败: " + e.getMessage(), e);
        }
    }

    private String extractExcelText(File excelFile) {
        StringBuilder text = new StringBuilder();
        Path tempDir = null;
        try (var fis = new java.io.FileInputStream(excelFile);
             Workbook workbook = excelFile.getName().endsWith(".xlsx")
                 ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                if (sheet == null) continue;
                text.append("[").append(sheet.getSheetName()).append("]\n");

                for (Row row : sheet) {
                    if (row == null) continue;
                    StringBuilder rowText = new StringBuilder();
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        if (cell == null) continue;
                        String cellValue = getCellStringValue(cell);
                        if (cellValue != null && !cellValue.isEmpty()) {
                            if (!rowText.isEmpty()) rowText.append("\t");
                            rowText.append(cellValue);
                        }
                    }
                    if (!rowText.isEmpty()) {
                        text.append(rowText).append("\n");
                    }
                }
                text.append("\n");
            }

            // 提取图片并 OCR（仅 xlsx 支持）
            if (workbook instanceof XSSFWorkbook xlsx) {
                var pictures = xlsx.getAllPictures();
                if (pictures != null && !pictures.isEmpty()) {
                    log.info("Excel 含 {} 张图片，开始 OCR", pictures.size());
                    tempDir = Files.createTempDirectory("ocr-excel-");
                    for (int i = 0; i < pictures.size(); i++) {
                        var picture = pictures.get(i);
                        try {
                            byte[] data = picture.getData();
                            if (data == null || data.length == 0) continue;
                            Path imgPath = tempDir.resolve("img_" + (i + 1) + ".png");
                            Files.write(imgPath, data);
                            TesseractEngine.OcrResult ocrResult = tesseractEngine.recognizeImage(imgPath.toFile());
                            String ocrText = ocrResult.getText();
                            if (ocrText != null && !ocrText.isEmpty()) {
                                text.append("[图片 ").append(i + 1).append(" 识别结果]\n");
                                text.append(ocrText).append("\n");
                            }
                            Files.deleteIfExists(imgPath);
                        } catch (Exception e) {
                            log.warn("Excel 图片 {} OCR 失败: {}", i + 1, e.getMessage());
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Excel 解析失败: " + e.getMessage(), e);
        } finally {
            if (tempDir != null) {
                try { Files.deleteIfExists(tempDir); } catch (Exception ignored) {}
            }
        }
        return text.toString().trim();
    }

    private String getCellStringValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) && !Double.isInfinite(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue(); } catch (Exception e1) {
                    try { yield String.valueOf(cell.getNumericCellValue()); } catch (Exception e2) { yield ""; }
                }
            }
            default -> "";
        };
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return "";
        return fileName.substring(dot + 1).toLowerCase();
    }

    private Path getTempDir() {
        String configuredDir = appConfig.getOcr() != null ? appConfig.getOcr().getTempDir() : null;
        if (configuredDir != null && !configuredDir.isEmpty()) {
            return Paths.get(configuredDir);
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "rx-ocr-upload");
    }
}
