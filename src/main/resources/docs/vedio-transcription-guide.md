# 视频转写模块实施文档

## 一、功能概述

本模块实现视频文件转文字功能，支持：
- 视频文件上传（mp4/avi/mkv等格式）
- FFmpeg 音频提取（从视频中提取音频轨道）
- 多语言支持（中文、英文等）
- 分段时间戳记录
- **说话人分离（角色区分）** — 支持两种模式：
  - **WhisperX 声纹分离**（默认）：基于 pyannote.audio 的真实声纹识别，自动区分不同说话人
  - **轮询分配**（回退）：whisper.cpp 模式下按 SPEAKER_00/01/02/03 交替分配
- **SRT/ASS 字幕文件生成**
- **按角色台词导出**
- 结果存储到数据库

## 二、技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 转写引擎 | WhisperX / whisper.cpp | 双引擎，优先 WhisperX |
| 音频提取 | FFmpeg | 从视频中提取音频 |
| 说话人分离 | pyannote.audio (via WhisperX) | 真实声纹分离 |
| Java集成 | RestTemplate HTTP 调用 | 调用 Python WhisperX 服务 |
| 数据库 | MySQL | 存储转写记录和分段信息 |
| GPU加速 | CUDA 12.1 + RTX 4070 Ti SUPER | WhisperX GPU 加速转写 |

## 三、文件结构

```
src/main/java/com/rx/admin/modules/video/
├── controller/
│   └── VideoTranscriptionController.java   # 控制器
├── service/
│   ├── IVideoTranscriptionService.java     # 服务接口
│   └── VideoTranscriptionService.java      # 服务实现（含 WhisperX 分支逻辑）
├── mapper/
│   ├── VideoTranscriptionMapper.java       # 主表Mapper
│   └── VideoSegmentMapper.java             # 分段表Mapper
├── entity/
│   ├── VideoTranscription.java             # 转写实体
│   └── VideoSegment.java                   # 分段实体
├── dto/
│   └── VideoTranscriptionQueryDTO.java     # 查询DTO
├── vo/
│   ├── VideoTranscriptionVO.java           # 返回VO
│   └── VideoSegmentVO.java                 # 分段VO
└── convert/
    └── VideoConvert.java                   # 对象转换

src/main/java/com/rx/admin/modules/transcription/
├── WhisperEngine.java                      # 核心转写引擎（含 WhisperX HTTP 调用）
└── TranscriptionDispatcher.java            # 异步转写调度器

src/main/java/com/rx/admin/common/config/
└── AppConfig.java                          # 配置类（含 WhisperX 配置项）
```

## 四、数据库设计

### 4.1 表结构

#### video_transcription（视频转写主表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| file_name | VARCHAR(255) | 原始文件名 |
| file_path | VARCHAR(500) | 文件存储路径 |
| audio_path | VARCHAR(500) | 提取的音频路径 |
| language | VARCHAR(20) | 语言代码（zh/en） |
| full_text | TEXT | 完整转写文本 |
| duration | DOUBLE | 音频时长(秒) |
| model_name | VARCHAR(50) | 使用的模型名称（WhisperX 模式下为 large-v3-turbo） |
| speaker_count | INT | 说话人数量 |
| status | INT | 状态(0-失败, 1-成功, 2-待转写) |
| srt_path | VARCHAR(500) | SRT字幕路径 |
| ass_path | VARCHAR(500) | ASS字幕路径 |
| error_message | VARCHAR(500) | 错误信息 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除(0-未删除 1-已删除) |

#### video_segment（视频转写分段表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| transcription_id | BIGINT | 转写ID |
| start_time | DOUBLE | 开始时间(秒) |
| end_time | DOUBLE | 结束时间(秒) |
| text | TEXT | 分段文本 |
| speaker_label | VARCHAR(20) | 说话人标签（WhisperX: 真实分离 / whisper.cpp: SPEAKER_00等） |
| speaker_name | VARCHAR(50) | 说话人名称（用户可编辑） |
| confidence | FLOAT | 置信度 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除(0-未删除 1-已删除) |

### 4.2 创建表语句

执行 `db/video_transcription.sql` 文件：

```bash
mysql -u root -proot rx_admin < db/video_transcription.sql
```

## 五、配置说明

### 5.1 application.yml 完整配置

```yaml
app:
  audio:
    # --- Whisper.cpp 配置 ---
    whisper-path: ${AUDIO_WHISPER_PATH:D:\vueprojects\ffmpeg\bin\whisper.exe}
    model-path: ${AUDIO_MODEL_PATH:D:\whisper\models}
    default-model: ${AUDIO_DEFAULT_MODEL:tiny}
    default-language: ${AUDIO_DEFAULT_LANGUAGE:zh}
    threads: ${AUDIO_THREADS:4}
    temp-dir: ${AUDIO_TEMP_DIR:D:\temp\audio}
    max-file-size-mb: ${AUDIO_MAX_FILE_SIZE_MB:100}
    enabled: ${AUDIO_ENABLED:true}
    # --- WhisperX 说话人分离配置 ---
    whisperx-enabled: ${WHISPERX_ENABLED:true}                  # 是否启用 WhisperX
    whisperx-api-url: ${WHISPERX_API_URL:http://localhost:8880} # WhisperX 服务地址
    whisperx-api-key: ${WHISPERX_API_KEY:namastex888}           # API Key
```

### 5.2 转写引擎优先级

```
whisperx-enabled=true 且 WhisperX 服务可用?
  ├─ 是 → WhisperX API（真实声纹分离）
  └─ 否 → whisper.cpp 可用?
           ├─ 是 → whisper.cpp（轮询分配说话人）
           └─ 否 → 演示模式（模拟数据）
```

### 5.3 依赖工具

| 工具 | 必需 | 说明 |
|------|------|------|
| FFmpeg | 是 | 视频音频提取，必须安装 |
| whisper.cpp | 否 | Whisper 转写引擎，不配置则使用演示模式 |
| WhisperX (Python) | 推荐 | 说话人分离服务，需 Python 3.10~3.12 |
| CUDA 12.1+ | 推荐 | WhisperX GPU 加速，无 GPU 时使用 CPU |

## 六、API 接口

### 6.1 上传视频文件（不转写）

```
POST /api/v1/video/transcription/upload-only
```

**参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | 视频文件 |
| language | String | 否 | 语言代码(zh/en) |

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "fileName": "test.mp4",
    "language": "zh",
    "status": 2
  }
}
```

### 6.2 对已上传记录进行转写

```
POST /api/v1/video/transcription/{id}/transcribe
```

**参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 转写记录ID |
| model | String | 否 | 模型名称（WhisperX 模式下忽略，使用 large-v3-turbo） |

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "fileName": "test.mp4",
    "language": "zh",
    "fullText": "角色A：你好\n角色B：你好，很高兴见到你",
    "duration": 15.5,
    "modelName": "large-v3-turbo",
    "speakerCount": 2,
    "status": 1,
    "segments": [
      {
        "startTime": 0.0,
        "endTime": 3.5,
        "text": "你好",
        "speakerLabel": "SPEAKER_00",
        "speakerName": "角色1"
      },
      {
        "startTime": 4.0,
        "endTime": 8.0,
        "text": "你好，很高兴见到你",
        "speakerLabel": "SPEAKER_01",
        "speakerName": "角色2"
      }
    ]
  }
}
```

### 6.3 分页查询转写记录

```
GET /api/v1/video/transcription/page
```

**参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认10 |
| keyword | String | 否 | 关键词搜索 |
| language | String | 否 | 语言筛选 |
| status | Integer | 否 | 状态筛选 |

### 6.4 获取转写详情

```
GET /api/v1/video/transcription/{id}
```

### 6.5 删除转写记录

```
DELETE /api/v1/video/transcription/{id}
```

### 6.6 批量删除转写记录

```
DELETE /api/v1/video/transcription/batch/{ids}
```

### 6.7 下载 SRT 字幕

```
GET /api/v1/video/transcription/{id}/download-srt
```

### 6.8 下载 ASS 字幕

```
GET /api/v1/video/transcription/{id}/download-ass
```

### 6.9 下载按角色台词

```
GET /api/v1/video/transcription/{id}/download-dialogue
```

### 6.10 修改说话人名称

```
PUT /api/v1/video/transcription/{id}/speaker-name
```

**参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| speakerLabel | String | 是 | 说话人标签(SPEAKER_00) |
| speakerName | String | 是 | 新的说话人名称 |

### 6.11 修改文件名

```
PUT /api/v1/video/transcription/{id}/file-name
```

**参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileName | String | 是 | 新文件名 |

## 七、权限配置

| 权限标识 | 说明 |
|---------|------|
| video:transcription:upload | 上传转写 |
| video:transcription:list | 列表查询 |
| video:transcription:view | 查看详情 |
| video:transcription:delete | 删除记录 |
| video:transcription:update | 修改说话人名称/文件名 |

## 八、前端页面

前端页面位于 `ui/src/views/video/transcription/index.vue`

**功能:**
- 视频文件上传（支持拖拽，多文件，最大500MB）
- 列表展示转写记录
- 语言筛选
- 查看转写详情（按角色分组展示）
- 说话人名称编辑
- SRT/ASS 字幕下载
- 按角色台词下载
- 批量删除

## 九、菜单注册

需要在 `sys_menu` 表中注册菜单：

```sql
INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, status, menu_type)
VALUES ('视频转写', 24, 'video/transcription', 'video/transcription/index', 'Video', 51, 1, 'C');
```

详细菜单注册脚本见 `db/video_transcription.sql`。

## 十、核心实现详解

### 10.1 转写流程（WhisperX 模式）

```
用户上传视频
    ↓
前端: POST /video/transcription/upload-only (FormData)
    ↓
Controller: 保存文件到 storage 目录，写 DB 记录 (status=2 待转写)
    ↓
用户点击"转写" → 选择模型
    ↓
前端: POST /video/transcription/{id}/transcribe
    ↓
Controller: updateStatus(PENDING) → TranscriptionDispatcher.processVideo (new Thread)
    ↓
Service.transcribeById:
    1. 检查 whisperx-enabled + isWhisperXAvailable()
    2. FFmpeg: video.mp4 → 16kHz mono WAV
    3. POST http://localhost:8880/transcribe (multipart, 含文件+语言)
    4. WhisperX 服务: faster-whisper 转写 + pyannote 声纹分离
    5. 返回 JSON: segments[{start, end, text, speaker}]
    6. Java 解析 → 填充 VideoSegment (speakerLabel = 真实 SPEAKER_XX)
    7. 中文繁体→简体转换 (ICU4J)
    8. 生成 SRT 字幕文件
    9. 更新 DB (status=1, fullText, segments, srtPath, speakerCount)
    ↓
前端轮询: GET /video/transcription/{id} (每2秒)
    → status=1 → 显示成功 → 刷新列表
    → 用户查看详情 → 按角色展示台词 → 下载 SRT/ASS/台词
```

### 10.2 核心代码改动

#### WhisperEngine.java — 新增 WhisperX HTTP 调用

```java
// 检测 WhisperX 服务是否可用
public boolean isWhisperXAvailable() {
    if (appConfig.getAudio() == null || !appConfig.getAudio().isWhisperxEnabled()) return false;
    // GET ${whisperxApiUrl}/health → 200 OK
}

// 调用 WhisperX API 进行转写
public WhisperResult transcribeWithWhisperX(File wavFile, String language) {
    // POST ${whisperxApiUrl}/transcribe
    // multipart/form-data: file + language
    // Header: X-API-Key
    // 解析 JSON → WhisperSegment (含 speaker 字段)
}
```

#### VideoTranscriptionService.java — 转写流程分支

```java
// 优先级: WhisperX → whisper.cpp → 演示模式
if (whisperxEnabled && whisperEngine.isWhisperXAvailable()) {
    result = whisperEngine.transcribeWithWhisperX(wavFile, language);
} else if (whisperEngine.isToolAvailable()) {
    result = whisperEngine.transcribeWav(wavFile, language, model);
} else {
    result = whisperEngine.fallbackResult(language);
}

// buildSegments: WhisperX 返回的 speaker 直接写入 VideoSegment
if (src.getSpeaker() != null) {
    seg.setSpeakerLabel(src.getSpeaker());
}

// assignSpeakers: 仅在无真实 speaker 时轮询分配
boolean hasRealSpeakers = segments.stream()
    .anyMatch(s -> s.getSpeakerLabel() != null && !s.getSpeakerLabel().isEmpty());
if (hasRealSpeakers) {
    // 统计真实说话人数量，为缺失的分配默认名称
} else {
    // 轮询分配 SPEAKER_00/01/02/03
}
```

#### AppConfig.AudioConfig — 新增配置字段

```java
@Data
public static class AudioConfig {
    // ... 原有字段 ...
    private boolean whisperxEnabled = false;
    private String whisperxApiUrl = "http://localhost:8880";
    private String whisperxApiKey = "namastex888";
}
```

### 10.3 SRT 字幕格式示例

```
1
00:00:00,000 --> 00:00:03,500
角色1：你好

2
00:00:04,000 --> 00:00:08,000
角色2：你好，很高兴见到你
```

### 10.4 按角色台词导出示例

```
按角色导出台词
========================================

【角色1】
------------------------------
  [00:00:00,000 --> 00:00:03,500]
  你好

【角色2】
------------------------------
  [00:00:04,000 --> 00:00:08,000]
  你好，很高兴见到你
```

## 十一、注意事项

1. **模型文件较大**：WhisperX 模型约 2GB，首次启动自动下载
2. **转写耗时**：视频转写可能需要较长时间，建议异步处理
3. **GPU 加速**：推荐使用 NVIDIA GPU，无 GPU 时 WhisperX 仍可运行但较慢
4. **视频格式**：支持 MP4/AVI/MKV/FLV/MOV/WEBM/WMV/M4V 等格式
5. **文件大小限制**：默认最大 500MB
6. **说话人数量**：WhisperX 自动检测说话人数量，理论无上限
7. **网络环境**：首次下载模型需要访问 HuggingFace，国内环境需配置镜像

---

## 十二、WhisperX 说话人分离服务部署

### 12.1 Python 环境要求

**必须使用 Python 3.10 ~ 3.12**。Python 3.13/3.14 无 PyTorch 预编译包。

```powershell
# 检查版本（必须显示 3.10.x / 3.11.x / 3.12.x）
python --version
```

### 12.2 安装 whisperx-api

```powershell
# 使用国内镜像加速
pip install whisperx-api -i https://pypi.tuna.tsinghua.edu.cn/simple
```

#### 已知问题：k2 模块缺失

whisperx-api 依赖 speechbrain，speechbrain 的 k2_fsa 集成需要 k2 模块。
k2 在 Windows + Python 3.12 下无预编译包，需要创建 mock 模块绕过：

```powershell
# 创建 mock k2 模块（跳过 k2 原生依赖）
$sitePackages = "C:\Users\admin\AppData\Local\Programs\Python\Python312\Lib\site-packages"
New-Item -ItemType Directory -Path "$sitePackages\k2" -Force
Set-Content -Path "$sitePackages\k2\__init__.py" -Value '"""Mock k2 module for speechbrain compatibility."""'
Set-Content -Path "$sitePackages\k2\_k2.py" -Value '"""Mock _k2 module."""'
```

### 12.3 模型文件说明

模型文件在**第一次启动服务时**自动下载到缓存：

```
C:\Users\<用户名>\.cache\whisperx\      # WhisperX 模型
C:\Users\<用户名>\.cache\torch\          # PyTorch 预训练模型
C:\Users\<用户名>\.cache\huggingface\    # pyannote.audio 声纹模型
```

> 首次启动会下载模型（耗时较长），**之后启动直接读取缓存，不再重复下载**。

#### 国内环境镜像配置

首次启动前设置 HuggingFace 镜像，避免下载超时：

```powershell
# 临时设置（当前终端）
$env:HF_ENDPOINT = "https://hf-mirror.com"

# 永久设置（用户级别）
[Environment]::SetEnvironmentVariable("HF_ENDPOINT", "https://hf-mirror.com", "User")
```

如需更改缓存目录：

```powershell
[Environment]::SetEnvironmentVariable("XDG_CACHE_HOME", "D:\whisperx-cache", "User")
[Environment]::SetEnvironmentVariable("HF_HOME", "D:\whisperx-cache\huggingface", "User")
```

### 12.4 启动服务

```powershell
# 设置 UTF-8 编码（避免 Windows GBK 编码问题）
$env:PYTHONIOENCODING = "utf-8"
$env:PYTHONUTF8 = "1"

# 设置 HuggingFace 镜像（国内环境）
$env:HF_ENDPOINT = "https://hf-mirror.com"

# 启动服务
python -m whisperx_api
```

默认监听 `http://0.0.0.0:8880`，默认 API Key: `namastex888`。

#### 验证服务

```powershell
# 健康检查
Invoke-RestMethod -Uri "http://localhost:8880/health"
# 返回: { status: "ok" }

# 或使用 curl
curl http://localhost:8880/health
```

#### 成功启动日志示例

```
================================================================
  ✓ CUDA         12.1         (required)  1 GPU(s) available
  ✓ GPU          NVIDIA GeForce RTX 4070 Ti SUPER (16.0GB)
  ✓ cuDNN        90100        (optional)
  ✓ ffmpeg       8.1.1        (required)  system
----------------------------------------------------------------
  All required dependencies are available.
================================================================

Using GPU [0]: NVIDIA GeForce RTX 4070 Ti SUPER
[whisperx-api] Preloading models at startup...
[whisperx-api] Loading WhisperX model: large-v3-turbo (float16)...
[whisperx-api] WhisperX model loaded successfully
[whisperx-api] Startup preload complete - ready for requests
```

### 12.5 Windows 后台启动脚本

创建 `start-whisperx.ps1`：

```powershell
# start-whisperx.ps1
$env:PYTHONIOENCODING = "utf-8"
$env:PYTHONUTF8 = "1"
$env:HF_ENDPOINT = "https://hf-mirror.com"

Start-Process -FilePath "C:\Users\admin\AppData\Local\Programs\Python\Python312\python.exe" `
    -ArgumentList "-m", "whisperx_api" `
    -WorkingDirectory "D:\vueprojects\RX" `
    -RedirectStandardOutput "D:\vueprojects\RX\whisperx-server.log" `
    -RedirectStandardError "D:\vueprojects\RX\whisperx-server-err.log" `
    -NoNewWindow

Write-Host "WhisperX service started. Check logs:"
Write-Host "  Output: D:\vueprojects\RX\whisperx-server.log"
Write-Host "  Error:  D:\vueprojects\RX\whisperx-server-err.log"
```

### 12.6 服务器部署（Linux）

#### 安装

```bash
# Ubuntu/Debian
sudo apt install python3.12 python3.12-venv python3-pip ffmpeg

python3.12 -m venv /opt/whisperx-env
source /opt/whisperx-env/bin/activate
pip install whisperx-api -i https://pypi.tuna.tsinghua.edu.cn/simple
```

#### systemd 自启动

```ini
# /etc/systemd/system/whisperx.service
[Unit]
Description=WhisperX API Service
After=network.target

[Service]
Type=simple
User=deploy
ExecStart=/opt/whisperx-env/bin/python -m whisperx_api --host 0.0.0.0 --port 8880
Restart=on-failure
RestartSec=10
Environment="CUDA_VISIBLE_DEVICES=0"
Environment="XDG_CACHE_HOME=/data/whisperx-cache"
Environment="HF_HOME=/data/whisperx-cache/huggingface"
Environment="HF_ENDPOINT=https://hf-mirror.com"

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable whisperx
sudo systemctl start whisperx
```

### 12.7 分开部署架构（推荐生产）

```
┌─ 应用服务器 (CPU) ─────────────────┐
│ Spring Boot + Vue (Nginx)          │
│ WHISPERX_API_URL=http://gpu-svr:8880 │
└────────┬───────────────────────────┘
         │ HTTP (内网)
┌────────▼───────────────────────────┐
│ GPU 服务器 (NVIDIA)                │
│ whisperx-api (port 8880)           │
│ 模型缓存: /data/whisperx-cache/    │
└────────────────────────────────────┘
```

### 12.8 Java 调用流程

```
Java 应用                           WhisperX 服务
  │                                      │
  │  1. FFmpeg 提取 16kHz WAV            │
  │  2. POST /transcribe (multipart) ────→
  │                                      │── WhisperX 转写
  │                                      │── pyannote 说话人分离
  │  ←──── JSON (含 speaker) ───────────│
  │  3. 解析 segments[{start,end,text,speaker}]
  │  4. 存入 video_segment 表
  │  5. 生成 SRT/ASS 字幕
```

WhisperX 返回的 JSON 格式：

```json
{
  "segments": [
    {"start": 0.0, "end": 3.5, "text": "你好", "speaker": "SPEAKER_00"},
    {"start": 4.0, "end": 8.0, "text": "你好，很高兴见到你", "speaker": "SPEAKER_01"}
  ],
  "language": "zh"
}
```

Java 端通过 `RestTemplate` 上传音频文件调用，`speaker` 字段直接填充 `video_segment.speaker_label`，替换 `assignSpeakers()` 的轮询逻辑。

## 十三、快速启动检查清单

### 首次部署

- [ ] 安装 Python 3.10~3.12
- [ ] `pip install whisperx-api -i https://pypi.tuna.tsinghua.edu.cn/simple`
- [ ] 创建 mock k2 模块（见 12.2 节）
- [ ] 设置 HuggingFace 镜像：`$env:HF_ENDPOINT = "https://hf-mirror.com"`
- [ ] 启动 WhisperX 服务：`python -m whisperx_api`（首次自动下载模型）
- [ ] 验证：`Invoke-RestMethod -Uri "http://localhost:8880/health"`
- [ ] 确保 `application.yml` 中 `whisperx-enabled: true`
- [ ] 启动后端：`mvn spring-boot:run -Dspring-boot.run.profiles=local`
- [ ] 测试视频转写，验证说话人分离效果

### 日常启动

```powershell
# 1. 启动 WhisperX 服务
& "C:\Users\admin\AppData\Local\Programs\Python\Python312\python.exe" -m whisperx_api

# 2. 启动后端
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. 启动前端
cd ui; npm run dev
```
