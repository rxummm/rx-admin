package com.rx.admin.modules.system.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.system.file.entity.SysFile;
import com.rx.admin.modules.system.file.mapper.SysFileMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings("null")
public class SysFileService extends ServiceImpl<SysFileMapper, SysFile> {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public PageResult<SysFile> pageQuery(int page, int size, String category, String keyword) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) {
            wrapper.eq(SysFile::getCategory, category);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysFile::getOriginalName, keyword)
                    .or().like(SysFile::getCategory, keyword);
        }
        wrapper.orderByDesc(SysFile::getCreateTime);

        IPage<SysFile> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), iPage.getRecords());
    }

    /**
     * 上传文件，保存到本地目录并插入数据库记录
     */
    public SysFile upload(MultipartFile file, String category, Long uploader) throws IOException {
        // 按日期生成子目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storedName = UUID.randomUUID().toString().replace("-", "") + getExtension(file.getOriginalFilename());
        String relativePath = dateDir + "/" + storedName;

        // 目标文件
        Path targetDir = Paths.get(uploadDir, dateDir);
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(storedName);
        file.transferTo(targetFile.toFile());

        // 保存数据库记录
        SysFile sysFile = new SysFile();
        sysFile.setOriginalName(file.getOriginalFilename());
        sysFile.setStoredName(storedName);
        sysFile.setPath(relativePath);
        sysFile.setSize(file.getSize());
        sysFile.setMimeType(file.getContentType());
        sysFile.setStorageType("local");
        sysFile.setCategory(category);
        sysFile.setUploader(uploader);
        save(sysFile);

        return sysFile;
    }

    /**
     * 删除文件（物理文件 + 数据库记录）
     */
    public void deleteFile(Long id) {
        SysFile sysFile = getById(id);
        if (sysFile == null) {
            return;
        }
        // 删除物理文件
        Path filePath = Paths.get(uploadDir, sysFile.getPath());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("删除物理文件失败: path={}, error={}", filePath, e.getMessage());
        }
        // 删除数据库记录
        removeById(id);
    }

    private String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
