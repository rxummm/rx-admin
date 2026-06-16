# OCR 文档识别 — 部署与使用指南

## 一、功能概述

基于 Tesseract OCR (tess4j) 的文档文字识别模块，支持：
- **PDF**：文字层直接提取 + 扫描页 OCR
- **Word (.docx)**：Apache POI 提取文字
- **图片**：Tesseract OCR 识别（png/jpg/jpeg/bmp/tiff/gif）
- **多语言**：中文（chi_sim）、英文（eng），可扩展

---

## 二、环境准备

### 2.1 Tesseract OCR 安装

#### 下载地址

https://github.com/UB-Mannheim/tesseract/wiki

选择 `tesseract-ocr-w64-setup-5.x.exe`（64位版本）

#### 安装步骤

1. 运行安装程序
2. **安装路径**选择 `D:\tesseract-ocr`
3. **组件选择**：勾选 `Chinese simplified`（简体中文）— 实际需手动下载训练数据
4. 完成安装

#### 配置环境变量

```powershell
# 将 Tesseract 添加到 PATH（当前会话）
$env:PATH += ";D:\tesseract-ocr"

# 永久添加（需管理员权限）
[Environment]::SetEnvironmentVariable("PATH", $env:PATH + ";D:\tesseract-ocr", "User")

# 设置 TESSDATA_PREFIX（tess4j 需要）
[Environment]::SetEnvironmentVariable("TESSDATA_PREFIX", "D:\tesseract-ocr\tessdata", "User")
$env:TESSDATA_PREFIX = "D:\tesseract-ocr\tessdata"
```

#### 验证安装

```powershell
& "D:\tesseract-ocr\tesseract.exe" --version
# 输出: tesseract v5.5.0.20241111

& "D:\tesseract-ocr\tesseract.exe" --list-langs --tessdata-dir "D:\tesseract-ocr\tessdata"
# 输出:
# List of available languages in "D:\tesseract-ocr\tessdata/" (2):
# chi_sim
# eng
```

### 2.2 语言训练数据下载

安装后 tessdata 目录为空，需手动下载训练数据文件：

| 语言 | 文件名 | 下载地址 | 大小 |
|------|--------|----------|------|
| 简体中文 | `chi_sim.traineddata` | https://github.com/tesseract-ocr/tessdata/raw/main/chi_sim.traineddata | ~44MB |
| 英文 | `eng.traineddata` | https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata | ~23MB |

#### 下载命令（PowerShell）

```powershell
# 下载中文训练数据
Invoke-WebRequest -Uri "https://github.com/tesseract-ocr/tessdata/raw/main/chi_sim.traineddata" -OutFile "D:\tesseract-ocr\tessdata\chi_sim.traineddata"

# 下载英文训练数据
Invoke-WebRequest -Uri "https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata" -OutFile "D:\tesseract-ocr\tessdata\eng.traineddata"
```

#### 验证训练数据

```powershell
Get-ChildItem "D:\tesseract-ocr\tessdata" -Filter "*.traineddata" | Select-Object Name, Length
# 输出:
# Name                  Length
# ----                  ------
# chi_sim.traineddata  44366093
# eng.traineddata      23466654

& "D:\tesseract-ocr\tesseract.exe" --list-langs --tessdata-dir "D:\tesseract-ocr\tessdata"
# 输出:
# List of available languages in "D:\tesseract-ocr\tessdata/" (2):
# chi_sim
# eng
```

---

## 三、数据库初始化

### 3.1 执行 SQL 脚本

```powershell
mysql -u root -proot rx_admin < D:\vueprojects\RX\db\ocr_recognition.sql
```

### 3.2 SQL 脚本内容（`db/ocr_recognition.sql`）

```sql
-- ============================================
-- OCR 文档识别 - 建表 SQL
-- ============================================

-- ============ 先删除旧数据 ============
DROP TABLE IF EXISTS ocr_recognition;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 600 AND 603;
DELETE FROM sys_menu WHERE id BETWEEN 600 AND 603;

-- ============ 创建 OCR 识别记录表 ============
CREATE TABLE ocr_recognition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    file_type VARCHAR(20) COMMENT '文件类型(pdf/docx/png/jpg等)',
    file_size BIGINT COMMENT '文件大小(字节)',
    language VARCHAR(20) DEFAULT 'chi_sim+eng' COMMENT '识别语言',
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

-- ============ 注册菜单（parent_id=24 为系统工具） ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (600, 24, 'OCR文档识别', 2, '/ocr/recognition', 'ocr/recognition/index', 'ocr:recognition:list', 'Document', 52, 1, 1, 0, NOW(), NOW());

-- ============ 按钮级权限 ============
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (601, 600, 'OCR识别', 3, '', '', 'ocr:recognition:recognize', '', 1, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (602, 600, '查看识别详情', 3, '', '', 'ocr:recognition:view', '', 2, 1, 1, 0, NOW(), NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (603, 600, '删除识别记录', 3, '', '', 'ocr:recognition:delete', '', 3, 1, 1, 0, NOW(), NOW());

-- ============ 分配给超级管理员角色（role_id=1） ============
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE id BETWEEN 600 AND 603;
```

### 3.3 验证数据库

```powershell
# 检查表是否创建成功
mysql -u root -proot -e "DESCRIBE ocr_recognition" rx_admin

# 检查菜单是否注册
mysql -u root -proot -e "SELECT id, menu_name, perms FROM sys_menu WHERE id BETWEEN 600 AND 603" rx_admin

# 检查权限分配
mysql -u root -proot -e "SELECT rm.role_id, m.perms FROM sys_role_menu rm JOIN sys_menu m ON rm.menu_id=m.id WHERE rm.menu_id BETWEEN 600 AND 603" rx_admin
```

---

## 四、后端配置

### 4.1 Maven 依赖（pom.xml）

```xml
<!-- Tesseract OCR Java 封装 -->
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.11.0</version>
</dependency>
```

### 4.2 application.yml 配置

```yaml
app:
  # OCR 文档识别配置
  ocr:
    enabled: ${OCR_ENABLED:true}
    tessdata-path: ${OCR_TESSDATA_PATH:D:\tesseract-ocr\tessdata}
    default-language: ${OCR_DEFAULT_LANGUAGE:chi_sim+eng}
    temp-dir: ${OCR_TEMP_DIR:D:\temp\ocr}
    max-file-size-mb: ${OCR_MAX_FILE_SIZE_MB:50}
```

### 4.3 配置项说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `app.ocr.enabled` | `true` | 是否启用 OCR 功能 |
| `app.ocr.tessdata-path` | `D:\tesseract-ocr\tessdata` | Tesseract 训练数据目录 |
| `app.ocr.default-language` | `chi_sim+eng` | 默认识别语言 |
| `app.ocr.temp-dir` | `D:\temp\ocr` | 临时文件目录 |
| `app.ocr.max-file-size-mb` | `50` | 最大上传文件大小(MB) |

### 4.4 创建临时目录

```powershell
New-Item -ItemType Directory -Path "D:\temp\ocr" -Force
```

---

## 五、后端文件结构

```
src/main/java/com/rx/admin/modules/ocr/
├── controller/
│   └── OcrController.java              # REST 控制器
├── service/
│   ├── IOcrService.java                # 服务接口
│   └── OcrService.java                 # 服务实现
├── engine/
│   ├── TesseractEngine.java            # Tesseract OCR 引擎封装
│   └── PdfOcrExtractor.java            # PDF 文字/图片提取
├── entity/
│   └── OcrRecognition.java             # 识别记录实体
├── dto/
│   └── OcrQueryDTO.java                # 查询 DTO
├── vo/
│   └── OcrRecognitionVO.java           # 返回 VO
└── convert/
    └── OcrConvert.java                 # MapStruct 转换
```

---

## 六、前端文件结构

```
ui/src/
├── api/
│   ├── ocr.js                          # OCR API 调用
│   └── routes.js                       # API 路由定义（OCR 部分）
├── views/ocr/recognition/
│   └── index.vue                       # OCR 识别页面
├── router/
│   └── componentMap.js                 # 组件注册（ocr/recognition/index）
└── i18n/lang/
    ├── zh-CN.js                        # 中文翻译
    └── en-US.js                        # 英文翻译
```

---

## 七、API 接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/v1/ocr/recognize` | 上传文件并 OCR 识别 | `ocr:recognition:recognize` |
| GET | `/api/v1/ocr/page` | 分页查询识别记录 | `ocr:recognition:list` |
| GET | `/api/v1/ocr/{id}` | 获取识别详情 | `ocr:recognition:view` |
| GET | `/api/v1/ocr/{id}/download` | 下载识别结果 TXT | `ocr:recognition:view` |
| DELETE | `/api/v1/ocr/{id}` | 删除识别记录 | `ocr:recognition:delete` |
| DELETE | `/api/v1/ocr/batch/{ids}` | 批量删除 | `ocr:recognition:delete` |

---

## 八、启动与停止

### 8.1 首次部署（完整流程）

```powershell
# ===== 步骤 1：安装 Tesseract =====
# 下载安装 https://github.com/UB-Mannheim/tesseract/wiki
# 安装到 D:\tesseract-ocr

# ===== 步骤 2：下载训练数据 =====
Invoke-WebRequest -Uri "https://github.com/tesseract-ocr/tessdata/raw/main/chi_sim.traineddata" -OutFile "D:\tesseract-ocr\tessdata\chi_sim.traineddata"
Invoke-WebRequest -Uri "https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata" -OutFile "D:\tesseract-ocr\tessdata\eng.traineddata"

# ===== 步骤 3：初始化数据库 =====
mysql -u root -proot rx_admin < D:\vueprojects\RX\db\ocr_recognition.sql

# ===== 步骤 4：创建临时目录 =====
New-Item -ItemType Directory -Path "D:\temp\ocr" -Force

# ===== 步骤 5：验证 Tesseract =====
& "D:\tesseract-ocr\tesseract.exe" --version
& "D:\tesseract-ocr\tesseract.exe" --list-langs --tessdata-dir "D:\tesseract-ocr\tessdata"
```

### 8.2 启动后端

```powershell
cd D:\vueprojects\RX

# 方式一：Maven 直接启动（推荐开发环境）
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 方式二：编译后启动
mvn clean package -DskipTests -q
java -jar target/rx-admin.jar --spring.profiles.active=local
```

后端启动成功标志：控制台出现 `Started RxAdminApplication in X seconds`

访问 Knife4j 文档：http://localhost:8088/doc.html

### 8.3 启动前端

```powershell
cd D:\vueprojects\RX\ui

# 安装依赖（首次）
npm install

# 启动开发服务器
npm run dev
```

前端启动成功标志：控制台出现 `Local: http://localhost:3000/`

### 8.4 停止进程

```powershell
# 停止后端（Java 进程）
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force

# 停止前端（Node 进程）
Get-Process -Name node -ErrorAction SilentlyContinue | Stop-Process -Force

# 或者在终端窗口按 Ctrl+C 停止当前进程
```

---

## 九、功能验证

### 9.1 后端 API 测试

```powershell
# 登录获取 token
$captcha = Invoke-RestMethod -Uri "http://localhost:8088/api/v1/auth/captcha" -Method GET
$uuid = $captcha.data.uuid
$body = '{"username":"admin","password":"admin123","captchaCode":"dev000","captchaUuid":"' + $uuid + '"}'
$login = Invoke-RestMethod -Uri "http://localhost:8088/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json"
$token = $login.data.token
Write-Host "Token: $token"

# 测试 OCR 识别（需先创建测试图片）
# 创建测试图片
Add-Type -AssemblyName System.Drawing
$bmp = New-Object System.Drawing.Bitmap(300, 80)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::White)
$font = New-Object System.Drawing.Font("Arial", 20)
$g.DrawString("Hello OCR Test 12345", $font, [System.Drawing.Brushes]::Black, 10, 20)
$bmp.Save("D:\temp\ocr\test.png")
$g.Dispose()
$bmp.Dispose()

# 上传识别
$boundary = [System.Guid]::NewGuid().ToString("N").Substring(0,16)
$fileBytes = [System.IO.File]::ReadAllBytes("D:\temp\ocr\test.png")
$sb = New-Object System.Text.StringBuilder
$sb.Append("--$boundary`r`n")
$sb.Append("Content-Disposition: form-data; name=`"file`"; filename=`"test.png`"`r`n")
$sb.Append("Content-Type: image/png`r`n`r`n")
$prefix = [System.Text.Encoding]::UTF8.GetBytes($sb.ToString())
$suffix = [System.Text.Encoding]::UTF8.GetBytes("`r`n--$boundary--`r`n")
$ms = New-Object System.IO.MemoryStream
$ms.Write($prefix, 0, $prefix.Length)
$ms.Write($fileBytes, 0, $fileBytes.Length)
$ms.Write($suffix, 0, $suffix.Length)
$ms.Position = 0
$content = New-Object System.Net.Http.StreamContent($ms)
$content.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("multipart/form-data; boundary=$boundary")
$handler = [System.Net.Http.HttpClientHandler]::new()
$client = [System.Net.Http.HttpClient]::new($handler)
$client.DefaultRequestHeaders.Add("Authorization", $token)
$resp = $client.PostAsync("http://localhost:8088/api/v1/ocr/recognize", $content).Result
Write-Host "Status: $($resp.StatusCode)"
$resp.Content.ReadAsStringAsync().Result
# 期望输出: {"code":200,"data":{"id":...,"resultText":"Hello OCR Test 12345",...}}
```

### 9.2 前端页面验证

1. 打开 http://localhost:3000
2. 登录（admin / admin123）
3. 左侧菜单 → 系统工具 → OCR文档识别
4. 上传图片进行识别测试

---

## 十、相关文件清单

| 文件 | 说明 |
|------|------|
| `db/ocr_recognition.sql` | 建表 + 菜单 + 权限 SQL |
| `pom.xml` | tess4j 依赖 |
| `application.yml` | OCR 配置（app.ocr 段） |
| `AppConfig.java` | OcrConfig 内部类 |
| `TesseractEngine.java` | OCR 引擎封装 |
| `PdfOcrExtractor.java` | PDF 提取器 |
| `OcrService.java` | 核心业务逻辑 |
| `OcrController.java` | REST 接口 |
| `OcrRecognition.java` | 实体类 |
| `OcrRecognitionVO.java` | 返回 VO |
| `OcrQueryDTO.java` | 查询 DTO |
| `OcrConvert.java` | MapStruct 转换 |
| `OcrRecognitionMapper.java` | MyBatis Mapper |
| `PermissionConstants.java` | 权限常量（OcrRecognition 类） |
| `ui/src/views/ocr/recognition/index.vue` | 前端页面 |
| `ui/src/api/ocr.js` | 前端 API |
| `ui/src/api/routes.js` | API 路由 |
| `ui/src/router/componentMap.js` | 组件注册 |
| `ui/src/i18n/lang/zh-CN.js` | 中文翻译 |
| `ui/src/i18n/lang/en-US.js` | 英文翻译 |

---

## 十一、常见问题

### Q1: 后端启动报 `ClassNotFoundException: IOException`
**原因**：tess4j 依赖冲突
**解决**：检查 `pom.xml` 中 tess4j 版本为 5.11.0，执行 `mvn clean install -q`

### Q2: OCR 识别报 `Invalid memory access`
**原因**：tessdata 路径配置错误
**解决**：确认 `app.ocr.tessdata-path` 指向正确的 tessdata 目录（`D:\tesseract-ocr\tessdata`）

### Q3: OCR 识别结果为空
**原因**：训练数据未下载或语言配置错误
**解决**：
```powershell
# 检查训练数据
Get-ChildItem "D:\tesseract-ocr\tessdata" -Filter "*.traineddata"

# 检查语言
& "D:\tesseract-ocr\tesseract.exe" --list-langs --tessdata-dir "D:\tesseract-ocr\tessdata"
```

### Q4: 上传文件报 403 Forbidden
**原因**：权限未分配
**解决**：重新执行 `db/ocr_recognition.sql`

### Q5: 前端看不到 OCR 菜单
**原因**：菜单未注册或未分配角色
**解决**：
```powershell
# 检查菜单
mysql -u root -proot -e "SELECT * FROM sys_menu WHERE id BETWEEN 600 AND 603" rx_admin

# 检查角色权限
mysql -u root -proot -e "SELECT * FROM sys_role_menu WHERE menu_id BETWEEN 600 AND 603" rx_admin
```

---

## 十二、OCR 准确率优化

### 12.1 已实现的优化（自动生效）

| 优化项 | 说明 |
|--------|------|
| **图片预处理** | 灰度化 → Otsu 二值化 → 中值滤波去噪 → 投影法纠偏 |
| **PDF 逐页处理** | 每页独立判断：文字页提取文字，图片页 OCR |
| **PDF 渲染精度** | 300 DPI 渲染，保证图片清晰度 |
| **Word/Excel 图片** | 嵌入图片自动提取并 OCR |

### 12.2 Tesseract 参数调优

当前默认配置适用于大多数场景，可根据需要调整：

| 参数 | 当前值 | 说明 |
|------|--------|------|
| PSM | 3 | 自动页面分割（推荐） |
| 语言 | chi_sim+eng | 中英文混合 |
| OEM | 默认(LSTM) | 深度学习引擎 |

可选 PSM 模式：
- `PSM 3`：自动分割（默认，适合一般文档）
- `PSM 6`：均匀块分割（适合表格、对齐文本）
- `PSM 7`：单行文本
- `PSM 8`：单个单词
- `PSM 11`：稀疏文本（无序排列的文字）

### 12.3 进阶方案（需额外部署）

如果 Tesseract 准确率不满足需求，可考虑以下更强的 OCR 引擎：

#### PaddleOCR（推荐，中文最强）

百度开源，中文识别准确率业界领先，支持 80+ 语言。

```bash
# 安装
pip install paddlepaddle paddleocr

# 启动 API 服务（类似 WhisperX 模式）
pip install paddleocr-api  # 或自建 Flask/FastAPI 服务
```

| 优势 | 说明 |
|------|------|
| 中文准确率 | 显著优于 Tesseract（尤其是复杂排版、表格） |
| 版面分析 | 自动识别段落、表格、标题结构 |
| 开源免费 | Apache 2.0 协议 |
| GPU 加速 | 支持 CUDA 加速 |

部署方式：Python 微服务（端口 8890），后端通过 HTTP 调用。

#### EasyOCR（多语言友好）

基于 PyTorch，支持 80+ 语言，开箱即用。

```bash
# 安装
pip install easyocr

# Python 调用
import easyocr
reader = easyocr.Reader(['ch_sim', 'en'])
result = reader.readtext('image.png')
```

| 优势 | 说明 |
|------|------|
| 多语言 | 80+ 语言，切换简单 |
| 易用性 | Python API 简洁，无需复杂配置 |
| GPU 支持 | PyTorch 自带 CUDA 支持 |

#### 方案对比

| 引擎 | 中文准确率 | 部署难度 | 依赖 | 适用场景 |
|------|-----------|----------|------|----------|
| **Tesseract** | 中高 | 低 | C++/JNA | 通用文档、已有集成 |
| **PaddleOCR** | 高 | 中 | Python/PaddlePaddle | 中文密集场景、复杂排版 |
| **EasyOCR** | 中高 | 低 | Python/PyTorch | 多语言混合、快速原型 |

#### 集成建议

1. **保持 Tesseract 作为默认引擎**（当前方案）
2. **可选启用 PaddleOCR**：在 `application.yml` 中配置 `app.ocr.engine=paddleocr`
3. **按文件类型选择引擎**：纯中文文档用 PaddleOCR，混合文档用 Tesseract

如果需要集成 PaddleOCR 或 EasyOCR，可参考 WhisperX 的部署模式（Python 微服务 + HTTP API）。
