/**
 * API 路由常量表
 *
 * 所有后端 API 端点集中定义在此文件，统一管理。
 * 静态路由用纯字符串，动态路由用箭头函数。
 *
 * 用法：import { API } from './routes'
 *       request({ url: API.SYS.USER.PAGE, method: 'get', params })
 *       request({ url: API.SYS.USER.BY_ID(id), method: 'get' })
 */
export const API = {
  // ==================== 认证 ====================
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    USER_INFO: '/auth/user-info',
    ROUTERS: '/auth/routers',
    CAPTCHA: '/auth/captcha',
    UPDATE_PROFILE: '/auth/update-profile',
    PING: '/auth/ping'
  },

  // ==================== 系统管理 ====================
  SYS: {
    USER: {
      PAGE: '/sys/user/page',
      CRUD: '/sys/user',
      BY_ID: (id) => `/sys/user/${id}`
    },
    ROLE: {
      LIST: '/sys/role/list',
      CRUD: '/sys/role',
      BY_ID: (id) => `/sys/role/${id}`
    },
    MENU: {
      TREE: '/sys/menu/tree',
      CRUD: '/sys/menu',
      BY_ID: (id) => `/sys/menu/${id}`,
      REQUESTABLE: '/sys/menu/requestable'
    },
    DEPT: {
      TREE: '/sys/dept/tree',
      CRUD: '/sys/dept',
      BY_ID: (id) => `/sys/dept/${id}`
    },
    DICT: {
      TYPE: {
        PAGE: '/sys/dict/type/page',
        CRUD: '/sys/dict/type',
        BY_ID: (id) => `/sys/dict/type/${id}`
      },
      DATA: {
        LIST_BY_TYPE: (typeId) => `/sys/dict/data/list/${typeId}`,
        CRUD: '/sys/dict/data',
        BY_ID: (id) => `/sys/dict/data/${id}`
      }
    },
    CONFIG: {
      GROUPED: '/system/config/grouped',
      VALUES: '/system/config/values',
      VALUE_BY_KEY: (key) => `/system/config/value/${key}`,
      CRUD: '/system/config',
      BY_ID: (id) => `/system/config/${id}`
    },
    FILE: {
      PAGE: '/sys/file/page',
      UPLOAD: '/sys/file/upload',
      BY_ID: (id) => `/sys/file/${id}`,
      BATCH: '/sys/file/batch',
      DOWNLOAD: (id) => `/api/v1/sys/file/download/${id}`
    },
    NOTIFICATION: {
      STREAM: '/api/v1/notification/stream'
    },
    PERMISSION_REQUEST: {
      CRUD: '/sys/permission-request',
      PENDING: '/sys/permission-request/pending',
      MY: '/sys/permission-request/my',
      APPROVE: (id) => `/sys/permission-request/${id}/approve`,
      REJECT: (id, data) => ({ url: `/sys/permission-request/${id}/reject`, data }),
      EMAIL_REQUEST: '/sys/permission-request/email-request'
    },
    PERMISSION_MANAGE: {
      USER_MENUS: (userId) => `/sys/permission-manage/user/${userId}/menus`,
      MANAGEABLE_TREE: (userId) => `/sys/permission-manage/user/${userId}/manageable-tree`,
      ADD: (userId) => `/sys/permission-manage/user/${userId}/add`,
      REMOVE: (userId) => `/sys/permission-manage/user/${userId}/remove`,
      SET: (userId) => `/sys/permission-manage/user/${userId}/set`
    }
  },

  // ==================== 监控 ====================
  MONITOR: {
    LOG: {
      PAGE: '/monitor/log/page',
      BY_ID: (id) => `/monitor/log/${id}`,
      BATCH: '/monitor/log/batch'
    },
    LOGIN_LOG: {
      PAGE: '/monitor/login-log/page',
      BY_ID: (id) => `/monitor/login-log/${id}`,
      BATCH: '/monitor/login-log/batch'
    },
    ONLINE: {
      LIST: '/monitor/online/list',
      KICK: (token) => `/monitor/online/${token}`
    },
    JOB: {
      PAGE: '/monitor/job/page',
      CRUD: '/monitor/job',
      BY_ID: (id) => `/monitor/job/${id}`,
      TOGGLE_STATUS: (id) => `/monitor/job/status/${id}`,
      RUN_ONCE: (id) => `/monitor/job/run/${id}`
    },
    JOB_LOG: {
      PAGE: '/monitor/job-log/page',
      BY_ID: (id) => `/monitor/job-log/${id}`,
      BATCH: '/monitor/job-log/batch'
    },
    SLOW_QUERY: {
      PAGE: '/monitor/slow-query/page',
      BY_ID: (id) => `/monitor/slow-query/${id}`,
      BATCH: '/monitor/slow-query/batch',
      CLEAR: '/monitor/slow-query/clear'
    },
    HEALTH: {
      SYSTEM: '/monitor/health/system',
      GC: '/monitor/health/gc'
    },
    CACHE: {
      LIST: '/monitor/cache/list',
      CLEAR: (name) => `/monitor/cache/clear/${encodeURIComponent(name)}`,
      CLEAR_ALL: '/monitor/cache/clear-all'
    },
    EXPORT_LOG: {
      PAGE: '/monitor/export-log/page'
    },
    LOG_ANALYSIS: {
      SUMMARY: '/monitor/log-analysis/summary',
      HOURLY: '/monitor/log-analysis/hourly',
      TYPE_DISTRIBUTION: '/monitor/log-analysis/type-distribution',
      TREND: '/monitor/log-analysis/trend'
    }
  },

  // ==================== 工具 ====================
  TOOL: {
    GEN: {
      TABLES: '/tool/gen/tables',
      COLUMNS: '/tool/gen/columns',
      PREVIEW: '/tool/gen/preview',
      GENERATE: '/tool/gen/generate'
    },
    REGION: {
      PAGE: '/tool/region/page',
      CHILDREN: '/tool/region/children',
      SEARCH: '/tool/region/search',
      CRUD: '/tool/region',
      BY_ID: (id) => `/tool/region/${id}`
    },
    ANALYSIS: {
      MENUS: '/tool/analysis/menus',
      ANALYZE: '/tool/analysis/analyze',
      SEARCH: '/tool/analysis/search'
    },
    DEV: {
      JSON_FORMAT: '/tool/dev/json-format',
      UUID: '/tool/dev/uuid',
      TIMESTAMP: '/tool/dev/timestamp'
    },
    DATABASE: {
      EXECUTE: '/tool/database/execute',
      TABLES: '/tool/database/tables',
      TABLE_COLUMNS: (table) => `/tool/database/tables/${encodeURIComponent(table)}/columns`,
      POOL_STATUS: '/tool/database/pool-status'
    },
    BACKUP: {
      LIST: '/tool/backup/list',
      CREATE: '/tool/backup/create',
      BY_NAME: (name) => `/tool/backup/${name}`,
      DOWNLOAD: (name) => `/tool/backup/download/${name}`
    },
    API_DEBUG: {
      ENDPOINTS: '/tool/api-debug/endpoints'
    },
    COMMON: {
      EXCEL_PARSE: '/common-tools/excel/parse',
      DOCUMENT_UPLOAD: '/common-tools/document/upload',
      DOCUMENT_LIST: '/common-tools/document/list',
      DOCUMENT_BY_ID: (id) => `/common-tools/document/${id}`,
      DOCUMENT_DEFAULT_DIR: '/common-tools/document/default-dir',
      PDF_TO_WORD: '/common-tools/convert/pdf-to-word',
      WORD_TO_PDF: '/common-tools/convert/word-to-pdf',
      EMAIL_SEND: '/common-tools/email/send',
      EMAIL_UPLOAD: '/common-tools/email/upload-attachment',
      EMAIL_CONFIG: '/common-tools/email/config'
    },
    IMPORT: {
      ANALYZE: '/tool/import/analyze',
      EXECUTE: '/tool/import/execute'
    }
  },

  // ==================== 导出 ====================
  EXPORT: {
    CONFIG: '/export/config',
    EXCEL: '/export/excel',
    PDF: '/export/pdf'
  },

  // ==================== 仪表盘 ====================
  DASHBOARD: {
    STATS: '/dashboard/stats',
    LOGIN_STATS: '/dashboard/enhanced/login-stats',
    EXPORT_STATS: '/dashboard/enhanced/export-stats',
    OPERATION_TOP10: '/dashboard/enhanced/operation-top10'
  },

  // ==================== 内容管理 ====================
  CONTENT: {
    NOTICE: {
      PAGE: '/content/notice/page',
      SUMMARY: '/content/notice/summary',
      TODO_COUNT: '/content/notice/todo-count',
      CRUD: '/content/notice',
      BY_ID: (id) => `/content/notice/${id}`,
      READ_IDS: '/content/notice/read-ids',
      READ: (id) => `/content/notice/read/${id}`,
      READ_ALL: '/content/notice/read-all'
    },
    MESSAGE: {
      PAGE: '/content/message/page',
      UNREAD_COUNT: '/content/message/unread-count',
      MARK_READ: (id) => `/content/message/${id}/read`,
      MARK_ALL_READ: '/content/message/read-all',
      BY_ID: (id) => `/content/message/${id}`
    },
    ANNOUNCEMENT: {
      POPUP: '/content/announcement/popup',
      READ: (id) => `/content/announcement/read/${id}`
    }
  },

  // ==================== 通知中心 ====================
  NOTIFY_CENTER: {
    TEMPLATES: {
      PAGE: '/notify-center/templates/page',
      CRUD: '/notify-center/templates',
      BY_ID: (id) => `/notify-center/templates/${id}`
    },
    SEND: '/notify-center/send',
    RECORDS: {
      PAGE: '/notify-center/records/page',
      BY_ID: (id) => `/notify-center/records/${id}`,
      BATCH: '/notify-center/records/batch',
      RETRY: (id) => `/notify-center/records/${id}/retry`
    }
  },

  // ==================== 音频转写 ====================
  AUDIO: {
    TRANSCRIPTION: {
      PAGE: '/audio/transcription/page',
      UPLOAD_ONLY: '/audio/transcription/upload-only',
      UPLOAD: '/audio/transcription/upload',
      TRANSCRIBE: (id) => `/audio/transcription/${id}/transcribe`,
      BASE: '/audio/transcription',
      BY_ID: (id) => `/audio/transcription/${id}`,
      FILE_NAME: (id) => `/audio/transcription/${id}/file-name`,
      BATCH_DELETE: (ids) => `/audio/transcription/batch/${ids}`
    }
  },

  // ==================== 音乐 ====================
  MUSIC: {
    SCAN: '/music/scan',
    SONGS: '/music/songs',
    SONG_DETAIL: (id) => `/music/song/${id}`,
    PLAY: (id) => `/music/play/${id}`,
    STATS: '/music/stats',
    RECENT: '/music/recent',
    TOP: '/music/top',
    FOLDER: '/music/folder'
  },

  // ==================== 视频播放 ====================
  VIDEO: {
    TRANSCRIPTION: {
      PAGE: '/video/transcription/page',
      UPLOAD_ONLY: '/video/transcription/upload-only',
      TRANSCRIBE: (id) => `/video/transcription/${id}/transcribe`,
      BASE: '/video/transcription',
      BY_ID: (id) => `/video/transcription/${id}`,
      FILE_NAME: (id) => `/video/transcription/${id}/file-name`,
      BATCH_DELETE: (ids) => `/video/transcription/batch/${ids}`,
      SPEAKER_NAME: (id) => `/video/transcription/${id}/speaker-name`
    },
    PLAYER: {
      SCAN: '/video/player/scan',
      LIST: '/video/player/list',
      DETAIL: (id) => `/video/player/${id}`,
      RECORD: '/video/player/record',
      STATS: '/video/player/stats',
      RECENT: '/video/player/recent',
      DELETE: (id) => `/video/player/${id}`,
      FOLDER: '/video/player/folder',
      STREAM: (id) => `/api/v1/video/player/stream/${id}`
    }
  },

  // ==================== AS400 ====================
  AS400: {
    OBJECTS_BY_LIB: (lib) => `/as400/objects/${lib}`,
    OBJECTS: '/as400/objects',
    ANALYSIS: '/as400/analysis'
  },

  // ==================== IService ====================
  ISERVICE: {
    CATEGORIES: '/iservice/categories',
    CATEGORY_BY_CODE: (code) => `/iservice/categories/${code}`,
    ITEM_DETAIL: (id) => `/iservice/items/${id}`
  },

  // ==================== 技术博客 ====================
  TECH_BLOG: {
    ARTICLES: '/techblog/articles',
    ARTICLE_BY_ID: (id) => `/techblog/articles/${id}`,
    ARTICLES_BATCH: '/techblog/articles/batch',
    CATEGORIES: '/techblog/categories',
    RECENT: '/techblog/recent',
    FETCH: '/techblog/fetch',
    PROGRESS: '/techblog/progress'
  },

  // ==================== 系统工具 ====================
  SYSTEM: {
    FAVORITE: {
      LIST: '/system/favorite/list',
      CRUD: '/system/favorite',
      TOGGLE: '/system/favorite/toggle',
      BY_ID: (id) => `/system/favorite/${id}`,
      SORT: '/system/favorite/sort'
    },
    IP_RULE: {
      PAGE: '/system/ip-rule/page',
      BY_ID: (id) => `/system/ip-rule/${id}`,
      CRUD: '/system/ip-rule',
      MODE_GET: '/system/ip-rule/mode',
      MODE_SET: '/system/ip-rule/mode'
    }
  },

  // ==================== OCR 文档识别 ====================
  OCR: {
    RECOGNIZE: '/ocr/recognize',
    PAGE: '/ocr/page',
    BY_ID: (id) => `/ocr/${id}`,
    DOWNLOAD: (id) => `/ocr/${id}/download`,
    BATCH: '/ocr/batch'
  },

  // ==================== 日历管理 ====================
  CALENDAR: {
    PAGE: '/calendar/event/page',
    MONTH: '/calendar/event/month',
    RANGE: '/calendar/event/range',
    TODAY: '/calendar/event/today',
    BY_ID: (id) => `/calendar/event/${id}`,
    CRUD: '/calendar/event'
  },

  // ==================== 经典文学（四大名著 + 文学库） ====================
  CLASSICS: {
    HONGLOU: {
      POEM: {
        PAGE: '/classics/honglou/poem/page',
        CRUD: '/classics/honglou/poem',
        BY_ID: (id) => `/classics/honglou/poem/${id}`,
        BATCH: '/classics/honglou/poem/batch'
      },
      CHARACTER: {
        PAGE: '/classics/honglou/character/page',
        CRUD: '/classics/honglou/character',
        BY_ID: (id) => `/classics/honglou/character/${id}`,
        BY_ROLE: '/classics/honglou/character/role',
        BATCH: '/classics/honglou/character/batch',
        ALL: '/classics/honglou/character/all'
      },
      RELATION: {
        BY_CHARACTER: (id) => `/classics/honglou/relation/${id}`,
        CRUD: '/classics/honglou/relation',
        BY_ID: (id) => `/classics/honglou/relation/${id}`,
        ALL: '/classics/honglou/relation/all'
      }
    },
    SHUIHU: {
      POEM: {
        PAGE: '/classics/shuihu/poem/page',
        CRUD: '/classics/shuihu/poem',
        BY_ID: (id) => `/classics/shuihu/poem/${id}`,
        BATCH: '/classics/shuihu/poem/batch'
      },
      CHAPTER: {
        PAGE: '/classics/shuihu/chapter/page',
        CRUD: '/classics/shuihu/chapter',
        BY_ID: (id) => `/classics/shuihu/chapter/${id}`,
        BATCH: '/classics/shuihu/chapter/batch'
      }
    },
    XIYOU: {
      POEM: {
        PAGE: '/classics/xiyou/poem/page',
        CRUD: '/classics/xiyou/poem',
        BY_ID: (id) => `/classics/xiyou/poem/${id}`,
        BATCH: '/classics/xiyou/poem/batch'
      },
      CHARACTER: {
        PAGE: '/classics/xiyou/character/page',
        CRUD: '/classics/xiyou/character',
        BY_ID: (id) => `/classics/xiyou/character/${id}`,
        BY_RACE: '/classics/xiyou/character/race',
        BATCH: '/classics/xiyou/character/batch'
      },
      EVENT: {
        ALL: '/classics/xiyou/event/all',
        PAGE: '/classics/xiyou/event/page',
        CRUD: '/classics/xiyou/event',
        BY_ID: (id) => `/classics/xiyou/event/${id}`,
        BATCH: '/classics/xiyou/event/batch'
      }
    },
    SANGUO: {
      POEM: {
        PAGE: '/classics/sanguo/poem/page',
        CRUD: '/classics/sanguo/poem',
        BY_ID: (id) => `/classics/sanguo/poem/${id}`,
        BATCH: '/classics/sanguo/poem/batch'
      },
      CHARACTER: {
        PAGE: '/classics/sanguo/character/page',
        CRUD: '/classics/sanguo/character',
        BY_ID: (id) => `/classics/sanguo/character/${id}`,
        BY_COUNTRY: '/classics/sanguo/character/country',
        BATCH: '/classics/sanguo/character/batch'
      }
    },
    LITERATURE: {
      AUTHOR: {
        PAGE: '/classics/literature/author/page',
        BY_ID: (id) => `/classics/literature/author/${id}`,
        ALL: '/classics/literature/author/all',
        CRUD: '/classics/literature/author',
        BATCH: '/classics/literature/author/batch'
      },
      DYNASTY: {
        PAGE: '/classics/literature/dynasty/page',
        BY_ID: (id) => `/classics/literature/dynasty/${id}`,
        ALL: '/classics/literature/dynasty/all',
        CRUD: '/classics/literature/dynasty',
        BATCH: '/classics/literature/dynasty/batch'
      },
      GENRE: {
        PAGE: '/classics/literature/genre/page',
        BY_ID: (id) => `/classics/literature/genre/${id}`,
        ALL: '/classics/literature/genre/all',
        CRUD: '/classics/literature/genre',
        BATCH: '/classics/literature/genre/batch'
      },
      CATEGORY: {
        PAGE: '/classics/literature/category/page',
        BY_ID: (id) => `/classics/literature/category/${id}`,
        ALL: '/classics/literature/category/all',
        CRUD: '/classics/literature/category',
        BATCH: '/classics/literature/category/batch'
      },
      WORK: {
        PAGE: '/classics/literature/work/page',
        BY_ID: (id) => `/classics/literature/work/${id}`,
        ALL: '/classics/literature/work/all',
        CRUD: '/classics/literature/work',
        BATCH: '/classics/literature/work/batch'
      }
    }
  },

  // ==================== 工作流 ====================
  WORKFLOW: {
    DEFINITION: {
      PAGE: '/wf/definition/page',
      CRUD: '/wf/definition',
      BY_ID: (id) => `/wf/definition/${id}`
    },
    INSTANCE: {
      PAGE: '/wf/instance/page',
      START: '/wf/instance/start',
      CANCEL: (id) => `/wf/instance/${id}/cancel`,
      DELETE: (id) => `/wf/instance/${id}`
    },
    TASK: {
      PAGE: '/wf/task/page',
      MY: '/wf/task/my',
      APPROVE: '/wf/task/approve',
      TRANSFER: '/wf/task/transfer'
    }
  },

  // ==================== Webhook ====================
  WEBHOOK: {
    PAGE: '/tool/webhook/page',
    CRUD: '/tool/webhook',
    BY_ID: (id) => `/tool/webhook/${id}`,
    TOGGLE: (id) => `/tool/webhook/${id}/toggle`
  },

  // ==================== 数据归档 ====================
  ARCHIVE: {
    LIST: '/tool/archive/list',
    CRUD: '/tool/archive',
    BY_ID: (id) => `/tool/archive/${id}`
  },

  // ==================== 通知偏好 ====================
  NOTIFICATION_PREF: {
    LIST: '/system/notification-pref/list',
    UPDATE: '/system/notification-pref'
  },

  // ==================== API 密钥 ====================
  API_KEY: {
    LIST: '/tool/api-key/list',
    GENERATE: '/tool/api-key',
    DELETE: (id) => `/tool/api-key/${id}`,
    TOGGLE: (id) => `/tool/api-key/${id}/toggle`
  },

  // ==================== 邮件模板 ====================
  EMAIL_TEMPLATE: {
    LIST: '/tool/email-template/list',
    BY_ID: (id) => `/tool/email-template/${id}`,
    CRUD: '/tool/email-template'
  },

  // ==================== 数据版本 ====================
  DATA_VERSION: {
    PAGE: '/monitor/data-version/page'
  },

  // ==================== 用户活动 ====================
  ACTIVITY: {
    HEATMAP: '/monitor/activity/heatmap'
  },

  // ==================== 看板 ====================
  KANBAN: {
    LIST: '/tool/kanban/list',
    BY_ID: (id) => `/tool/kanban/${id}`,
    CREATE: '/tool/kanban',
    CARD: {
      CREATE: '/tool/kanban/card',
      MOVE: (id) => `/tool/kanban/card/${id}/move`,
      DELETE: (id) => `/tool/kanban/card/${id}`
    },
    DELETE: (id) => `/tool/kanban/${id}`
  },

  // ==================== 甘特图 ====================
  GANTT: {
    PROJECTS: '/tool/gantt/projects',
    PROJECT_BY_ID: (id) => `/tool/gantt/projects/${id}`,
    CREATE_PROJECT: '/tool/gantt/projects',
    CREATE_TASK: '/tool/gantt/tasks',
    UPDATE_PROGRESS: (id) => `/tool/gantt/tasks/${id}/progress`,
    DELETE_TASK: (id) => `/tool/gantt/tasks/${id}`,
    DELETE_PROJECT: (id) => `/tool/gantt/projects/${id}`
  },

  // ==================== 知识库 ====================
  WIKI: {
    SPACES: '/tool/wiki/spaces',
    PAGES: (spaceId) => `/tool/wiki/spaces/${spaceId}/pages`,
    PAGE_BY_ID: (id) => `/tool/wiki/pages/${id}`,
    CREATE_SPACE: '/tool/wiki/spaces',
    CREATE_PAGE: '/tool/wiki/pages',
    DELETE_SPACE: (id) => `/tool/wiki/spaces/${id}`
  },

  // ==================== 多语言 ====================
  I18N: {
    LOCALES: '/sys/i18n/locales',
    KEYS: '/sys/i18n/keys',
    TRANSLATIONS: (locale) => `/sys/i18n/translations/${locale}`,
    CREATE_KEY: '/sys/i18n/keys',
    SAVE_TRANSLATION: '/sys/i18n/translations',
    DELETE_KEY: (id) => `/sys/i18n/keys/${id}`
  },

  // ==================== 性能分析 ====================
  PROFILING: {
    STATS: '/monitor/profiling/stats'
  },

  // ==================== Schema Diff ====================
  SCHEMA_DIFF: {
    TABLES: '/tool/schema-diff/tables',
    SCHEMA: (table) => `/tool/schema-diff/schema/${table}`,
    COMPARE: '/tool/schema-diff/compare'
  }
}
