package com.rx.admin.common.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@SuppressWarnings("null")
public class OcrHealthIndicator implements HealthIndicator {

    @Value("${app.ocr.enabled:false}")
    private boolean enabled;

    @Value("${app.ocr.tessdata-path:}")
    private String tessdataPath;

    @Override
    public Health health() {
        if (!enabled) {
            return Health.unknown().withDetail("message", "OCR disabled").build();
        }
        if (tessdataPath.isBlank()) {
            return Health.down().withDetail("message", "Tessdata path not configured").build();
        }
        if (!Files.exists(Paths.get(tessdataPath))) {
            return Health.down().withDetail("message", "Tessdata not found: " + tessdataPath).build();
        }
        return Health.up().withDetail("tessdata", tessdataPath).build();
    }
}
