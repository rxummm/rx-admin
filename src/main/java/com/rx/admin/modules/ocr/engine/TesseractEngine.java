package com.rx.admin.modules.ocr.engine;

import com.rx.admin.common.config.AppConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor
public class TesseractEngine {

    private final AppConfig appConfig;
    private final ImagePreprocessor imagePreprocessor;

    private Tesseract tesseract;

    @Data
    public static class OcrResult {
        private String text;
        private double confidence;
        private long durationMs;

        public OcrResult() {}

        public OcrResult(String text, double confidence, long durationMs) {
            this.text = text;
            this.confidence = confidence;
            this.durationMs = durationMs;
        }
    }

    @PostConstruct
    public void init() {
        try {
            tesseract = new Tesseract();
            String tessdataPath = appConfig.getOcr() != null ? appConfig.getOcr().getTessdataPath() : "D:\\tessdata";
            tesseract.setDatapath(tessdataPath);
            String lang = appConfig.getOcr() != null ? appConfig.getOcr().getDefaultLanguage() : "chi_sim+eng";
            tesseract.setLanguage(lang);
            tesseract.setPageSegMode(3);
            log.info("Tesseract OCR 初始化完成: tessdata={}, language={}", tessdataPath, lang);
        } catch (Exception e) {
            log.warn("Tesseract OCR 初始化失败: {}", e.getMessage());
        }
    }

    public OcrResult recognizeImage(File imageFile) {
        if (tesseract == null) {
            throw new RuntimeException("Tesseract OCR 未初始化");
        }
        long start = System.currentTimeMillis();

        File preprocessed = null;
        try {
            // 图片预处理提升准确率
            preprocessed = imagePreprocessor.preprocess(imageFile);
            String text = tesseract.doOCR(preprocessed);
            long duration = System.currentTimeMillis() - start;
            return new OcrResult(text != null ? text.trim() : "", 0.0, duration);
        } catch (TesseractException e) {
            throw new RuntimeException("OCR 识别失败: " + e.getMessage(), e);
        } finally {
            // 清理预处理临时文件
            if (preprocessed != null && preprocessed != imageFile) {
                preprocessed.delete();
            }
        }
    }

    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("tesseract", "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean ok = p.waitFor() == 0;
            p.destroy();
            return ok;
        } catch (Exception e) {
            log.debug("Tesseract 不可用: {}", e.getMessage());
            return false;
        }
    }
}
