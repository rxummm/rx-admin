# 视频字幕生成方案分析文档

> **当前状态**：WhisperX 声纹分离已集成完成并可用。
> 详细实施文档见 `vedio-transcription-guide.md`。

## 〇、当前实现状态（2026-06 更新）

### 已完成功能

| 功能 | 状态 | 说明 |
|------|------|------|
| WhisperX 声纹分离 | ✅ 已实现 | 通过 HTTP API 调用 Python whisperx-api 服务 |
| 真实说话人识别 | ✅ 已实现 | 基于 pyannote.audio，自动检测说话人数量 |
| 自动回退机制 | ✅ 已实现 | WhisperX 不可用时自动回退到 whisper.cpp |
| 国内镜像支持 | ✅ 已配置 | HuggingFace 镜像 `hf-mirror.com` |
| k2 模块兼容 | ✅ 已解决 | Windows + Python 3.12 下 mock k2 绕过 |

### 实际部署环境

| 组件 | 版本/型号 |
|------|-----------|
| Python | 3.12.10 |
| whisperx-api | 2.0.1rc2 |
| CUDA | 12.1 |
| GPU | NVIDIA GeForce RTX 4070 Ti SUPER (16GB) |
| WhisperX 模型 | large-v3-turbo (float16) |

### 快速启动命令

```powershell
# 1. 启动 WhisperX 服务
$env:PYTHONIOENCODING = "utf-8"
$env:PYTHONUTF8 = "1"
$env:HF_ENDPOINT = "https://hf-mirror.com"
& "C:\Users\admin\AppData\Local\Programs\Python\Python312\python.exe" -m whisperx_api

# 2. 验证
Invoke-RestMethod -Uri "http://localhost:8880/health"
# 返回: { status: "ok" }

# 3. 启动后端（确保 application.yml 中 whisperx-enabled: true）
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 一、主流实现方式对比

| 方案 | 技术栈 | 输出格式 | 复杂度 | 精度 |
|------|--------|----------|--------|------|
| **A. Whisper.cpp + FFmpeg** (当前方案) | C++ 本地推理 | SRT/TXT/VTT | 低 | 中高 |
| **B. Faster-Whisper** | Python CUDA 加速 | SRT/VTT/JSON | 中 | 高 |
| **C. WhisperX** | Python + PyTorch | SRT/ASS + 说话人分离 | 高 | 极高 |

### 各方案详解

#### 方案 A：Whisper.cpp + FFmpeg（推荐复用）

**优势：**
- 与当前系统技术栈完全一致，复用率高
- 支持 CPU 运行，资源占用低
- 依赖 FFmpeg 提取音频（已有）
- whisper.cpp 原生支持 SRT 输出：`-osrt`

**实现方式：**
```bash
# 1. FFmpeg 提取音频
ffmpeg -i video.mp4 -af aresample=async=1 -ar 16000 -ac 1 -c:a pcm_s16le audio.wav

# 2. Whisper 生成 SRT
whisper-cli -m ggml-small.bin -f audio.wav -l zh -osrt
```

**输出格式：**
- SRT：直接可用
- VTT：`-ovtt` 参数
- TXT：`-otxt` 参数

#### 方案 B：Faster-Whisper

**优势：**
- GPU 加速，速度快
- 词级时间戳更精确

**劣势：**
- 需要 CUDA/PyTorch 环境
- 依赖复杂，部署难度大

#### 方案 C：WhisperX（进阶）

**优势：**
- 说话人分离（Diarization）
- 词级时间戳
- 自动标点和大写

**劣势：**
- 需要额外安装 pyannote.audio 模型
- Python 环境依赖
- 不适合当前 Java 项目架构

---

## 二、字幕格式对比（SRT / ASS）

| 特性 | SRT | ASS | VTT |
|------|-----|-----|-----|
| **样式支持** | 无 | 丰富 | 基础 |
| **动画效果** | 无 | 支持 | 有限 |
| **文件体积** | 小 | 中等 | 小 |
| **兼容性** | 极高 | 中等 | 高(网页) |
| **编辑难度** | 低 | 高 | 中 |
| **适用场景** | 通用字幕 | 特效字幕 | 网页视频 |

### SRT 格式示例
```
1
00:00:01,000 --> 00:00:04,000
这是第一段字幕。

2
00:00:04,500 --> 00:00:08,000
这是第二段字幕。
```

### ASS 格式示例
```
[Script Info]
Title: Generated Subtitle

[Styles]
Format: Name, Fontname, Fontsize, PrimaryColour, Bold
Style: Default,Arial,24,&HFFFFFF,&H1

[Events]
Format: Layer, Start, End, Style, Text
Dialogue: 0,0:00:01.00,0:00:04.00,Default,,这是第一段字幕。
Dialogue: 0,0:00:04.50,0:00:08.00,Default,,这是第二段字幕。
```

---

## 三、当前系统复用分析

### 现有能力（可直接复用）

| 能力 | 状态 | 复用方式 |
|------|------|----------|
| FFmpeg 音频提取 | 已有 | 直接复用 |
| Whisper.cpp 调用 | 已有 | 添加 `-osrt` 参数 |
| 简繁转换 | 已有 | 复用 ICU4J |
| 文件存储 | 已有 | 直接复用 |

### 需要新增

| 功能 | 实现方式 | 复杂度 |
|------|----------|--------|
| 视频上传 | 前端修改 el-upload accept | 低 |
| SRT 生成 | Whisper 添加 `-osrt` 参数 | 低 |
| SRT → ASS 转换 | Java 代码实现 | 中 |
| 字幕文件下载 | REST API 返回文件 | 低 |

### 技术可行性评估

| 项目 | 评估 | 说明 |
|------|------|------|
| 视频支持 | 可行 | FFmpeg 已支持 mp4/avi/mkv |
| SRT 生成 | 可行 | Whisper 原生支持 |
| ASS 生成 | 需转换 | Java 解析 SRT 生成 ASS |
| 双格式输出 | 可行 | 同时生成 SRT + TXT |

---

## 四、推荐实施方案

### 方案：扩展当前音频转写模块

```
统一文件上传
    │
    ├─── 音频: mp3/wav/m4a
    └─── 视频: mp4/avi/mkv
            │
            ▼
    FFmpeg 音频提取
    统一转换为 16kHz 单声道 WAV
            │
            ▼
    Whisper.cpp 转写
    -otxt (完整文本) + -osrt (字幕)
            │
      ┌─────┴─────┐
      ▼           ▼
  台词文档      字幕文件
   (TXT)         (SRT)
      │           │
      │           ▼
      │     SRT → ASS 转换
      │     (可选，带样式)
      ▼           ▼
  文本下载      字幕下载
```

### 实现步骤

| 阶段 | 任务 | 工作量 |
|------|------|--------|
| Phase 1 | 视频上传 + 音频提取 | 1-2天 |
| Phase 2 | SRT 字幕生成 | 0.5天 |
| Phase 3 | 台词+字幕文件下载 | 0.5天 |
| Phase 4 | SRT→ASS 转换(可选) | 1天 |

### 数据库表扩展

```sql
ALTER TABLE audio_transcription ADD COLUMN file_type VARCHAR(20) DEFAULT 'audio';
ALTER TABLE audio_transcription ADD COLUMN srt_path VARCHAR(500);
ALTER TABLE audio_transcription ADD COLUMN ass_path VARCHAR(500);
```

---

## 五、结论

| 维度 | 评估 |
|------|------|
| **技术可行性** | 高，当前系统可直接复用 80% 代码 |
| **开发成本** | 低，预计 3-4 天完成核心功能 |
| **用户体验** | 好，统一入口，无需切换模块 |
| **推荐指数** | 五星 |

**建议：** 在现有音频转写模块基础上扩展，添加视频支持、SRT 生成、文件下载功能。ASS 字幕作为可选功能后续迭代。

---

## 六、Java 与 WhisperX 集成方案详解

### 核心问题：Java 能直接调用 WhisperX 吗？

| 技术 | 语言 | Java 兼容性 |
|------|------|-------------|
| Whisper.cpp | C++ | ✅ 可以（ProcessBuilder 直接调用） |
| WhisperX | Python + PyTorch | ❌ 不可以（无 Java SDK） |
| faster-whisper | Python + CTranslate2 | ❌ 不可以（无 Java SDK） |

**结论**：Java 不能直接调用 Python 的 WhisperX，但可以通过 **HTTP API** 间接使用。

### 推荐方案：Java → Python HTTP API

```
Java 应用  ──HTTP POST──▶  Python WhisperX 服务  ──处理──▶  返回 JSON 结果
    │                         │
    │   multipart/form-data   │
    │◀──转写结果──────────────│
```

### 架构图

```
┌─────────────────────────────────────────────────────────┐
│                     服务器/电脑                          │
│                                                         │
│   ┌──────────────────┐     ┌──────────────────────┐   │
│   │  Python 服务      │     │  Spring Boot 项目    │   │
│   │                  │     │                      │   │
│   │  Port: 8880     │◀───│  Port: 8088         │   │
│   │                  │ HTTP │                      │   │
│   │  (WhisperX)     │     │  (业务逻辑 + API)    │   │
│   └──────────────────┘     └──────────────────────┘   │
│                                                         │
│   ┌──────────────────┐                                 │
│   │  前端 Vue         │                                 │
│   │  Port: 3000     │                                 │
│   │                  │                                 │
│   └──────────────────┘                                 │
└─────────────────────────────────────────────────────────┘
```

### 方案对比

| 集成方式 | 打包体积 | 性能 | 部署难度 | 推荐度 |
|----------|----------|------|----------|--------|
| **HTTP API** (推荐) | 无需打包 | ⭐⭐⭐⭐⭐ | 低 | ⭐⭐⭐⭐⭐ |
| **PyInstaller exe** | 500MB-1GB | ⭐⭐⭐ | 中 | ⭐⭐ |
| **完整 WhisperX exe** | 3-8GB | ⭐⭐⭐⭐⭐ | 高 | ❌ 不推荐 |

### 快速启动 whisperx-api（Windows 详细步骤）

#### 1. Python 版本要求

**必须使用 Python 3.10 ~ 3.12**。Python 3.13/3.14 无 PyTorch 预编译包，安装会失败。

```powershell
# 检查版本
python --version
# 必须显示 3.10.x / 3.11.x / 3.12.x
```

#### 2. 安装 whisperx-api

```powershell
# 使用国内镜像加速（推荐）
pip install whisperx-api -i https://pypi.tuna.tsinghua.edu.cn/simple
```

安装位置：
- **whisperx-api 包本体** → `{Python安装目录}\Lib\site-packages\whisperx_api\`
- **whisperx 依赖包** → `{Python安装目录}\Lib\site-packages\whisperx\`

#### 3. 模型文件说明（重要）

模型文件（~2GB）**不是 pip install 时安装的**，而是在**第一次启动服务时**自动下载到本地缓存目录：

```
C:\Users\<用户名>\.cache\whisperx\      # WhisperX 模型
C:\Users\<用户名>\.cache\torch\          # PyTorch 预训练模型
C:\Users\<用户名>\.cache\huggingface\    # pyannote.audio 声纹模型
```

> 首次启动会下载模型（耗时较长），**之后启动直接读取缓存，不会再下载**。

#### 4. 自定义缓存目录（可选）

默认下载到 `C:\Users\<用户名>\.cache\`，可通过环境变量更改：

```powershell
# 临时设置（当前终端生效）
$env:XDG_CACHE_HOME = "D:\whisperx-cache"
$env:HF_HOME = "D:\whisperx-cache\huggingface"

# 永久设置（用户级别）
[Environment]::SetEnvironmentVariable("XDG_CACHE_HOME", "D:\whisperx-cache", "User")
[Environment]::SetEnvironmentVariable("HF_HOME", "D:\whisperx-cache\huggingface", "User")
```

#### 5. 启动服务

```powershell
python -m whisperx_api
```

默认监听 `http://0.0.0.0:8880`，默认 API Key: `namastex888`。

看到如下输出说明启动成功：
```
INFO:     Uvicorn running on http://0.0.0.0:8880
```

验证服务：
```powershell
curl http://localhost:8880/health
# 返回 {"status":"ok"}
```

**功能特性**：
- GPU 加速转写
- 说话人分离 (Diarization)
- 词级时间戳
- SRT/VTT/TXT/JSON 多格式导出
- Webhook 回调

### Java 调用示例（已实现）

实际实现位于 `WhisperEngine.java`，核心代码：

```java
// 检测 WhisperX 服务是否可用
public boolean isWhisperXAvailable() {
    if (appConfig.getAudio() == null || !appConfig.getAudio().isWhisperxEnabled()) return false;
    try {
        RestTemplate rt = createRestTemplate();
        ResponseEntity<Map> resp = rt.getForEntity(apiUrl + "/health", Map.class);
        return resp.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
        return false;
    }
}

// 调用 WhisperX API 进行转写（含声纹分离）
public WhisperResult transcribeWithWhisperX(File wavFile, String language) {
    RestTemplate rt = createRestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.set("X-API-Key", appConfig.getAudio().getWhisperxApiKey());

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(wavFile));
    body.add("language", language);

    ResponseEntity<Map> response = rt.exchange(
        apiUrl + "/transcribe",
        HttpMethod.POST,
        new HttpEntity<>(body, headers),
        Map.class
    );

    // 解析 JSON: segments[{start, end, text, speaker}]
    WhisperResult result = new WhisperResult();
    result.setSegments(parseWhisperXSegments((List<Map<?, ?>>) response.getBody().get("segments")));
    result.setUsedFallback(false);
    return result;
}
```

### Python exe 打包方案（不推荐）

如果坚持用 Python exe 方案，可用 **faster-whisper + PyInstaller**：

```python
# transcribe_cli.py (简化版)
from faster_whisper import WhisperModel
import sys
import json

model = WhisperModel("small", device="cpu", compute_type="int8")
segments, info = model.transcribe(sys.argv[1], language="zh")

result = {
    "text": "".join([s.text for s in segments]),
    "segments": [{"start": s.start, "end": s.end, "text": s.text} for s in segments],
    "language": info.language
}
print(json.dumps(result, ensure_ascii=False))
```

```bash
# 打包命令
pip install pyinstaller
pyinstaller --onefile --name whisper_cli transcribe_cli.py
# 打包后体积：约 500MB-1GB
```

### Windows 已知问题：k2 模块缺失

whisperx-api 依赖 `speechbrain`，而 `speechbrain.integrations.k2_fsa` 尝试导入 `k2` 模块。
`k2` 在 Windows + Python 3.12 下无预编译包，会导致启动失败：

```
ImportError: Lazy import of LazyModule(package=None, target=speechbrain.integrations.k2_fsa, loaded=False) failed
```

**解决方案**：创建 mock k2 模块绕过：

```powershell
$sitePackages = "C:\Users\admin\AppData\Local\Programs\Python\Python312\Lib\site-packages"
New-Item -ItemType Directory -Path "$sitePackages\k2" -Force
Set-Content -Path "$sitePackages\k2\__init__.py" -Value '"""Mock k2 module for speechbrain compatibility."""'
Set-Content -Path "$sitePackages\k2\_k2.py" -Value '"""Mock _k2 module."""'
```

> k2 仅用于 speechbrain 的高级图解码功能，mock 后不影响 WhisperX 的声纹分离核心功能。

### 服务器部署完整步骤

#### 前提条件

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| Python | 3.10 ~ 3.12 | WhisperX 运行环境 |
| FFmpeg | 最新 | 视频音频提取 |
| Java | 17+ | Spring Boot 3.5 |
| Node.js | 16+ | 前端构建 |
| NVIDIA Driver | 推荐 | GPU 加速（可选） |

#### 步骤一：安装 Python + WhisperX

```bash
# 安装 Python 3.12（Ubuntu/Debian 示例）
sudo apt install python3.12 python3.12-venv python3-pip

# 创建虚拟环境（推荐，避免污染系统 Python）
python3.12 -m venv /opt/whisperx-env
source /opt/whisperx-env/bin/activate

# 安装 whisperx-api（首次会下载 PyTorch ~2GB）
pip install whisperx-api -i https://pypi.tuna.tsinghua.edu.cn/simple

# 测试启动（首次会自动下载模型 ~2GB 到 ~/.cache/）
python -m whisperx_api &
curl http://localhost:8880/health
```

> ⚠️ 首次启动会自动下载模型到 `~/.cache/whisperx/`、`~/.cache/torch/`、`~/.cache/huggingface/`，耗时较长。**以后启动直接读取缓存，不再下载**。

#### 步骤二：配置自启动（systemd）

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

# GPU 优化
Environment="CUDA_VISIBLE_DEVICES=0"
# 自定义缓存目录（可选）
Environment="XDG_CACHE_HOME=/data/whisperx-cache"
Environment="HF_HOME=/data/whisperx-cache/huggingface"

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable whisperx
sudo systemctl start whisperx
sudo systemctl status whisperx
```

#### 步骤三：配置 Java 后端

修改 `application-prod.yml`：

```yaml
app:
  audio:
    whisperx-enabled: true
    whisperx-api-url: http://localhost:8880
    # 如果分开部署，改为远程地址：
    # whisperx-api-url: http://192.168.1.100:8880
    whisperx-api-key: namastex888
```

构建并启动：

```bash
# 构建
mvn clean package -Dspring-boot.run.profiles=prod -DskipTests

# 启动
java -jar target/rx-admin.jar --spring.profiles.active=prod
```

#### 步骤四：部署前端

```bash
cd ui
npm install
npm run build
# 构建产物在 ui/dist/，用 nginx 代理
```

```nginx
# /etc/nginx/sites-available/rx-admin
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/ui/dist;
    index index.html;

    location /api/ {
        proxy_pass http://localhost:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

#### 一键启动脚本

```bash
# start-server.sh - Linux 服务器一键启动
#!/bin/bash
echo "Starting WhisperX..."
sudo systemctl start whisperx
sleep 5

echo "Starting Spring Boot..."
nohup java -jar /opt/rx-admin/rx-admin.jar --spring.profiles.active=prod > /opt/rx-admin/app.log 2>&1 &

echo "Starting Frontend..."
cd /opt/rx-admin/ui && nohup npm run dev > /opt/rx-admin/ui.log 2>&1 &

echo "All services started."
```

#### 分开部署场景（推荐生产）

```
┌─ 应用服务器 (CPU) ─────────────────┐
│ Spring Boot + Vue (Nginx)          │
│ WHISPERX_API_URL=http://gpu-svr:8880 │
└────────┬───────────────────────────┘
         │ HTTP (内网)
┌────────▼───────────────────────────┐
│ GPU 服务器 (NVIDIA)                │
│ whisperx-api (port 8880)           │
│ 模型文件: /data/whisperx-cache/    │
└────────────────────────────────────┘
```

### 部署方式对比

| 方式 | 描述 | 适用场景 |
|------|----------|------|
| **本地开发** | 两个服务都本地运行 | 开发调试 |
| **同一服务器** | Python + Spring Boot 在同一台机器 | 小规模部署 |
| **分开部署** | Python 在 GPU 服务器，Java 在另一台 | 大规模/生产 |

### 与当前 Whisper.cpp 方案对比

| 维度 | Whisper.cpp (当前) | WhisperX (扩展) |
|------|-------------------|-----------------|
| **运行环境** | CPU | GPU (推荐) / CPU |
| **精度** | 中高 | 极高 |
| **说话人分离** | ❌ 不支持 | ✅ 支持 |
| **词级时间戳** | ❌ 不支持 | ✅ 支持 |
| **部署复杂度** | 低 | 中 (需 Python 环境) |
| **与现有系统集成** | 直接调用 | HTTP API 调用 |
| **维护成本** | 低 | 中 |

### 结论

| 场景 | 推荐方案 |
|------|----------|
| 有 GPU，追求高精度 | **HTTP API** (`uvx whisperx-api`) |
| 纯 CPU，追求简单 | **当前 Whisper.cpp** (已有) |
| 必须 exe 封装 | **faster-whisper + PyInstaller** |

**最终建议**：保留当前的 Whisper.cpp 方案作为基础，新增 WhisperX 作为可选高级模式，用户可根据硬件条件选择使用。

---

## 七、参考资料

1. [whisper.cpp 命令行工具详解](https://adg.csdn.net/69524fd55b9f5f31781b7e62.html)
2. [毫秒级语音分段：whisper.cpp 时间戳生成技术全解析](https://blog.csdn.net/gitblog_00511/article/details/151820351)
3. [本地多语言 AI 字幕组：whisper 实战教程](https://www.leavesongs.com/THINK/using-whisper-ai-to-generate-video-subtitles.html)
4. [用 Tauri + FFmpeg + Whisper.cpp 从零打造本地字幕生成器](https://juejin.cn/post/7528457291697012774)
5. [FFmpeg 字幕处理实战：SRT 与 ASS 格式的深度对比与应用场景解析](https://devpress.csdn.net/avi/69bc3bb654b52172bc629fcb.html)
6. [彻底搞懂「字幕」：从格式、软硬到嵌入](https://juejin.cn/post/7566476530501353499)
7. [whisperx-api - PyPI](https://pypi.org/project/whisperx-api/)
8. [faster-whisper-server - OpenAI 兼容 API](https://pypi.org/project/faster-whisper-server/)
9. [Java 调用 Python 模型：HTTP API 方案](https://blog.51cto.com/u_16213652/14239257)
10. [4倍速语音转写：FastAPI + Faster-Whisper 实战](https://blog.csdn.net/gitblog_00408/article/details/151367333)
