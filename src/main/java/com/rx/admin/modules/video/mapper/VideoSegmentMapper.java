package com.rx.admin.modules.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.video.entity.VideoSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoSegmentMapper extends BaseMapper<VideoSegment> {
    @Select("SELECT * FROM video_segment WHERE transcription_id = #{transcriptionId} ORDER BY start_time")
    List<VideoSegment> selectByTranscriptionId(Long transcriptionId);

    @Select("SELECT DISTINCT speaker_label FROM video_segment WHERE transcription_id = #{transcriptionId} ORDER BY speaker_label")
    List<String> selectDistinctSpeakerLabels(Long transcriptionId);
}