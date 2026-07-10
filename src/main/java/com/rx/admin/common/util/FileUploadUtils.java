package com.rx.admin.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
public class FileUploadUtils {

    private FileUploadUtils() {}

    public static Path saveTempFile(MultipartFile file, Path tempDir) throws IOException {
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        String safeFileName = System.currentTimeMillis() + "_" + sanitizeFileName(file.getOriginalFilename());
        Path tempFile = tempDir.resolve(safeFileName);
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    public static void cleanupTempFile(Path tempFile) {
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("清理临时文件失败: {}", tempFile, e);
            }
        }
    }

    public static String sanitizeFileName(String fileName) {
        if (fileName == null) return "file";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public static boolean isValidFile(String fileName, List<String> validExtensions) {
        if (fileName == null) return false;
        String lowerName = fileName.toLowerCase();
        return validExtensions.stream().anyMatch(lowerName::endsWith);
    }
}
