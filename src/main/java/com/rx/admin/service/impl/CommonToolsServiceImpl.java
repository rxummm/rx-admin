package com.rx.admin.service.impl;

import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.entity.SharedFile;
import com.rx.admin.mapper.SharedFileMapper;
import com.rx.admin.service.CommonToolsService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommonToolsServiceImpl extends ServiceImpl<SharedFileMapper, SharedFile> implements CommonToolsService {

    @Value("${common-tools.upload.dir:D:\\vueprojects\\RX\\ui\\public\\shareddocs}")
    private String defaultUploadDir;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    // ==================== Excel解析 ====================

    @Override
    public List<Map<String, Object>> parseExcel(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过50MB限制");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("仅支持 .xlsx 和 .xls 格式的Excel文件");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        boolean[] headerParsed = {false};

        FastExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                if (!headerParsed[0]) {
                    // 第一行作为表头
                    for (Map.Entry<Integer, String> entry : data.entrySet()) {
                        String val = entry.getValue() != null ? entry.getValue().trim() : "";
                        headers.add(val.isEmpty() ? "列" + (entry.getKey() + 1) : val);
                    }
                    headerParsed[0] = true;
                    return;
                }
                // 数据行
                Map<String, Object> rowData = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int j = 0; j < headers.size(); j++) {
                    String value = data.getOrDefault(j, "");
                    if (value == null) value = "";
                    rowData.put(headers.get(j), value);
                    if (!value.isEmpty()) hasValue = true;
                }
                if (hasValue) result.add(rowData);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 读取完成
            }
        }).sheet().headRowNumber(0).doRead();

        return result;
    }

    // ==================== 文档上传 ====================

    @Override
    public SharedFile uploadDocument(MultipartFile file, String targetDir) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过50MB限制");
        }

        String uploadDir = (targetDir != null && !targetDir.isBlank()) ? targetDir : defaultUploadDir;
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + extension;
        Path filePath = dirPath.resolve(storedName);

        file.transferTo(filePath.toFile());

        SharedFile sf = new SharedFile();
        sf.setFileName(originalName);
        sf.setStoredName(storedName);
        sf.setFilePath(filePath.toString());
        sf.setFileSize(file.getSize());
        sf.setFileType(extension.toLowerCase());
        sf.setUploadUser("system");
        sf.setUploadTime(LocalDateTime.now());
        save(sf);

        return sf;
    }

    @Override
    public Page<SharedFile> getUploadedFiles(int page, int size, String keyword) {
        Page<SharedFile> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SharedFile> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SharedFile::getFileName, keyword);
        }
        wrapper.orderByDesc(SharedFile::getUploadTime);
        return page(pageParam, wrapper);
    }

    @Override
    public boolean deleteFile(Long id) {
        SharedFile sf = getById(id);
        if (sf == null) return false;
        try {
            Files.deleteIfExists(Paths.get(sf.getFilePath()));
        } catch (IOException ignored) {
        }
        return removeById(id);
    }

    @Override
    public String getDefaultUploadDir() {
        return defaultUploadDir;
    }

    // ==================== 文档格式转换 ====================

    @Override
    public String convertPdfToWord(MultipartFile file, String outputDir) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("仅支持PDF文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过50MB限制");
        }

        String outDir = (outputDir != null && !outputDir.isBlank()) ? outputDir : defaultUploadDir;
        Path dirPath = Paths.get(outDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 提取PDF文本
        String text;
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            text = stripper.getText(document);
        }

        // 生成Word文档
        String outputFileName = originalName.replaceAll("\\.(?i)pdf$", "") + "_converted.docx";
        Path outputPath = dirPath.resolve(outputFileName);

        try (XWPFDocument docx = new XWPFDocument()) {
            String[] lines = text.split("\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    XWPFParagraph para = docx.createParagraph();
                    para.createRun(); // 空行
                } else {
                    XWPFParagraph para = docx.createParagraph();
                    XWPFRun run = para.createRun();
                    run.setText(line);
                    run.setFontSize(12);
                    run.setFontFamily("宋体");
                }
            }
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                docx.write(fos);
            }
        }

        return outputPath.toString();
    }

    @Override
    public String convertWordToPdf(MultipartFile file, String outputDir) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".docx")) {
            throw new IllegalArgumentException("仅支持.docx文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过50MB限制");
        }

        String outDir = (outputDir != null && !outputDir.isBlank()) ? outputDir : defaultUploadDir;
        Path dirPath = Paths.get(outDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 读取Word文本内容
        StringBuilder text = new StringBuilder();
        try (XWPFDocument docx = new XWPFDocument(file.getInputStream())) {
            for (XWPFParagraph para : docx.getParagraphs()) {
                String line = para.getText();
                if (line != null) {
                    text.append(line).append("\n");
                }
            }
        }

        // 生成PDF
        String outputFileName = originalName.replaceAll("\\.(?i)docx$", "") + "_converted.pdf";
        Path outputPath = dirPath.resolve(outputFileName);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                PDType0Font font = loadChineseFont(document);
                contentStream.setFont(font, 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 750);

                String[] lines = text.toString().split("\\n");
                for (String line : lines) {
                    if (line.length() > 80) {
                        line = line.substring(0, 80);
                    }
                    // 过滤字体不支持的字符，替换为 ?
                    String safeLine = encodeSafe(line, font);
                    if (!safeLine.isEmpty()) {
                        contentStream.showText(safeLine);
                    }
                    contentStream.newLine();
                }

                contentStream.endText();
            }
            document.save(outputPath.toFile());
        }

        return outputPath.toString();
    }

    /**
     * 按优先级尝试加载系统中文字体（仅 .ttf/.otf，PDFBox 对 .ttc 支持不佳）
     */
    private PDType0Font loadChineseFont(PDDocument document) throws IOException {
        String[] fontPaths = {
            "C:/Windows/Fonts/simhei.ttf",         // Windows 黑体
            "C:/Windows/Fonts/SimsunExtG.ttf",     // Windows 宋体扩展集（字符覆盖更广）
            "C:/Windows/Fonts/simfang.ttf",        // Windows 仿宋
            "C:/Windows/Fonts/simkai.ttf",         // Windows 楷体
            "C:/Windows/Fonts/STSONG.TTF",         // Windows 华文宋体
            "C:/Windows/Fonts/Deng.ttf",           // Windows 等线
            "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/System/Library/Fonts/STSong.ttf",
        };
        for (String path : fontPaths) {
            File fontFile = new File(path);
            if (fontFile.exists()) {
                try {
                    return PDType0Font.load(document, fontFile);
                } catch (IOException e) {
                    // 该字体加载失败，继续尝试下一个
                }
            }
        }
        throw new IOException("未找到可用的中文字体文件(.ttf/.otf)。"
                + " 已尝试路径: " + String.join(", ", fontPaths));
    }

    /**
     * 过滤字符串中字体不支持的字符，替换为 ?
     */
    private String encodeSafe(String text, PDType0Font font) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            try {
                font.encode(text.substring(i, i + 1));
                sb.append(c);
            } catch (Exception e) {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
