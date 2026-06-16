# OCR 文档识别模块规划文档

## 一、功能概述

本模块实现对 PDF、Word、图片等文档的 OCR 文字识别功能，支持：
- **PDF 文字提取**：直接提取 PDF 中的文字层（无需 OCR）
- **PDF 图片 OCR**：对 PDF 中的扫描页/图片页进行 OCR 识别
- **Word 文字提取**：提取 Word (.docx) 文档中的文字内容
- **图片 OCR**：对上传的图片文件进行文字识别
- **多语言识别**：支持中文、英文、日文等多种语言
- **识别结果导出**：支持导出为 TXT、纯文本格式
- **识别历史记录**：保存识别记录到数据库，支持查询和管理

## 二、需求分析

### 2.1 核心场景

| 场景 | 输入 | 处理方式 | 输出 |
|------|------|----------|------|
| PDF 文字提取 | 纯文字 PDF | PDFBox 提取文字层 | 纯文本 |
| PDF 扫描件 OCR | 扫描 PDF（图片） | PDFBox 提取图片 → Tesseract OCR | 纯文本 |
| Word 文字提取 | .docx 文件 | Apache POI 提取文字 | 纯文本 |
| 图片 OCR | .png/.jpg/.jpeg/.bmp/.tiff | Tesseract OCR | 纯文本 |
| 混合文档 | 含文字+图片的 PDF | PDFBox 提取文字 + 图片 OCR 合并 | 纯文本 |

### 2.2 支持格式

| 类别 | 格式 | 处理方式 |
|------|------|----------|
| PDF | .pdf | PDFBox（文字层）+ Tesseract（图片层） |
| Word | .docx | Apache POI |
| 图片 | .png/.jpg/.jpeg/.bmp/.tiff/.gif | Tesseract OCR |

### 2.3 技术选型对比

| 方案 | 语言 | 中文支持 | 精度 | 部署难度 | 推荐度 |
|------|------|----------|------|----------|--------|
| **Tesseract OCR (tess4j)** | Java 封装 | ✅ 需训练数据 | 中高 | 低 | ⭐⭐⭐⭐⭐ |
| **PaddleOCR (Python)** | Python | ✅ 原生支持 | 高 | 中 | ⭐⭐⭐⭐ |
| **EasyOCR (Python)** | Python | ✅ 支持 | 高 | 中 | ⭐⭐⭐ |
| **百度/腾讯云 OCR** | REST API | ✅ 优秀 | 极高 | 低 | ⭐⭐⭐⭐（付费） |

**推荐方案**：Tesseract OCR (tess4j)
- 理由：与现有项目 Java 技术栈一致，免费开源，支持中文，部署简单
- 备选：如需更高精度，可后期接入 PaddleOCR Python 服务（类似 WhisperX 模式）

## 三、技术架构

### 3.1 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| OCR 引擎 | Tesseract OCR 5.x | 开源 OCR 引擎 |
| Java 封装 | tess4j 5.x | Tesseract 的 Java JNA 封装 |
| PDF 处理 | Apache PDFBox 3.x | PDF 解析（已有依赖） |
| Word 处理 | Apache POI | Word 文档解析（已有依赖） |
| 图片处理 | Java AWT/ImageIO | 图片预处理（灰度化、二值化） |
| 数据库 | MySQL | 存储识别记录 |

### 3.2 架构图

```
┌─────────────────────────────────────────────────┐
│                  前端 Vue                        │
│  ┌─────────────┐  ┌─────────────┐              │
│  │ 文档OCR识别  │  │ 识别历史记录 │              │
│  │  页面        │  │  页面        │              │
│  └──────┬──────┘  └──────┬──────┘              │
│         │ /api/ocr/      │                      │
└─────────┼────────────────┼──────────────────────┘
          │                │
┌─────────▼────────────────▼──────────────────────┐
│              Spring Boot 后端                    │
│  ┌────────────────────────────────────────┐     │
│  │         OcrController                  │     │
│  │  POST /upload → 识别                    │     │
│  │  GET  /page   → 历史记录                │     │
│  │  GET  /{id}   → 详情                    │     │
│  │  DELETE /{id} → 删除                    │     │
│  └──────────────┬─────────────────────────┘     │
│                 │                               │
│  ┌──────────────▼─────────────────────────┐     │
│  │         OcrService                     │     │
│  │  ┌─────────┐  ┌──────────┐            │     │
│  │  │ PDFBox  │  │  POI     │            │     │
│  │  │ 提取文字 │  │ 提取文字  │            │     │
│  │  └────┬────┘  └────┬─────┘            │     │
│  │       │             │                  │     │
│  │  ┌────▼─────────────▼─────┐           │     │
│  │  │   TesseractService     │           │     │
│  │  │   tess4j 调用 Tesseract│           │     │
│  │  │   OCR 识别             │           │     │
│  │  └────────────────────────┘           │     │
│  └────────────────────────────────────────┘     │
└─────────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────────┐
│           Tesseract OCR 5.x                     │
│  ┌──────────────────────────────────────┐       │
│  │  训练数据 (tessdata)                  │       │
│  │  ├── chi_sim.traineddata  (简体中文)  │       │
│  │  ├── eng.traineddata      (英文)     │       │
│  │  └── jpn.traineddata      (日文)     │       │
│  └──────────────────────────────────────┘       │
└─────────────────────────────────────────────────┘
```

## 四、数据库设计

### 4.1 表结构

#### ocr_recognition（OCR 识别记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键ID |
| file_name | VARCHAR(255) | 原始文件名 |
| file_path | VARCHAR(500) | 文件存储路径 |
| file_type | VARCHAR(20) | 文件类型(pdf/docx/png/jpg等) |
| file_size | BIGINT | 文件大小(字节) |
| language | VARCHAR(20) | 识别语言(chi_sim/eng/jpn) |
| ocr_engine | VARCHAR(50) | OCR引擎(tesseract/pdfbox/poi) |
| result_text | TEXT | 识别结果全文 |
| page_count | INT | 页数/图片数 |
| char_count | INT | 识别字符数 |
| confidence | FLOAT | 平均置信度 |
| status | TINYINT | 状态(0-失败, 1-成功, 2-识别中) |
| error_message | VARCHAR(500) | 错误信息 |
| duration_ms | BIGINT | 识别耗时(毫秒) |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

### 4.2 建表 SQL

```sql
CREATE TABLE ocr_recognition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    file_type VARCHAR(20) COMMENT '文件类型(pdf/docx/png/jpg等)',
    file_size BIGINT COMMENT '文件大小(字节)',
    language VARCHAR(20) DEFAULT 'chi_sim' COMMENT '识别语言',
    ocr_engine VARCHAR(50) DEFAULT 'tesseract' COMMENT 'OCR引擎',
    result_text TEXT COMMENT '识别结果全文',
    page_count INT DEFAULT 1 COMMENT '页数/图片数',
    char_count INT DEFAULT 0 COMMENT '识别字符数',
    confidence FLOAT COMMENT '平均置信度',
    status TINYINT DEFAULT 2 COMMENT '状态(0-失败 1-成功 2-识别中)',
    error_message VARCHAR(500) COMMENT '错误信息',
    duration_ms BIGINT COMMENT '识别耗时(毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_file_name (file_name),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OCR识别记录表';
```

## 五、后端实现规划

### 5.1 文件结构

```
src/main/java/com/rx/admin/modules/ocr/
├── controller/
│   └── OcrController.java              # REST 控制器
├── service/
│   ├── IOcrService.java                # 服务接口
│   └── OcrService.java                 # 服务实现
├── engine/
│   ├── TesseractEngine.java            # Tesseract OCR 引擎封装
│   ├── PdfOcrExtractor.java            # PDF 文字/图片提取
│   └── ImagePreprocessor.java          # 图片预处理（灰度化、二值化）
├── entity/
│   └── OcrRecognition.java             # 识别记录实体
├── dto/
│   └── OcrQueryDTO.java                # 查询 DTO
├── vo/
│   └── OcrRecognitionVO.java           # 返回 VO
└── convert/
    └── OcrConvert.java                 # MapStruct 转换
```

### 5.2 核心类设计

#### TesseractEngine.java — OCR 引擎封装

```java
@Slf4j
@Component
public class TesseractEngine {

    private final AppConfig appConfig;
    private Tesseract tesseract;

    @PostConstruct
    public void init() {
        tesseract = new Tesseract();
        tesseract.setDatapath(appConfig.getOcr().getTessdataPath());
        tesseract.setLanguage(appConfig.getOcr().getDefaultLanguage());
        tesseract.setPageSegMode(3);  // 自动页面分割
        tesseract.setOcrEngineType(1);  // LSTM 引擎
    }

    // 识别单张图片
    public OcrResult recognizeImage(File imageFile) {
        long start = System.currentTimeMillis();
        String text = tesseract.doOCR(imageFile);
        double confidence = tesseract.getMeanConfidence();
        long duration = System.currentTimeMillis() - start;
        return new OcrResult(text, confidence, duration);
    }

    // 识别 PDF（逐页提取图片后 OCR）
    public OcrResult recognizePdf(File pdfFile) {
        // PDFBox 逐页提取 → 图片 → Tesseract 识别 → 合并结果
    }

    // 检测 Tesseract 是否可用
    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("tesseract", "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### PdfOcrExtractor.java — PDF 处理

```java
@Component
public class PdfOcrExtractor {

    private final TesseractEngine tesseractEngine;

    // 提取 PDF 文字（优先文字层，无文字层则 OCR）
    public String extractText(File pdfFile) {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            // 如果提取的文字过少，可能是扫描件，尝试 OCR
            if (text.trim().length() < 50 && doc.getNumberOfPages() > 0) {
                return ocrPdfPages(doc);
            }
            return text;
        }
    }

    // 逐页提取图片并 OCR
    private String ocrPdfPages(PDDocument doc) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            PDPage page = doc.getPage(i);
            BufferedImage image = convertPageToImage(page);
            File tempImage = saveTempImage(image);
            try {
                String pageText = tesseractEngine.recognizeImage(tempImage).getText();
                result.append(pageText).append("\n\n");
            } finally {
                Files.deleteIfExists(tempImage.toPath());
            }
        }
        return result.toString();
    }
}
```

#### OcrService.java — 核心业务

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService implements IOcrService {

    private final OcrRecognitionMapper ocrMapper;
    private final TesseractEngine tesseractEngine;
    private final PdfOcrExtractor pdfExtractor;
    private final OcrConvert ocrConvert;

    // 识别文档
    @Transactional
    public OcrRecognitionVO recognize(File file, String originalName, String language) {
        String fileType = getFileExtension(originalName);
        OcrRecognition record = createRecord(originalName, file, fileType, language);

        try {
            long start = System.currentTimeMillis();
            String text;
            String engine;

            if ("pdf".equals(fileType)) {
                text = pdfExtractor.extractText(file);
                engine = "pdfbox+tesseract";
            } else if ("docx".equals(fileType)) {
                text = extractDocxText(file);
                engine = "poi";
            } else if (isImageFile(fileType)) {
                var result = tesseractEngine.recognizeImage(file);
                text = result.getText();
                engine = "tesseract";
            } else {
                throw new RuntimeException("不支持的文件格式: " + fileType);
            }

            // 更新记录
            record.setResultText(text);
            record.setCharCount(text.length());
            record.setOcrEngine(engine);
            record.setStatus(1); // 成功
            record.setDurationMs(System.currentTimeMillis() - start);
            ocrMapper.updateById(record);

            return ocrConvert.toVO(record);

        } catch (Exception e) {
            record.setStatus(0);
            record.setErrorMessage(e.getMessage());
            ocrMapper.updateById(record);
            throw new RuntimeException("OCR 识别失败: " + e.getMessage());
        }
    }

    // 上传并识别（异步）
    public OcrRecognitionVO uploadAndRecognize(File file, String originalName, String language) {
        // 保存文件 → 写 DB → 异步识别
    }
}
```

### 5.3 配置项

```yaml
app:
  ocr:
    enabled: ${OCR_ENABLED:true}
    tessdata-path: ${OCR_TESSDATA_PATH:D:\tessdata}
    default-language: ${OCR_DEFAULT_LANGUAGE:chi_sim+eng}
    temp-dir: ${OCR_TEMP_DIR:D:\temp\ocr}
    max-file-size-mb: ${OCR_MAX_FILE_SIZE_MB:50}
    supported-formats: pdf,docx,png,jpg,jpeg,bmp,tiff
```

## 六、前端实现规划

### 6.1 页面结构

```
ui/src/views/ocr/
├── recognition/
│   └── index.vue           # OCR 识别主页面
└── history/
    └── index.vue           # 识别历史记录（可选，或合并到主页面）
```

### 6.2 主页面功能布局

```
┌──────────────────────────────────────────────────────────┐
│  OCR 文档识别                                             │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │  文件上传区域                                        │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐           │  │
│  │  │  PDF     │ │  Word    │ │  图片    │           │  │
│  │  │  上传    │ │  上传    │ │  上传    │           │  │
│  │  └──────────┘ └──────────┘ └──────────┘           │  │
│  │  支持拖拽上传，最大 50MB                             │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │  识别设置                                            │  │
│  │  语言: [简体中文 ▼]  引擎: [Tesseract ▼]            │  │
│  │  [开始识别]                                          │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │  识别结果                                            │  │
│  │  文件: test.pdf | 页数: 5 | 字数: 1234 | 耗时: 3.2s│  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  识别出的文字内容...                           │  │  │
│  │  │  ...                                         │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │  [复制文本] [下载TXT] [重新识别]                     │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │  识别历史                                            │  │
│  │  ┌─────┬──────────┬──────┬──────┬──────┬──────┐   │  │
│  │  │ 序号 │ 文件名    │ 语言 │ 字数 │ 耗时 │ 操作 │   │  │
│  │  ├─────┼──────────┼──────┼──────┼──────┼──────┤   │  │
│  │  │ 1   │ test.pdf │ 中文 │ 1234 │ 3.2s │ 详情 │   │  │
│  │  └─────┴──────────┴──────┴──────┴──────┴──────┘   │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

### 6.3 前端组件

| 组件 | 功能 |
|------|------|
| `el-upload` | 文件上传（拖拽 + 点击） |
| `el-select` | 语言选择（中文/英文/日文/自动） |
| `el-button` | 开始识别、复制、下载 |
| `el-table` | 历史记录列表 |
| `el-dialog` | 识别详情弹窗 |
| `el-tag` | 文件类型标签 |
| `el-progress` | 识别进度（可选） |

## 七、API 接口设计

### 7.1 上传并识别

```
POST /api/v1/ocr/recognize
```

**参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | 文件(pdf/docx/png/jpg) |
| language | String | 否 | 识别语言，默认 chi_sim+eng |

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "fileName": "scan.pdf",
    "fileType": "pdf",
    "language": "chi_sim+eng",
    "ocrEngine": "pdfbox+tesseract",
    "resultText": "识别出的文字内容...",
    "pageCount": 5,
    "charCount": 1234,
    "confidence": 85.6,
    "status": 1,
    "durationMs": 3200
  }
}
```

### 7.2 分页查询历史记录

```
GET /api/v1/ocr/page
```

**参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认10 |
| keyword | String | 否 | 文件名搜索 |
| fileType | String | 否 | 文件类型筛选 |
| status | Integer | 否 | 状态筛选 |

### 7.3 获取识别详情

```
GET /api/v1/ocr/{id}
```

### 7.4 下载识别结果

```
GET /api/v1/ocr/{id}/download
```

返回纯文本 (.txt) 文件。

### 7.5 删除识别记录

```
DELETE /api/v1/ocr/{id}
```

### 7.6 批量删除

```
DELETE /api/v1/ocr/batch/{ids}
```

## 八、权限配置

| 权限标识 | 说明 |
|---------|------|
| ocr:recognition:recognize | 执行 OCR 识别 |
| ocr:recognition:list | 查看识别历史 |
| ocr:recognition:view | 查看识别详情 |
| ocr:recognition:delete | 删除识别记录 |

### 菜单注册

```sql
-- OCR 文档识别（挂在系统工具下）
-- parent_id=24 对应"系统工具"目录
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (400, 24, 'OCR文档识别', 2, '/ocr/recognition', 'ocr/recognition/index', 'ocr:recognition:list', 'Document', 52, 1, 1, 0, NOW(), NOW());

-- 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (401, 400, 'OCR识别', 3, '', '', 'ocr:recognition:recognize', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (402, 400, '查看识别详情', 3, '', '', 'ocr:recognition:view', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (403, 400, '删除识别记录', 3, '', '', 'ocr:recognition:delete', '', 3, 1, 1, 0, NOW(), NOW());

-- 分配给超级管理员
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE id BETWEEN 400 AND 403;
```

## 九、Maven 依赖

```xml
<!-- Tesseract OCR Java 封装 -->
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.11.0</version>
</dependency>

<!-- Apache PDFBox（已有） -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>${pdfbox.version}</version>
</dependency>

<!-- Apache POI（已有，用于 Word 解析） -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
</dependency>
```

## 十、Tesseract 安装与配置

### 10.1 安装 Tesseract OCR

#### Windows

```powershell
# 1. 下载安装包
# https://github.com/UB-Mannheim/tesseract/wiki
# 选择 tesseract-ocr-w64-setup-5.x.exe

# 2. 安装到 D:\tesseract-ocr（勾选 Chinese simplified）

# 3. 验证
tesseract --version
# 输出: tesseract v5.x.x

# 4. 查看已安装语言
tesseract --list-langs
# 应包含: chi_sim, eng
```

#### Linux

```bash
# Ubuntu/Debian
sudo apt install tesseract-ocr tesseract-ocr-chi-sim tesseract-ocr-eng

# 验证
tesseract --version
tesseract --list-langs
```

### 10.2 训练数据

Tesseract 需要语言训练数据文件 (.traineddata)：

```
D:\tessdata\
├── chi_sim.traineddata    # 简体中文
├── chi_tra.traineddata    # 繁体中文
├── eng.traineddata        # 英文
└── jpn.traineddata        # 日文
```

下载地址：https://github.com/tesseract-ocr/tessdata

### 10.3 配置环境变量

```powershell
# Windows
[Environment]::SetEnvironmentVariable("TESSDATA_PREFIX", "D:\tessdata", "User")
$env:PATH += ";D:\tesseract-ocr"

# Linux
export TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata
```

## 十一、注意事项

### 11.1 OCR 识别质量优化

| 优化项 | 说明 |
|--------|------|
| 图片预处理 | 灰度化 → 二值化 → 去噪 → 提升识别率 |
| DPI 设置 | 图片 DPI 建议 300+，低 DPI 识别率差 |
| 语言选择 | 纯中文用 chi_sim，中英混排用 chi_sim+eng |
| 页面分割模式 | PSM 3（自动）适合大多数文档，PSM 6（均匀块）适合表格 |

### 11.2 已知限制

1. **Tesseract 对复杂排版识别一般**：多栏、艺术字体识别率低
2. **手写体不支持**：Tesseract 仅支持印刷体
3. **PDF 扫描件需要足够 DPI**：低于 150 DPI 的扫描件识别率差
4. **大文件处理**：超过 50MB 的 PDF 需要分页处理，避免内存溢出

### 11.3 性能估算

| 文件类型 | 大小 | 预计耗时 |
|----------|------|----------|
| 1页 PDF（文字层） | 100KB | < 1秒 |
| 1页 PDF（扫描件） | 1MB | 3-8秒 |
| 10页 PDF（扫描件） | 10MB | 30-80秒 |
| 1张图片 | 2MB | 1-3秒 |
| Word 文档 | 500KB | < 1秒 |

## 十二、与现有模块的关系

| 现有模块 | 关系 | 说明 |
|----------|------|------|
| `tool/commonTools` | 可合并 | OCR 识别可作为"办公工具"的子菜单 |
| `video/transcription` | 模式一致 | 参考其文件上传 + 异步处理 + 结果展示模式 |
| `audio/transcription` | 模式一致 | 参考其数据库设计和 API 设计 |
| `system/file` | 复用 | 文件上传/存储可复用现有工具类 |

## 十三、实施步骤

### Phase 1：基础功能（1-2天）
- [ ] 安装 Tesseract OCR + 训练数据
- [ ] 创建数据库表
- [ ] 实现 TesseractEngine（图片 OCR）
- [ ] 实现 OcrService（基础识别）
- [ ] 实现 OcrController（API 接口）
- [ ] 前端基础页面（上传 + 结果展示）

### Phase 2：PDF/Word 支持（1天）
- [ ] 实现 PdfOcrExtractor（PDF 文字提取 + 图片 OCR）
- [ ] 实现 Word 文字提取
- [ ] 前端文件类型适配

### Phase 3：优化与完善（1天）
- [ ] 图片预处理（灰度化、二值化）
- [ ] 识别历史记录页面
- [ ] 识别结果导出 TXT
- [ ] 菜单注册 + 权限配置

## 十四、快速启动检查清单

### 首次部署

- [ ] 安装 Tesseract OCR 5.x
- [ ] 下载语言训练数据到 tessdata 目录
- [ ] 配置 TESSDATA_PREFIX 环境变量
- [ ] 添加 tess4j 依赖到 pom.xml
- [ ] 执行建表 SQL
- [ ] 配置 application.yml（ocr 段）
- [ ] 启动后端验证

### 日常使用

```powershell
# 1. 启动后端
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 2. 启动前端
cd ui; npm run dev

# 3. 访问 OCR 识别页面
# http://localhost:3000/#/ocr/recognition
```
