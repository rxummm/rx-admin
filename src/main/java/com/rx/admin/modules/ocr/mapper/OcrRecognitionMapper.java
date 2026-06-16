package com.rx.admin.modules.ocr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.ocr.entity.OcrRecognition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OcrRecognitionMapper extends BaseMapper<OcrRecognition> {

    @Select("SELECT DISTINCT file_type FROM ocr_recognition WHERE deleted = 0 AND file_type IS NOT NULL ORDER BY file_type")
    List<String> selectDistinctFileTypes();
}
