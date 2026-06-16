package com.rx.admin.modules.transcription;

import com.rx.admin.common.config.AppConfig;
import com.ibm.icu.text.Transliterator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WhisperEngine {

    private final AppConfig appConfig;

    private static final Pattern SEGMENT_PATTERN = Pattern.compile(
        "\\[(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}) --> (\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\]\\s*(.+)", Pattern.MULTILINE
    );

    @Data
    public static class WhisperResult {
        private String fullText;
        private List<WhisperSegment> segments;
        private boolean usedFallback;
    }

    @Data
    public static class WhisperSegment {
        private double startTime;
        private double endTime;
        private String text;
        private String speaker;  // 说话人标签（SPEAKER_00 等，来自 WhisperX）
    }

    public WhisperResult transcribeWav(File wavFile, String language, String modelName) {
        WhisperResult result = new WhisperResult();
        if (isToolAvailable()) {
            try {
                String output = runWhisper(wavFile, language, modelName);
                result.setFullText(parseFullText(output));
                result.setSegments(parseSegments(output));
                result.setUsedFallback(false);
            } catch (Exception e) {
                log.warn("Whisper 转写失败，回退到演示模式: {}", e.getMessage());
                fallback(result, language);
            }
        } else {
            log.info("Whisper 未配置，使用演示模式");
            fallback(result, language);
        }
        return result;
    }

    public WhisperResult transcribeAudio(File audioFile, String language, String modelName) throws IOException, InterruptedException {
        File wavFile = convertToWav(audioFile);
        try {
            return transcribeWav(wavFile, language, modelName);
        } finally {
            Files.deleteIfExists(wavFile.toPath());
        }
    }

    public double getMediaDuration(File mediaFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "error", "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                mediaFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                int exitCode = process.waitFor();
                if (exitCode == 0 && line != null && !line.isEmpty()) {
                    return Double.parseDouble(line.trim());
                }
            }
        } catch (Exception e) {
            log.debug("FFprobe 获取时长失败: {}", e.getMessage());
        }
        return 0;
    }

    public File extractAudioFromVideo(File videoFile) throws IOException, InterruptedException {
        Path tempDir = resolveTempDir("video");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        String outputPath = tempDir.resolve(System.currentTimeMillis() + "_audio.wav").toString();
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg", "-i", videoFile.getAbsolutePath(),
            "-af", "aresample=async=1",
            "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le",
            "-y", "-vn", outputPath
        );
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 音频提取失败");
        }
        return new File(outputPath);
    }

    public Path resolveTempDir(String subDir) {
        String configuredDir = appConfig.getAudio() != null ? appConfig.getAudio().getTempDir() : null;
        if (configuredDir != null && !configuredDir.isEmpty()) {
            if (configuredDir.startsWith("/tmp/")) {
                return Paths.get(System.getProperty("java.io.tmpdir"), "rx-" + subDir + "-upload");
            }
            return Paths.get(configuredDir);
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "rx-" + subDir + "-upload");
    }

    public Path resolveStorageDir(String subDir) {
        String configuredDir = appConfig.getAudio() != null ? appConfig.getAudio().getTempDir() : null;
        if (configuredDir != null && !configuredDir.isEmpty() && !configuredDir.startsWith("/tmp/")) {
            return Paths.get(configuredDir, "storage");
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "rx-" + subDir + "-storage");
    }

    public String sanitizeFileName(String fileName, String defaultName) {
        if (fileName == null) return defaultName;
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public WhisperResult fallbackResult(String language) {
        WhisperResult result = new WhisperResult();
        fallback(result, language);
        return result;
    }

    public boolean isToolAvailable() {
        if (appConfig.getAudio() == null) return false;
        String whisperPath = appConfig.getAudio().getWhisperPath();
        if (whisperPath == null || whisperPath.isEmpty()) return false;

        boolean whisperOk = false;
        try {
            ProcessBuilder pb = new ProcessBuilder(whisperPath, "--help");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            whisperOk = (exitCode == 0 || exitCode == 1);
        } catch (IOException | InterruptedException e) {
            log.debug("Whisper 命令不可用: {}", e.getMessage());
        }
        if (!whisperOk) return false;

        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
            return true;
        } catch (IOException | InterruptedException e) {
            log.debug("ffmpeg 不可用: {}", e.getMessage());
        }
        return false;
    }

    public String toSimplified(String text) {
        if (text == null) return null;
        return Transliterator.getInstance("Traditional-Simplified").transliterate(text);
    }

    public String toTraditional(String text) {
        if (text == null) return null;
        return Transliterator.getInstance("Simplified-Traditional").transliterate(text);
    }

    public boolean isWhisperXAvailable() {
        if (appConfig.getAudio() == null || !appConfig.getAudio().isWhisperxEnabled()) {
            return false;
        }
        String apiUrl = appConfig.getAudio().getWhisperxApiUrl();
        if (apiUrl == null || apiUrl.isEmpty()) return false;
        try {
            RestTemplate rt = createRestTemplate();
            ResponseEntity<Map<String, Object>> resp = rt.exchange(
                apiUrl + "/health", HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("WhisperX 服务不可用: {}", e.getMessage());
            return false;
        }
    }

    public WhisperResult transcribeWithWhisperX(File wavFile, String language) {
        String apiUrl = appConfig.getAudio().getWhisperxApiUrl();
        String apiKey = appConfig.getAudio().getWhisperxApiKey();
        log.info("WhisperX 转写: url={}, file={}, language={}", apiUrl, wavFile.getName(), language);

        RestTemplate rt = createRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("Authorization", apiKey);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(wavFile));
        body.add("language_code", language);
        body.add("speaker_labels", "true");

        ResponseEntity<Map<String, Object>> response = rt.exchange(
            apiUrl + "/v1/transcript",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> respBody = response.getBody();
        if (respBody == null) {
            throw new RuntimeException("WhisperX 返回空响应");
        }

        String transcriptId = (String) respBody.get("id");
        if (transcriptId == null) {
            throw new RuntimeException("WhisperX 未返回 transcript id");
        }

        log.info("WhisperX 任务已提交: id={}", transcriptId);

        for (int i = 0; i < 600; i++) {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); break; }

            ResponseEntity<Map<String, Object>> pollResp = rt.exchange(
                apiUrl + "/v1/transcript/" + transcriptId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> pollBody = pollResp.getBody();
            if (pollBody == null) continue;

            String status = (String) pollBody.get("status");
            if ("completed".equals(status)) {
                log.info("WhisperX 转写完成: id={}", transcriptId);
                return buildWhisperResultFromResponse(pollBody);
            } else if ("error".equals(status)) {
                throw new RuntimeException("WhisperX 转写失败: " + pollBody.get("error"));
            }
        }

        throw new RuntimeException("WhisperX 转写超时（20分钟）");
    }

    private WhisperResult buildWhisperResultFromResponse(Map<?, ?> pollBody) {
        WhisperResult result = new WhisperResult();
        result.setUsedFallback(false);
        result.setFullText(pollBody.get("text") != null ? pollBody.get("text").toString() : "");

        List<WhisperSegment> segments = new ArrayList<>();
        Object utterancesObj = pollBody.get("utterances");
        if (utterancesObj instanceof List<?> utterances) {
            for (Object u : utterances) {
                if (u instanceof Map<?, ?> utt) {
                    WhisperSegment seg = new WhisperSegment();
                    seg.startTime = toDouble(utt.get("start"));
                    seg.endTime = toDouble(utt.get("end"));
                    seg.text = utt.get("text") != null ? utt.get("text").toString().trim() : "";
                    Object speaker = utt.get("speaker");
                    if (speaker != null) {
                        seg.speaker = speaker.toString();
                    }
                    segments.add(seg);
                }
            }
        }
        result.setSegments(segments);
        return result;
    }

    private double toDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        if (val != null) {
            try { return Double.parseDouble(val.toString()); } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(600_000);  // 10 分钟，大文件转写耗时长
        return new RestTemplate(factory);
    }

    private File convertToWav(File inputFile) throws IOException, InterruptedException {
        Path tempDir = resolveTempDir("audio");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        String outputPath = tempDir.resolve(System.currentTimeMillis() + ".wav").toString();
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg", "-i", inputFile.getAbsolutePath(),
            "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le",
            "-y", outputPath
        );
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 转换失败");
        }
        return new File(outputPath);
    }

    private String runWhisper(File wavFile, String language, String modelName) throws IOException, InterruptedException {
        if (!modelName.startsWith("ggml-")) {
            modelName = "ggml-" + modelName;
        }
        String modelPath = appConfig.getAudio().getModelPath()
            + File.separator + modelName + ".bin";

        log.info("Whisper 转写配置: threads={}, model={}, language={}",
            appConfig.getAudio().getThreads(), modelName, language);

        ProcessBuilder pb = new ProcessBuilder(
            appConfig.getAudio().getWhisperPath(),
            "-m", modelPath,
            "-f", wavFile.getAbsolutePath(),
            "-l", language,
            "-t", String.valueOf(appConfig.getAudio().getThreads()),
            "-ng"
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean exited = process.waitFor(30, java.util.concurrent.TimeUnit.MINUTES);
        if (!exited) {
            process.destroyForcibly();
            throw new RuntimeException("Whisper 转写超时（30分钟）");
        }

        int exitCode = process.exitValue();

        if (exitCode == 0 && output.length() > 0) {
            return output.toString();
        }

        throw new RuntimeException("Whisper 转写失败 (exit=" + exitCode + "): " + output);
    }

    private void fallback(WhisperResult result, String language) {
        String mock = generateMockOutput(language);
        result.setFullText(parseFullText(mock));
        result.setSegments(parseSegments(mock));
        result.setUsedFallback(true);
    }

    private String generateMockOutput(String language) {
        StringBuilder sb = new StringBuilder();
        double currentTime = 0.0;

        String[] zhSegments = {
            "你好，我是角色A。",
            "你好角色A，我是角色B。",
            "很高兴见到你。",
            "我也是，今天天气不错。",
            "是的，适合外出活动。"
        };

        String[] enSegments = {
            "Hello, I am Speaker A.",
            "Hello Speaker A, I am Speaker B.",
            "Nice to meet you.",
            "Same here, the weather is nice today.",
            "Yes, perfect for outdoor activities."
        };

        String[] segments = "zh".equalsIgnoreCase(language) ? zhSegments : enSegments;

        for (String segment : segments) {
            double duration = ThreadLocalRandom.current().nextDouble(2.5, 5.5);
            double startTime = currentTime;
            double endTime = currentTime + duration;
            currentTime = endTime;
            sb.append(String.format("[%s --> %s]  %s\n", toHms(startTime), toHms(endTime), segment));
        }
        return sb.toString();
    }

    private String parseFullText(String output) {
        StringBuilder fullText = new StringBuilder();
        Matcher matcher = SEGMENT_PATTERN.matcher(output);
        while (matcher.find()) {
            String text = matcher.group(3).trim();
            text = filterSpeakerLabel(text);
            fullText.append(text).append("\n");
        }
        if (fullText.length() == 0) {
            fullText.append(output);
        }
        return fullText.toString().trim();
    }

    private List<WhisperSegment> parseSegments(String output) {
        List<WhisperSegment> segments = new ArrayList<>();
        Matcher matcher = SEGMENT_PATTERN.matcher(output);
        while (matcher.find()) {
            WhisperSegment seg = new WhisperSegment();
            seg.setStartTime(toSeconds(matcher.group(1)));
            seg.setEndTime(toSeconds(matcher.group(2)));
            String text = matcher.group(3).trim();
            text = filterSpeakerLabel(text);
            seg.setText(text);
            segments.add(seg);
        }
        return segments;
    }

    private static double toSeconds(String hms) {
        String[] parts = hms.split(":");
        double seconds = 0;
        if (parts.length == 3) {
            seconds += Integer.parseInt(parts[0]) * 3600;
            seconds += Integer.parseInt(parts[1]) * 60;
            seconds += Double.parseDouble(parts[2]);
        }
        return seconds;
    }

    private static String toHms(double seconds) {
        int h = (int) (seconds / 3600);
        int m = (int) ((seconds % 3600) / 60);
        double s = seconds % 60;
        return String.format("%02d:%02d:%06.3f", h, m, s);
    }

    private String filterSpeakerLabel(String text) {
        if (text == null) return null;
        return text.replaceAll("^\\(字幕:[^)]+\\)\\s*", "")
                   .replaceAll("\\(字幕:[^)]+\\)", "");
    }
}