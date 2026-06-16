package com.rx.admin.modules.ocr.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * OCR 图片预处理器
 * 灰度化 → 二值化 → 去噪 → 纠偏，提升 Tesseract 识别准确率
 */
@Slf4j
@Component
public class ImagePreprocessor {

    /**
     * 预处理图片，返回优化后的临时文件
     * 调用方负责删除临时文件
     */
    public File preprocess(File inputImage) {
        try {
            BufferedImage original = ImageIO.read(inputImage);
            if (original == null) {
                log.warn("无法读取图片: {}", inputImage.getName());
                return inputImage;
            }

            log.info("图片预处理开始: {}x{}, type={}", original.getWidth(), original.getHeight(), original.getType());

            // 1. 灰度化
            BufferedImage gray = toGrayscale(original);

            // 2. 二值化（Otsu 自动阈值）
            BufferedImage binary = binarize(gray);

            // 3. 去噪（中值滤波去椒盐噪声）
            BufferedImage denoised = denoise(binary);

            // 4. 纠偏（微小旋转修正）
            BufferedImage deskewed = deskew(denoised);

            // 写入临时文件
            File output = File.createTempFile("ocr-preprocessed-", ".png");
            output.deleteOnExit();
            ImageIO.write(deskewed, "png", output);

            log.info("图片预处理完成: {} -> {}", inputImage.getName(), output.getName());
            return output;

        } catch (IOException e) {
            log.warn("图片预处理失败，使用原图: {}", e.getMessage());
            return inputImage;
        }
    }

    /** 灰度化：RGB → 灰度 */
    private BufferedImage toGrayscale(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage gray = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    /** 二值化：Otsu 自动阈值 */
    private BufferedImage binarize(BufferedImage gray) {
        int w = gray.getWidth();
        int h = gray.getHeight();

        // 统计直方图
        int[] histogram = new int[256];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                histogram[gray.getRGB(x, y) & 0xFF]++;
            }
        }

        // Otsu 阈值计算
        int total = w * h;
        double sum = 0;
        for (int i = 0; i < 256; i++) sum += (double) i * histogram[i];

        double sumB = 0;
        int wB = 0;
        double maxVariance = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            int wF = total - wB;
            if (wF == 0) break;

            sumB += (double) i * histogram[i];
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double variance = (double) wB * wF * (mB - mF) * (mB - mF);

            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }

        // 应用二值化
        BufferedImage binary = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int grayVal = gray.getRGB(x, y) & 0xFF;
                binary.setRGB(x, y, grayVal < threshold ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return binary;
    }

    /** 去噪：3x3 中值滤波，去除椒盐噪声 */
    private BufferedImage denoise(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, src.getType());

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int[] neighbors = new int[9];
                int idx = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        neighbors[idx++] = src.getRGB(x + dx, y + dy) & 0xFF;
                    }
                }
                java.util.Arrays.sort(neighbors);
                int median = neighbors[4]; // 中值
                result.setRGB(x, y, new Color(median, median, median).getRGB());
            }
        }
        return result;
    }

    /** 纠偏：检测倾斜角度并微调（±5° 范围） */
    private BufferedImage deskew(BufferedImage src) {
        // 简易水平投影法检测倾斜

        double bestAngle = 0;
        double maxScore = 0;

        // 尝试 -5° 到 +5°，步长 0.5°
        for (double angle = -5; angle <= 5; angle += 0.5) {
            double score = calcProjectionScore(src, angle);
            if (score > maxScore) {
                maxScore = score;
                bestAngle = angle;
            }
        }

        if (Math.abs(bestAngle) < 0.5) {
            return src; // 倾斜太小，不处理
        }

        log.info("检测到倾斜 {}°，执行纠偏", String.format("%.1f", bestAngle));
        return rotate(src, bestAngle);
    }

    /** 计算水平投影得分（越集中越好） */
    private double calcProjectionScore(BufferedImage src, double angle) {
        BufferedImage rotated = rotate(src, angle);
        int w = rotated.getWidth();
        int h = rotated.getHeight();
        int[] projection = new int[h];

        for (int y = 0; y < h; y++) {
            int count = 0;
            for (int x = 0; x < w; x++) {
                if ((rotated.getRGB(x, y) & 0xFF) < 128) count++;
            }
            projection[y] = count;
        }

        // 计算投影方差（越集中越好）
        double sum = 0, sumSq = 0;
        for (int v : projection) { sum += v; sumSq += (double) v * v; }
        double mean = sum / h;
        return sumSq / h - mean * mean;
    }

    /** 旋转图片 */
    private BufferedImage rotate(BufferedImage src, double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        double sin = Math.abs(Math.sin(rad));
        double cos = Math.abs(Math.cos(rad));
        int w = src.getWidth();
        int h = src.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newW, newH, src.getType());
        Graphics2D g = rotated.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.translate((newW - w) / 2.0, (newH - h) / 2.0);
        g.rotate(rad, w / 2.0, h / 2.0);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rotated;
    }
}
