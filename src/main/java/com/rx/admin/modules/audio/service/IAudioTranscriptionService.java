package com.rx.admin.modules.audio.service;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.audio.dto.AudioTranscriptionQueryDTO;
import com.rx.admin.modules.audio.entity.AudioTranscription;
import com.rx.admin.modules.audio.vo.AudioTranscriptionVO;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface IAudioTranscriptionService {
    Path getTempDir();
    AudioTranscriptionVO transcribe(File audioFile, String originalName, String language);
    AudioTranscriptionVO uploadOnly(File audioFile, String originalName, String language);
    AudioTranscriptionVO transcribeById(Long id, String model);
    AudioTranscriptionVO getById(Long id);
    PageResult<AudioTranscription> pageQuery(AudioTranscriptionQueryDTO query);
    PageResult<AudioTranscriptionVO> pageQueryVO(AudioTranscriptionQueryDTO query);
    void deleteById(Long id);
    void deleteBatch(List<Long> ids);
    AudioTranscriptionVO updateFileName(Long id, String fileName);
    AudioTranscriptionVO convertScript(Long id, String target);
    void updateStatus(Long id, Integer status);
    String generateSrtContent(Long id);
    String generateAssContent(Long id);
}
