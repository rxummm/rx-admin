package com.rx.admin.common.constant;

public class PermissionConstants {

    private PermissionConstants() {
    }

    public static final String SYSTEM_MODULE = "sys";

    // ==================== 系统模块 ====================
    public static class Menu {
        public static final String QUERY = "sys:menu:query";
        public static final String ADD = "sys:menu:add";
        public static final String EDIT = "sys:menu:edit";
        public static final String DELETE = "sys:menu:delete";
    }

    public static class User {
        public static final String QUERY = "sys:user:query";
        public static final String ADD = "sys:user:add";
        public static final String EDIT = "sys:user:edit";
        public static final String DELETE = "sys:user:delete";
        public static final String RESET_PWD = "sys:user:resetPwd";
        public static final String EXPORT = "sys:user:export";
    }

    public static class Role {
        public static final String QUERY = "sys:role:query";
        public static final String ADD = "sys:role:add";
        public static final String EDIT = "sys:role:edit";
        public static final String DELETE = "sys:role:delete";
        public static final String ASSIGN = "sys:role:assign";
    }

    public static class Dept {
        public static final String QUERY = "sys:dept:query";
        public static final String ADD = "sys:dept:add";
        public static final String EDIT = "sys:dept:edit";
        public static final String DELETE = "sys:dept:delete";
    }

    public static class Config {
        public static final String QUERY = "sys:config:query";
        public static final String ADD = "sys:config:add";
        public static final String EDIT = "sys:config:edit";
        public static final String DELETE = "sys:config:delete";
    }

    public static class Dict {
        public static final String QUERY = "sys:dict:query";
        public static final String ADD = "sys:dict:add";
        public static final String EDIT = "sys:dict:edit";
        public static final String DELETE = "sys:dict:delete";
    }

    public static class Log {
        public static final String QUERY = "sys:log:query";
        public static final String DELETE = "sys:log:delete";
    }

    // ==================== 监控模块 ====================
    public static class Monitor {
        public static final String ONLINE_QUERY = "monitor:online:query";
        public static final String ONLINE_KICK = "monitor:online:kick";
        public static final String LOG_ANALYSIS_LIST = "monitor:log-analysis:list";
        public static final String LOG_QUERY = "monitor:log:query";
        public static final String LOG_DELETE = "monitor:log:delete";
        public static final String LOGIN_LOG_QUERY = "monitor:loginLog:query";
        public static final String LOGIN_LOG_DELETE = "monitor:loginLog:delete";
        public static final String JOB_QUERY = "monitor:job:query";
        public static final String JOB_ADD = "monitor:job:add";
        public static final String JOB_EDIT = "monitor:job:edit";
        public static final String JOB_DELETE = "monitor:job:delete";
        public static final String JOB_RUN = "monitor:job:run";
        public static final String JOB_LOG_QUERY = "monitor:job-log:query";
        public static final String JOB_LOG_DELETE = "monitor:job-log:delete";
        public static final String HEALTH_QUERY = "monitor:health:query";
        public static final String CACHE_QUERY = "monitor:cache:query";
        public static final String CACHE_DELETE = "monitor:cache:delete";
        public static final String DASHBOARD_QUERY = "monitor:dashboard:query";
        public static final String NOTIFICATION_QUERY = "monitor:notification:query";
        public static final String SLOW_QUERY_QUERY = "monitor:slow-query:query";
        public static final String SLOW_QUERY_DELETE = "monitor:slow-query:delete";
        public static final String EXPORT_LOG_QUERY = "monitor:export-log:query";
    }

    // ==================== 内容模块 ====================
    public static class Content {
        public static final String NOTICE_QUERY = "content:notice:query";
        public static final String NOTICE_ADD = "content:notice:add";
        public static final String NOTICE_EDIT = "content:notice:edit";
        public static final String NOTICE_DELETE = "content:notice:delete";
        public static final String MESSAGE_QUERY = "content:message:query";
        public static final String MESSAGE_SEND = "content:message:send";
        public static final String MESSAGE_DELETE = "content:message:delete";
        public static final String ANNOUNCEMENT_READ = "content:announcement:read";
    }

    // ==================== 经典文学模块 ====================
    public static class Xiyou {
        public static final String POEM_ADD = "classics:xiyou:poem:add";
        public static final String POEM_EDIT = "classics:xiyou:poem:edit";
        public static final String POEM_DELETE = "classics:xiyou:poem:delete";
        public static final String CHARACTER_ADD = "classics:xiyou:character:add";
        public static final String CHARACTER_EDIT = "classics:xiyou:character:edit";
        public static final String CHARACTER_DELETE = "classics:xiyou:character:delete";
        public static final String EVENT_ADD = "classics:xiyou:event:add";
        public static final String EVENT_EDIT = "classics:xiyou:event:edit";
        public static final String EVENT_DELETE = "classics:xiyou:event:delete";
    }

    public static class Shuihu {
        public static final String CHAPTER_ADD = "classics:shuihu:chapter:add";
        public static final String CHAPTER_EDIT = "classics:shuihu:chapter:edit";
        public static final String CHAPTER_DELETE = "classics:shuihu:chapter:delete";
        public static final String POEM_ADD = "classics:shuihu:poem:add";
        public static final String POEM_EDIT = "classics:shuihu:poem:edit";
        public static final String POEM_DELETE = "classics:shuihu:poem:delete";
    }

    public static class Sanguo {
        public static final String POEM_ADD = "classics:sanguo:poem:add";
        public static final String POEM_EDIT = "classics:sanguo:poem:edit";
        public static final String POEM_DELETE = "classics:sanguo:poem:delete";
        public static final String CHARACTER_ADD = "classics:sanguo:character:add";
        public static final String CHARACTER_EDIT = "classics:sanguo:character:edit";
        public static final String CHARACTER_DELETE = "classics:sanguo:character:delete";
    }

    public static class Honglou {
        public static final String POEM_ADD = "classics:honglou:poem:add";
        public static final String POEM_EDIT = "classics:honglou:poem:edit";
        public static final String POEM_DELETE = "classics:honglou:poem:delete";
        public static final String CHARACTER_ADD = "classics:honglou:character:add";
        public static final String CHARACTER_EDIT = "classics:honglou:character:edit";
        public static final String CHARACTER_DELETE = "classics:honglou:character:delete";
        public static final String RELATION_ADD = "classics:honglou:relation:add";
        public static final String RELATION_EDIT = "classics:honglou:relation:edit";
        public static final String RELATION_DELETE = "classics:honglou:relation:delete";
    }

    // ==================== 工具模块 ====================
    public static class Tool {
        public static final String GEN_QUERY = "tool:gen:query";
        public static final String GEN_ADD = "tool:gen:add";
        public static final String GEN_EDIT = "tool:gen:edit";
        public static final String GEN_DELETE = "tool:gen:delete";
        public static final String GEN_EXECUTE = "tool:gen:execute";
        public static final String BACKUP_QUERY = "tool:backup:query";
        public static final String BACKUP_EXECUTE = "tool:backup:execute";
        public static final String BACKUP_DELETE = "tool:backup:delete";
        public static final String DB_TOOL_QUERY = "tool:dbTool:query";
        public static final String DB_TOOL_EXECUTE = "tool:dbTool:execute";
        public static final String DB_CONSOLE_EXECUTE = "tool:db-console:execute";
        public static final String IMPORT_LIST = "tool:import:list";
        public static final String API_DEBUG_LIST = "tool:api-debug:list";
        public static final String REGION_QUERY = "tool:region:query";
        public static final String REGION_ADD = "tool:region:add";
        public static final String REGION_EDIT = "tool:region:edit";
        public static final String REGION_DELETE = "tool:region:delete";
    }

    // ==================== 视频模块 ====================
    public static class Video {
        public static final String PLAYER_SCAN = "video:player:scan";
        public static final String PLAYER_LIST = "video:player:list";
        public static final String PLAYER_VIEW = "video:player:view";
    }

    // ==================== AS400 模块 ====================
    public static class As400 {
        public static final String OBJECTS_QUERY = "as400:objects:query";
        public static final String ISERVICE_QUERY = "as400:iservice:query";
    }

    // ==================== 系统权限管理 ====================
    public static class Permission {
        public static final String MANAGE = "system:permission:manage";
    }

    // ==================== IP 规则模块 ====================
    public static class IpRule {
        public static final String LIST = "system:ip-rule:list";
        public static final String ADD = "system:ip-rule:add";
        public static final String EDIT = "system:ip-rule:edit";
        public static final String DELETE = "system:ip-rule:delete";
    }

    // ==================== 技术博客模块 ====================
    public static class TechBlog {
        public static final String QUERY = "techblog:query";
        public static final String SYNC = "techblog:sync";
        public static final String ADD = "techblog:add";
        public static final String EDIT = "techblog:edit";
        public static final String DELETE = "techblog:delete";
        public static final String BATCH_DELETE = "techblog:batchDelete";
    }

    public static class Backup {
        public static final String LIST = "tool:backup:list";
    }

    public static class Literature {
        public static final String AUTHOR_ADD = "classics:literature:author:add";
        public static final String AUTHOR_EDIT = "classics:literature:author:edit";
        public static final String AUTHOR_DELETE = "classics:literature:author:delete";
        public static final String DYNASTY_ADD = "classics:literature:dynasty:add";
        public static final String DYNASTY_EDIT = "classics:literature:dynasty:edit";
        public static final String DYNASTY_DELETE = "classics:literature:dynasty:delete";
        public static final String GENRE_ADD = "classics:literature:genre:add";
        public static final String GENRE_EDIT = "classics:literature:genre:edit";
        public static final String GENRE_DELETE = "classics:literature:genre:delete";
        public static final String CATEGORY_ADD = "classics:literature:category:add";
        public static final String CATEGORY_EDIT = "classics:literature:category:edit";
        public static final String CATEGORY_DELETE = "classics:literature:category:delete";
        public static final String WORK_ADD = "classics:literature:work:add";
        public static final String WORK_EDIT = "classics:literature:work:edit";
        public static final String WORK_DELETE = "classics:literature:work:delete";
    }

    // ==================== 音频转写模块 ====================
    public static class AudioTranscription {
        public static final String UPLOAD = "audio:transcription:upload";
        public static final String LIST = "audio:transcription:list";
        public static final String VIEW = "audio:transcription:view";
        public static final String UPDATE = "audio:transcription:update";
        public static final String DELETE = "audio:transcription:delete";
    }

    // ==================== 视频转写模块 ====================
    public static class VideoTranscription {
        public static final String UPLOAD = "video:transcription:upload";
        public static final String LIST = "video:transcription:list";
        public static final String VIEW = "video:transcription:view";
        public static final String UPDATE = "video:transcription:update";
        public static final String DELETE = "video:transcription:delete";
    }

    // ==================== OCR 文档识别模块 ====================
    public static class OcrRecognition {
        public static final String RECOGNIZE = "ocr:recognition:recognize";
        public static final String LIST = "ocr:recognition:list";
        public static final String VIEW = "ocr:recognition:view";
        public static final String DELETE = "ocr:recognition:delete";
    }

    // ==================== 日历管理模块 ====================
    public static class Calendar {
        public static final String LIST = "tool:calendar:list";
        public static final String ADD = "tool:calendar:add";
        public static final String EDIT = "tool:calendar:edit";
        public static final String DELETE = "tool:calendar:delete";
    }
}
