package com.rx.admin.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;

/**
 * 图形验证码生成工具
 * 生成包含随机字母数字的图片，返回 Base64 编码
 */
public class CaptchaUtil {

    private static final int WIDTH = 130;
    private static final int HEIGHT = 48;
    private static final int CODE_LENGTH = 4;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Random RANDOM = new Random();

    /** 生成随机验证码文本 */
    public static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /** 生成验证码图片并返回 Base64 */
    public static String generateBase64(String code) {
        BufferedImage image = createImage(code);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Captcha image generation failed", e);
        }
    }

    private static BufferedImage createImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font("Arial", Font.BOLD, 28));

        // 干扰线
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 8; i++) {
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 噪点
        for (int i = 0; i < 60; i++) {
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g.setColor(new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200)));
            g.drawOval(x, y, 2, 2);
        }

        // 绘制验证码字符
        char[] chars = code.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            g.setColor(new Color(RANDOM.nextInt(100), RANDOM.nextInt(150), RANDOM.nextInt(200)));
            double angle = (RANDOM.nextDouble() - 0.5) * 0.4;
            g.rotate(angle, 20 + i * 28, HEIGHT / 2 + 5);
            g.drawString(String.valueOf(chars[i]), 15 + i * 28, HEIGHT / 2 + 10);
            g.rotate(-angle, 20 + i * 28, HEIGHT / 2 + 5);
        }

        g.dispose();
        return image;
    }
}