package com.rx.admin.common.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class AudioHealthIndicator implements HealthIndicator {

    @Value("${app.audio.enabled:false}")
    private boolean enabled;

    @Value("${app.audio.whisper-path:}")
    private String whisperPath;

    @Override
    public Health health() {
        if (!enabled) {
            return Health.unknown().withDetail("message", "Audio transcription disabled").build();
        }
        if (whisperPath.isBlank()) {
            return Health.down().withDetail("message", "Whisper path not configured").build();
        }
        if (!Files.exists(Paths.get(whisperPath))) {
            return Health.down().withDetail("message", "Whisper binary not found: " + whisperPath).build();
        }
        return Health.up().withDetail("whisper", whisperPath).build();
    }
}
