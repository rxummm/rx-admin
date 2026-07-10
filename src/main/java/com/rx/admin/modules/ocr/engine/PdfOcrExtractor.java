package com.rx.admin.modules.ocr.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfOcrExtractor {

    private final TesseractEngine tesseractEngine;

    /** 单页文字少于此阈值时，判定为图片页，走 OCR */
    private static final int TEXT_THRESHOLD = 30;

    public String extractText(File pdfFile) {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int totalPages = document.getNumberOfPages();
            log.info("PDF 共 {} 页，开始逐页处理", totalPages);

            PDFTextStripper stripper = new PDFTextStripper();
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder allText = new StringBuilder();
            Path tempDir = Files.createTempDirectory("ocr-pdf-");

            try {
                for (int i = 0; i < totalPages; i++) {
                    int pageNum = i + 1;

                    // 提取当前页文字
                    stripper.setStartPage(pageNum);
                    stripper.setEndPage(pageNum);
                    String pageText = stripper.getText(document);
                    if (pageText != null) {
                        pageText = pageText.trim();
                    }

                    if (pageText != null && pageText.length() >= TEXT_THRESHOLD) {
                        // 文字页：直接使用提取的文字
                        log.info("第 {} 页: 文字页 ({} 字符)", pageNum, pageText.length());
                        allText.append(pageText).append("\n\n");
                    } else {
                        // 图片页：渲染为图片后 OCR
                        log.info("第 {} 页: 图片页，开始 OCR (文字层仅 {} 字符)", pageNum,
                                pageText != null ? pageText.length() : 0);
                        BufferedImage image = renderer.renderImageWithDPI(i, 300);
                        File tempImage = tempDir.resolve("page_" + pageNum + ".png").toFile();
                        ImageIO.write(image, "png", tempImage);

                        try {
                            TesseractEngine.OcrResult ocrResult = tesseractEngine.recognizeImage(tempImage);
                            String ocrText = ocrResult.getText();
                            if (ocrText != null && !ocrText.isEmpty()) {
                                allText.append(ocrText).append("\n\n");
                            }
                        } finally {
                            Files.deleteIfExists(tempImage.toPath());
                        }
                    }
                }
            } finally {
                // 清理临时目录
                try { Files.deleteIfExists(tempDir); } catch (Exception e) { log.debug("临时目录清理失败", e); }
            }

            String result = allText.toString().trim();
            log.info("PDF 处理完成: {} 字符", result.length());
            return result;

        } catch (IOException e) {
            throw new RuntimeException("PDF 解析失败: " + e.getMessage(), e);
        }
    }
}
