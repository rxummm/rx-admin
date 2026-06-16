package com.rx.admin.modules.transcription;

import com.rx.admin.common.constant.TranscriptionConstants;
import com.rx.admin.modules.audio.service.AudioTranscriptionService;
import com.rx.admin.modules.video.service.VideoTranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranscriptionDispatcher {

    private final VideoTranscriptionService videoTranscriptionService;
    private final AudioTranscriptionService audioTranscriptionService;

    public void processVideo(Long id, String model) {
        new Thread(() -> {
            try {
                videoTranscriptionService.transcribeById(id, model);
                log.info("视频转写完成: id={}", id);
            } catch (Exception e) {
                log.error("视频转写失败: id={}", id, e);
                try {
                    videoTranscriptionService.updateStatus(id, TranscriptionConstants.Status.FAIL);
                } catch (Exception ex) {
                    log.error("更新失败状态异常: id={}", id, ex);
                }
            }
        }, "video-transcribe-" + id).start();
    }

    public void processAudio(Long id, String model) {
        new Thread(() -> {
            try {
                audioTranscriptionService.transcribeById(id, model);
                log.info("音频转写完成: id={}", id);
            } catch (Exception e) {
                log.error("音频转写失败: id={}", id, e);
                try {
                    audioTranscriptionService.updateStatus(id, TranscriptionConstants.Status.FAIL);
                } catch (Exception ex) {
                    log.error("更新失败状态异常: id={}", id, ex);
                }
            }
        }, "audio-transcribe-" + id).start();
    }
}
