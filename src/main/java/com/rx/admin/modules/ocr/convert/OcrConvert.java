package com.rx.admin.modules.ocr.convert;

import com.rx.admin.modules.ocr.entity.OcrRecognition;
import com.rx.admin.modules.ocr.vo.OcrRecognitionVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OcrConvert {

    @Mapping(source = "createTime", target = "createTime")
    OcrRecognitionVO toVO(OcrRecognition entity);

    List<OcrRecognitionVO> toVOList(List<OcrRecognition> entities);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void updateEntity(OcrRecognitionVO vo, @MappingTarget OcrRecognition entity);
}
