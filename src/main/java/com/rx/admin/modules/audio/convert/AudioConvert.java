package com.rx.admin.modules.audio.convert;

import com.rx.admin.modules.audio.entity.AudioSegment;
import com.rx.admin.modules.audio.entity.AudioTranscription;
import com.rx.admin.modules.audio.vo.AudioSegmentVO;
import com.rx.admin.modules.audio.vo.AudioTranscriptionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AudioConvert {
    @Mapping(source = "createTime", target = "createdAt")
    AudioTranscriptionVO toVO(AudioTranscription entity);
    AudioSegmentVO toSegmentVO(AudioSegment entity);
    List<AudioSegmentVO> toSegmentVOList(List<AudioSegment> list);
}
