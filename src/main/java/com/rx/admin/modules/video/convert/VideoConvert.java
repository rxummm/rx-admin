package com.rx.admin.modules.video.convert;

import com.rx.admin.modules.video.entity.VideoSegment;
import com.rx.admin.modules.video.entity.VideoTranscription;
import com.rx.admin.modules.video.vo.VideoSegmentVO;
import com.rx.admin.modules.video.vo.VideoTranscriptionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoConvert {
    @Mapping(source = "createTime", target = "createdAt")
    VideoTranscriptionVO toVO(VideoTranscription entity);
    VideoSegmentVO toSegmentVO(VideoSegment entity);
    List<VideoSegmentVO> toSegmentVOList(List<VideoSegment> list);
}