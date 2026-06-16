package com.rx.admin.modules.video.service;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.video.dto.VideoTranscriptionQueryDTO;
import com.rx.admin.modules.video.entity.VideoTranscription;
import com.rx.admin.modules.video.vo.VideoTranscriptionVO;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface IVideoTranscriptionService {
    Path getTempDir();
    VideoTranscriptionVO transcribe(File videoFile, String originalName, String language);
    VideoTranscriptionVO uploadOnly(File videoFile, String originalName, String language);
    VideoTranscriptionVO transcribeById(Long id, String model);
    VideoTranscriptionVO getById(Long id);
    PageResult<VideoTranscription> pageQuery(VideoTranscriptionQueryDTO query);
    PageResult<VideoTranscriptionVO> pageQueryVO(VideoTranscriptionQueryDTO query);
    void deleteById(Long id);
    void deleteBatch(List<Long> ids);
    VideoTranscriptionVO updateFileName(Long id, String fileName);
    VideoTranscriptionVO updateSpeakerName(Long id, String speakerLabel, String speakerName);
    void updateStatus(Long id, Integer status);
    String generateSrtContent(Long id);
    String generateAssContent(Long id);
    String generateDialogueByRole(Long id);
}