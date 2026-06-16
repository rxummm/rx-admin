package com.rx.admin.modules.video.player.convert;

import com.rx.admin.modules.video.player.entity.VideoFile;
import com.rx.admin.modules.video.player.entity.VideoPlayRecord;
import com.rx.admin.modules.video.player.vo.VideoFileVO;
import com.rx.admin.modules.video.player.vo.VideoPlayRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoPlayerConvert {
    VideoFileVO toVO(VideoFile entity);
    List<VideoFileVO> toVOList(List<VideoFile> list);
    VideoPlayRecordVO toRecordVO(VideoPlayRecord entity);
    List<VideoPlayRecordVO> toRecordVOList(List<VideoPlayRecord> list);
}
