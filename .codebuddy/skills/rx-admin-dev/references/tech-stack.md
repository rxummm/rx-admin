# 技术栈选型标准

## 强制技术栈

| 层级 | 技术 | 版本要求 | 说明 |
|------|------|---------|------|
| **运行环境** | Java / Node.js | Java 17+ / Node 18+ | LTS 版本 |
| **后端框架** | Spring Boot | 3.5.x | Jakarta EE 9+ |
| **ORM** | MyBatis Plus + MapStruct | 3.5.x / 1.5.x | `BaseMapper` + 编译时对象转换 |
| **对象转换** | MapStruct | 1.5.x | `@Mapper(componentModel = "spring", unmappedTargetPolicy = IGNORE)` |
| **安全认证** | Sa-Token | 1.37+ | 替代 Spring Security / Shiro |
| **API 文档** | Knife4j | 4.4+ | OpenAPI 3 规范，`@Tag` / `@Operation` 注解 |
| **数据库** | MySQL | 8.0+ | utf8mb4 字符集 |
| **密码加密** | BCryptPasswordEncoder | — | Spring Security Crypto |
| **限流** | Guava RateLimiter | 33.0+ | 登录接口每秒 3 次限制 |
| **本地缓存** | Caffeine | 3.x (Spring Boot 内嵌) | 系统配置、菜单树高频数据缓存 |
| **Maven 插件** | build-helper-maven-plugin | 3.x | 将 MapStruct 生成代码加入源码路径 |
| **邮件服务** | Spring Boot Mail | 3.5.x | 通用邮件发送（SMTP） |
| **前端框架** | Vue 3 (Composition API) | ^3.4.0 | `<script setup>` 语法 |
| **构建工具** | Vite | ^5.0 | 替代 webpack |
| **路由** | Vue Router | ^4.2 | 动态路由 + `addRoute` |
| **状态管理** | Pinia | ^2.1 | Composition API 风格 |
| **HTTP 客户端** | Axios | ^1.6 | 统一拦截器封装 |
| **UI 组件库** | Element Plus | ^2.4 | 全量引入 + 暗黑模式 |
| **CSS 预处理** | SCSS (sass-embedded) | ^1.69 | 全局变量注入（替代 sass） |
| **国际化** | Vue I18n | ^9.14 | Composition API 模式 |
| **进度条** | NProgress | ^0.2 | 路由切换进度条 |
| **错误监控** | @sentry/vue | ^10.0 | Sentry v10 + browserTracingIntegration |
| **图表** | ECharts | ^6.1 | 仪表盘/知识图谱/日志分析/健康监控 |
| **自托管字体** | @fontsource/dm-sans, ibm-plex-sans, jetbrains-mono | ^5.x | 替代 Google Fonts CDN |

## 禁止引入的技术

- **Fastjson / Fastjson2**：统一使用 Jackson（Spring Boot 默认）
- **Spring Security**：使用 Sa-Token 替代
- **Shiro**：使用 Sa-Token 替代
- **JSP / Thymeleaf**：前后端完全分离，后端只返回 JSON
- **Vue 2 / Options API**：统一使用 Vue 3 Composition API
- **Vuex**：使用 Pinia 替代
- **Webpack**：使用 Vite 替代
- **sass（Dart Sass 旧版）**：使用 sass-embedded 替代
- **@sentry/tracing**：使用 @sentry/vue v10 browserTracingIntegration 替代
- **Google Fonts CDN**：使用 @fontsource 自托管替代