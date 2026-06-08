package com.rx.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.entity.SharedFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CommonToolsService {

    /**
     * 解析Excel文件，返回数据列表（第一行为表头）
     */
    List<Map<String, Object>> parseExcel(MultipartFile file) throws Exception;

    /**
     * 上传文档文件
     */
    SharedFile uploadDocument(MultipartFile file, String targetDir) throws Exception;

    /**
     * 分页查询已上传文件
     */
    Page<SharedFile> getUploadedFiles(int page, int size, String keyword);

    /**
     * 删除文件
     */
    boolean deleteFile(Long id);

    /**
     * 获取默认上传目录
     */
    String getDefaultUploadDir();

    /**
     * PDF转Word
     */
    String convertPdfToWord(MultipartFile file, String outputDir) throws Exception;

    /**
     * Word转PDF
     */
    String convertWordToPdf(MultipartFile file, String outputDir) throws Exception;
}
