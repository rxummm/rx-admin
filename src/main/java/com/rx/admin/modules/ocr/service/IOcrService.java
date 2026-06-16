package com.rx.admin.modules.ocr.service;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.ocr.dto.OcrQueryDTO;
import com.rx.admin.modules.ocr.vo.OcrRecognitionVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface IOcrService {

    OcrRecognitionVO recognize(File file, String originalName, String language);

    OcrRecognitionVO uploadAndRecognize(MultipartFile multipartFile, String language);

    OcrRecognitionVO getById(Long id);

    PageResult<OcrRecognitionVO> pageQuery(OcrQueryDTO query);

    void deleteById(Long id);

    void deleteBatch(List<Long> ids);

    String generateResultTxt(Long id);
}
