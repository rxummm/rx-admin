package com.rx.admin.modules.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.video.entity.VideoTranscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VideoTranscriptionMapper extends BaseMapper<VideoTranscription> {
    @Update("UPDATE video_transcription t SET t.duration = (SELECT MAX(s.end_time) FROM video_segment s WHERE s.transcription_id = t.id) WHERE t.duration IS NULL AND EXISTS (SELECT 1 FROM video_segment s WHERE s.transcription_id = t.id)")
    void backfillNullDuration();
}