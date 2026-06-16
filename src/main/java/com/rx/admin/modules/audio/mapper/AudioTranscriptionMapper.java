package com.rx.admin.modules.audio.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.audio.entity.AudioTranscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AudioTranscriptionMapper extends BaseMapper<AudioTranscription> {
    @Update("UPDATE audio_transcription t SET t.duration = (SELECT MAX(s.end_time) FROM audio_segment s WHERE s.transcription_id = t.id) WHERE t.duration IS NULL AND EXISTS (SELECT 1 FROM audio_segment s WHERE s.transcription_id = t.id)")
    void backfillNullDuration();
}
