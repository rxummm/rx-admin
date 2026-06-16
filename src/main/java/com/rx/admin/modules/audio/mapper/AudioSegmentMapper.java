package com.rx.admin.modules.audio.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.audio.entity.AudioSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AudioSegmentMapper extends BaseMapper<AudioSegment> {
    @Select("SELECT * FROM audio_segment WHERE transcription_id = #{transcriptionId} ORDER BY start_time")
    List<AudioSegment> selectByTranscriptionId(Long transcriptionId);
}
