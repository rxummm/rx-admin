# 音频转写模块实施文档

## 一、功能概述

本模块实现音频文件转文字功能，支持：
- 音频文件上传并转写
- 实时麦克风输入转写（后续扩展）
- 多语言支持（中文、英文等）
- 分段时间戳记录
- 结果存储到数据库

## 二、技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 转写引擎 | Whisper (openai/whisper) | 开源免费，支持100+语言 |
| 音频处理 | FFmpeg | 格式转换、降噪处理 |
| Java集成 | whisper.cpp (命令行调用) | 性能更好，内存占用低 |
| 数据库 | MySQL | 存储转写记录和分段信息 |

## 三、文件结构

```
src/main/java/com/rx/admin/modules/audio/
├── controller/
│   └── AudioTranscriptionController.java   # 控制器
├── service/
│   ├── IAudioTranscriptionService.java     # 服务接口
│   └── AudioTranscriptionService.java      # 服务实现
├── mapper/
│   ├── AudioTranscriptionMapper.java       # 主表Mapper
│   └── AudioSegmentMapper.java             # 分段表Mapper
├── entity/
│   ├── AudioTranscription.java             # 转写实体
│   └── AudioSegment.java                   # 分段实体
├── dto/
│   └── AudioTranscriptionQueryDTO.java     # 查询DTO
├── vo/
│   ├── AudioTranscriptionVO.java           # 返回VO
│   └── AudioSegmentVO.java                 # 分段VO
└── convert/
    └── AudioConvert.java                   # 对象转换
```

## 四、数据库设计

### 4.1 表结构

#### audio_transcription（转写主表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| file_name | VARCHAR(255) | 原始文件名 |
| file_path | VARCHAR(500) | 文件存储路径 |
| language | VARCHAR(20) | 语言代码（zh/en） |
| full_text | TEXT | 完整转写文本 |
| duration | DOUBLE | 音频时长(秒) |
| model_name | VARCHAR(50) | 使用的模型名称 |
| accuracy | FLOAT | 准确率评分 |
| status | INT | 状态(1-成功, 0-失败) |
| error_message | VARCHAR(500) | 错误信息 |
| created_by | BIGINT | 创建人ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### audio_segment（分段表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| transcription_id | BIGINT | 转写ID |
| start_time | DOUBLE | 开始时间(秒) |
| end_time | DOUBLE | 结束时间(秒) |
| text | TEXT | 分段文本 |
| confidence | FLOAT | 置信度 |
| created_at | DATETIME | 创建时间 |

### 4.2 创建表语句

执行 `db/audio_transcription.sql` 文件：

```bash
mysql -u root -proot rx_admin < db/audio_transcription.sql
```

## 五、配置说明

### 5.1 application.yml 配置

```yaml
app:
  audio:
    whisper-path: ${AUDIO_WHISPER_PATH:whisper}              # Whisper可执行文件路径
    model-path: ${AUDIO_MODEL_PATH:/opt/whisper/models}      # 模型文件存储路径
    default-model: ${AUDIO_DEFAULT_MODEL:small}              # 默认模型(tiny/base/small/medium/large-v3)
    default-language: ${AUDIO_DEFAULT_LANGUAGE:zh}           # 默认语言(zh/en/ja/ko)
    temp-dir: ${AUDIO_TEMP_DIR:/tmp/audio}                   # 临时文件目录
    max-file-size-mb: ${AUDIO_MAX_FILE_SIZE_MB:100}          # 最大文件大小(MB)
    enabled: ${AUDIO_ENABLED:true}                           # 是否启用
```

#### Windows 配置示例

```yaml
app:
  audio:
    whisper-path: ${AUDIO_WHISPER_PATH:D:\vueprojects\ffmpeg\bin\whisper.exe}    # whisper.cpp
    model-path: ${AUDIO_MODEL_PATH:D:\whisper\models}        # Windows 模型路径
    default-model: ${AUDIO_DEFAULT_MODEL:small}
    default-language: ${AUDIO_DEFAULT_LANGUAGE:zh}
    temp-dir: ${AUDIO_TEMP_DIR:D:\temp\audio}                # Windows 临时目录
    max-file-size-mb: ${AUDIO_MAX_FILE_SIZE_MB:100}
    enabled: ${AUDIO_ENABLED:true}
```

#### Linux/macOS 配置示例

```yaml
app:
  audio:
    whisper-path: ${AUDIO_WHISPER_PATH:whisper}              # whisper.cpp
    model-path: ${AUDIO_MODEL_PATH:/opt/whisper/models}
    default-model: ${AUDIO_DEFAULT_MODEL:small}
    default-language: ${AUDIO_DEFAULT_LANGUAGE:zh}
    temp-dir: ${AUDIO_TEMP_DIR:/tmp/audio}
    max-file-size-mb: ${AUDIO_MAX_FILE_SIZE_MB:100}
    enabled: ${AUDIO_ENABLED:true}
```

### 5.2 环境变量覆盖

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| AUDIO_WHISPER_PATH | whisper | Whisper可执行文件路径 |
| AUDIO_MODEL_PATH | /opt/whisper/models | 模型文件路径 |
| AUDIO_DEFAULT_MODEL | small | 默认模型 |
| AUDIO_DEFAULT_LANGUAGE | zh | 默认语言 |
| AUDIO_TEMP_DIR | /tmp/audio | 临时目录 |
| AUDIO_MAX_FILE_SIZE_MB | 100 | 最大文件大小 |
| AUDIO_ENABLED | true | 是否启用 |

## 六、部署步骤

### 6.1 安装 FFmpeg

#### Windows（推荐使用包管理器）

**方式一：使用 Chocolatey**
```powershell
# 1. 安装 Chocolatey（如果未安装）
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 2. 安装 FFmpeg
choco install ffmpeg -y
```

**方式二：使用 Scoop**
```powershell
# 1. 安装 Scoop（如果未安装）
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression

# 2. 安装 FFmpeg
scoop install ffmpeg
```

**方式三：手动安装**
```powershell
# 1. 下载 FFmpeg
# 访问 https://ffmpeg.org/download.html 下载 64位版本
# 或直接下载：https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip

# 2. 解压到固定目录，例如 D:\vueprojects\ffmpeg

# 3. 添加到系统 PATH
# 右键"此电脑" → 属性 → 高级系统设置 → 环境变量
# 系统变量 → Path → 编辑 → 新建 → 输入 D:\vueprojects\ffmpeg\bin

# 4. 使用 PowerShell 永久添加 PATH（管理员权限）
$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
if ($currentPath -notlike "*ffmpeg*") {
    $newPath = $currentPath + ";D:\vueprojects\ffmpeg\bin"
    [Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
    Write-Host "PATH 添加成功！"
}

# 5. 验证安装
ffmpeg -version
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update && sudo apt install ffmpeg -y
```

**macOS:**
```bash
brew install ffmpeg
```

### 6.2 安装 Whisper

#### 方式一：whisper.cpp（推荐，性能更好）

**Windows（使用预编译二进制文件）:**
```powershell
# 1. 下载预编译二进制文件
# 访问：https://github.com/ggerganov/whisper.cpp/releases
# 下载：whisper-bin-x64.zip

# 2. 解压到指定目录，例如 D:\whisper

# 3. 将 main.exe 复制为 whisper.exe 并放到系统路径
Copy-Item "D:\whisper\main.exe" "D:\vueprojects\ffmpeg\bin\whisper.exe" -Force

# 4. 复制所有 DLL 文件到系统路径
Copy-Item "D:\whisper\*.dll" "D:\vueprojects\ffmpeg\bin\" -Force

# 5. 验证
& "D:\vueprojects\ffmpeg\bin\whisper.exe" --help
```

**Windows（编译源码方式）:**
```powershell
# 1. 克隆仓库
git clone https://github.com/ggerganov/whisper.cpp.git
cd whisper.cpp

# 2. 编译（需要 CMake 和 Visual Studio Build Tools）
# 如果没有 CMake，先安装：choco install cmake
cmake -B build
cmake --build build --config Release

# 3. 复制可执行文件到系统路径
Copy-Item .\build\Release\main.exe D:\vueprojects\ffmpeg\bin\whisper.exe

# 4. 验证
whisper --version
```

**Linux:**
```bash
# 克隆仓库
git clone https://github.com/ggerganov/whisper.cpp.git
cd whisper.cpp

# 编译
make

# 复制可执行文件到系统路径
sudo cp ./main /usr/local/bin/whisper

# 验证
whisper --version
```

**macOS:**
```bash
# 克隆仓库
git clone https://github.com/ggerganov/whisper.cpp.git
cd whisper.cpp

# 编译
make

# 复制可执行文件到系统路径
sudo cp ./main /usr/local/bin/whisper

# 验证
whisper --version
```

#### 方式二：Python 版本（开发调试，安装简单）

```powershell
# 安装 Python 版本
pip install openai-whisper

# 验证
python -m whisper --version
```

**注意**：使用 Python 版本时，需要修改 `application.yml` 配置：
```yaml
app:
  audio:
    whisper-path: python -m whisper    # 使用 Python 模块
```

**Windows 路径配置：**
```yaml
app:
  audio:
    whisper-path: python -m whisper
    model-path: D:\whisper\models      # Windows 路径
    temp-dir: D:\temp\audio            # Windows 临时目录
```

### 6.3 下载模型文件

**Windows:**
```powershell
# 1. 创建模型目录
New-Item -ItemType Directory -Path "D:\whisper\models" -Force
cd D:\whisper\models

# 2. 下载 small 模型（推荐）
# 使用浏览器下载：https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin
# 或使用 PowerShell：
Invoke-WebRequest -Uri "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin" -OutFile "ggml-small.bin"

# 3. 可选：下载其他模型
# Invoke-WebRequest -Uri "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin" -OutFile "ggml-base.bin"
# Invoke-WebRequest -Uri "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin" -OutFile "ggml-medium.bin"
# Invoke-WebRequest -Uri "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.bin" -OutFile "ggml-large-v3.bin"
```

**Linux:**
```bash
# 创建模型目录
mkdir -p /opt/whisper/models
cd /opt/whisper/models

# 下载 small 模型（推荐）
wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin

# 可选：下载其他模型
# wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin
# wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin
# wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.bin
```

**macOS:**
```bash
# 创建模型目录
mkdir -p /opt/whisper/models
cd /opt/whisper/models

# 下载 small 模型（推荐）
curl -L -o ggml-small.bin https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin

# 可选：下载其他模型
# curl -L -o ggml-base.bin https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin
# curl -L -o ggml-medium.bin https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin
# curl -L -o ggml-large-v3.bin https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.bin
```

### 6.4 模型选择指南

| 模型 | 参数 | 大小 | 速度 | 准确率 | 推荐场景 |
|------|------|------|------|--------|---------|
| tiny | ~39M | 150MB | 最快 | 一般 | 快速转写 |
| base | ~74M | 290MB | 快 | 良好 | 日常使用 |
| **small** | ~244M | 960MB | 中等 | **优秀** | **推荐** |
| medium | ~769M | 3.0GB | 慢 | 很高 | 高精度需求 |
| large-v3 | ~1.5B | 6.1GB | 很慢 | 最佳 | 生产环境 |

## 七、API 接口

### 7.1 上传音频并转写

```
POST /api/v1/audio/transcription/upload
```

**参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | 音频文件 |
| language | String | 否 | 语言代码(zh/en) |

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "fileName": "test.mp3",
    "language": "zh",
    "fullText": "你好，这是测试音频。",
    "duration": 5.2,
    "modelName": "small",
    "status": 1,
    "segments": [
      {
        "startTime": 0.0,
        "endTime": 2.5,
        "text": "你好"
      }
    ]
  }
}
```

### 7.2 分页查询转写记录

```
GET /api/v1/audio/transcription/page
```

**参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认10 |
| keyword | String | 否 | 关键词搜索 |
| language | String | 否 | 语言筛选 |
| status | Integer | 否 | 状态筛选 |

### 7.3 获取转写详情

```
GET /api/v1/audio/transcription/{id}
```

### 7.4 删除转写记录

```
DELETE /api/v1/audio/transcription/{id}
```

## 八、权限配置

| 权限标识 | 说明 |
|---------|------|
| audio:transcription:upload | 上传转写 |
| audio:transcription:list | 列表查询 |
| audio:transcription:view | 查看详情 |
| audio:transcription:delete | 删除记录 |

## 九、前端页面

前端页面位于 `ui/src/views/audio/transcription/index.vue`

**功能:**
- 文件上传（支持拖拽）
- 列表展示转写记录
- 语言筛选
- 查看转写详情（包含分段时间戳）
- 批量删除

## 十、菜单注册

需要在 `sys_menu` 表中注册菜单：

```sql
INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, status, menu_type)
VALUES ('音频转写', 24, 'audio/transcription', 'audio/transcription/index', 'Audio', 50, 1, 'C');
```

## 十一、注意事项

1. **模型文件较大**：确保服务器有足够存储空间
2. **转写耗时**：音频文件转写可能需要较长时间，建议异步处理
3. **内存占用**：large 模型可能占用大量内存，根据服务器配置选择合适模型
4. **音频格式**：支持 MP3/WAV/OGG/FLAC/AAC 等格式，系统会自动转换为 WAV
5. **文件大小限制**：默认最大 100MB，可通过配置调整
