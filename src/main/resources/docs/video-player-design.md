# 视频播放功能设计方案

## 1. 需求概述

实现一个视频播放模块，支持：
- 播放本地视频文件（扫描服务器指定文件夹）
- 粘贴视频网址播放（mp4、m3u8 等）
- 播放历史记录与统计

## 2. 技术选型

### 2.1 前端播放器：ArtPlayer

| 对比项 | ArtPlayer | DPlayer | video.js | Plyr |
|--------|-----------|---------|----------|------|
| 体积 | ~40KB | ~60KB | ~200KB | ~30KB |
| HLS 支持 | 原生 | 需 hls.js | 插件 | 插件 |
| 维护状态 | 活跃 | 停更 | 活跃 | 活跃 |
| 中文文档 | 完善 | 一般 | 一般 | 一般 |
| 自定义 UI | 强 | 一般 | 强 | 一般 |

**选择理由**：体积小、原生支持 HLS/DASH、API 简洁、中文社区活跃。

### 2.2 HLS 兼容：hls.js

- 用于在不原生支持 HLS 的浏览器中播放 m3u8 视频流
- ArtPlayer 通过 `artplayer-plugin-playurl` 或手动集成 hls.js 实现

### 2.3 后端：参考音乐播放器模块

- 扫描本地文件夹 → 入库 → 流式传输（HTTP Range）
- 与音乐播放器保持一致的架构模式

## 3. 系统架构

```
┌─────────────────────────────────────────────────────┐
│                    前端 (Vue 3)                       │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │ 视频列表面板  │  │  ArtPlayer   │  │ URL 输入框  │  │
│  │ (本地扫描)   │  │  播放器实例   │  │ (粘贴网址)  │  │
│  └──────┬──────┘  └──────┬───────┘  └─────┬──────┘  │
│         │                │                │          │
│         └────────┬───────┴────────────────┘          │
│                  │ REST API                          │
└──────────────────┼───────────────────────────────────┘
                   │
┌──────────────────┼───────────────────────────────────┐
│            后端 (Spring Boot)                         │
│  ┌───────────────┴──────────────┐                    │
│  │   VideoPlayerController      │                    │
│  │   /api/v1/video/player/*     │                    │
│  └───────────────┬──────────────┘                    │
│                  │                                   │
│  ┌───────────────┴──────────────┐                    │
│  │   VideoPlayerService         │                    │
│  │   - scanVideoFolder()        │                    │
│  │   - streamVideo() (Range)    │                    │
│  │   - recordPlay()             │                    │
│  └───────────────┬──────────────┘                    │
│                  │                                   │
│  ┌───────────────┴──────────────┐                    │
│  │   MyBatis-Plus Mapper        │                    │
│  │   video_file / video_play_record │                │
│  └──────────────────────────────┘                    │
└──────────────────────────────────────────────────────┘
```

## 4. 数据库设计

### 4.1 video_file 表（视频文件）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| title | VARCHAR(255) | 视频标题 |
| file_name | VARCHAR(255) | 文件名 |
| file_path | VARCHAR(500) | 文件路径 |
| file_size | BIGINT | 文件大小(字节) |
| duration | INT | 时长(秒) |
| width | INT | 分辨率宽 |
| height | INT | 分辨率高 |
| video_type | VARCHAR(20) | 格式(mp4/webm/ogg/mkv等) |
| play_count | INT DEFAULT 0 | 播放次数 |
| last_play_time | DATETIME | 最后播放时间 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT DEFAULT 0 | 逻辑删除 |

### 4.2 video_play_record 表（播放记录）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| video_id | BIGINT | 视频ID |
| video_title | VARCHAR(255) | 视频标题(冗余) |
| username | VARCHAR(50) | 播放用户 |
| played_seconds | INT | 播放时长(秒) |
| create_time | DATETIME | 创建时间 |

## 5. API 设计

| Method | Path | 说明 | 权限 |
|--------|------|------|------|
| POST | `/video/player/scan` | 扫描本地视频文件夹 | `video:player:scan` |
| GET | `/video/player/list` | 获取视频列表(分页+搜索) | `video:player:list` |
| GET | `/video/player/{id}` | 获取视频详情 | `video:player:list` |
| GET | `/video/player/stream/{id}` | 流式播放视频(Range) | `video:player:list` |
| POST | `/video/player/record` | 记录播放 | `video:player:list` |
| GET | `/video/player/stats` | 播放统计 | `video:player:list` |
| GET | `/video/player/recent` | 最近播放记录 | `video:player:list` |
| DELETE | `/video/player/{id}` | 删除视频记录 | `video:player:delete` |

## 6. 前端页面设计

### 6.1 布局

```
┌──────────────────────────────────────────────────┐
│  🎬 视频播放    [搜索框]    [扫描] [统计]         │
├──────────────────┬───────────────────────────────┤
│  视频列表         │                               │
│  ┌────────────┐  │                               │
│  │ 视频1 ▶    │  │     ArtPlayer 播放器           │
│  ├────────────┤  │     (支持全屏/倍速/进度)       │
│  │ 视频2      │  │                               │
│  ├────────────┤  │                               │
│  │ ...        │  │                               │
│  └────────────┘  │                               │
│                  ├───────────────────────────────┤
│  ┌────────────┐  │  视频信息 / URL播放输入框      │
│  │ URL播放    │  │                               │
│  │ [输入框]   │  │                               │
│  │ [播放]     │  │                               │
│  └────────────┘  │                               │
└──────────────────┴───────────────────────────────┘
```

### 6.2 核心功能

1. **本地视频列表**：扫描服务器文件夹，展示视频列表
2. **URL 播放**：输入框粘贴视频网址（mp4/m3u8 等）
3. **ArtPlayer 播放器**：播放/暂停/进度/音量/全屏/倍速
4. **播放历史**：记录播放历史，支持续播
5. **统计面板**：总视频数/总播放次数/今日播放

## 7. 关键技术实现

### 7.1 视频流式传输（Range 请求）

参考 `MusicController.streamMusic()` 实现：
- 支持 HTTP Range 请求（206 Partial Content）
- 8KB 缓冲区流式读取，避免大文件内存溢出
- 正确设置 `Content-Type`、`Accept-Ranges`、`Content-Range` 头

### 7.2 HLS 播放

- ArtPlayer 原生支持 HLS（通过 hls.js）
- 前端检测 URL 是否为 m3u8 格式，自动启用 hls.js

### 7.3 URL 播放

- 前端直接将 URL 传给 ArtPlayer 的 `url` 属性
- 无需后端中转，由浏览器直接请求视频资源

## 8. 配置项

```yaml
# application.yml
video:
  folder: C:/Users/admin/Downloads/video
  supported-formats: mp4,webm,ogg,mkv,avi,flv,mov
```

## 9. 新增菜单 SQL

```sql
-- 父菜单ID=24 对应"系统工具"
INSERT INTO sys_menu (menu_name, parent_id, menu_type, path, component, perms, icon, sort, status, visible)
VALUES ('视频播放', 24, 2, '/tool/videoPlayer', 'video/player/index', 'video:player:list', 'fa-solid fa-video', 45, 1, 1);
```

## 10. 文件清单

### 后端

```
src/main/java/com/rx/admin/modules/video/player/
  controller/VideoPlayerController.java
  service/VideoPlayerService.java
  service/impl/VideoPlayerServiceImpl.java
  entity/VideoFile.java
  entity/VideoPlayRecord.java
  mapper/VideoFileMapper.java
  mapper/VideoPlayRecordMapper.java
  convert/VideoPlayerConvert.java
  convert/VideoPlayRecordConvert.java
  vo/VideoFileVO.java
  vo/VideoPlayRecordVO.java
  vo/VideoPlayStatsVO.java
```

### 前端

```
ui/src/views/video/player/index.vue          -- 主页面
ui/src/api/video.js                          -- API 模块
ui/src/api/routes.js                         -- 路由常量(追加)
ui/src/router/componentMap.js                -- 组件注册(追加)
ui/src/composables/useMenuI18n.js            -- 菜单翻译(追加)
ui/src/i18n/lang/zh-CN.js                    -- 中文翻译(追加)
ui/src/i18n/lang/en-US.js                    -- 英文翻译(追加)
ui/vite.config.js                            -- manualChunks(追加)
ui/package.json                              -- 依赖(追加)
```

### 数据库

```
db/video_player.sql                          -- 建表+菜单+权限
```

### 配置

```
src/main/resources/application.yml           -- video.folder 配置
```
