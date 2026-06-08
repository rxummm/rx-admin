package com.rx.admin.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * API接口分析服务
 * 根据菜单名称返回完整的前后端交互链路分析数据
 */
@Service
public class ApiAnalysisService {

    /**
     * 获取所有可分析的菜单列表
     */
    public List<Map<String, String>> getAvailableMenus() {
        List<Map<String, String>> menus = new ArrayList<>();
        for (MenuAnalysisConfig config : MENU_CONFIGS.values()) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("name", config.menuName);
            m.put("module", config.module);
            m.put("description", config.description);
            menus.add(m);
        }
        return menus;
    }

    /**
     * 搜索菜单（模糊匹配）
     */
    public List<Map<String, String>> searchMenus(String keyword) {
        return MENU_CONFIGS.values().stream()
                .filter(c -> c.menuName.contains(keyword) || c.module.contains(keyword) || c.description.contains(keyword))
                .map(c -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("name", c.menuName);
                    m.put("module", c.module);
                    m.put("description", c.description);
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * 分析指定菜单
     */
    public Map<String, Object> analyze(String menuName) {
        // 精确匹配
        MenuAnalysisConfig config = MENU_CONFIGS.get(menuName);
        if (config == null) {
            // 模糊匹配
            Optional<String> matched = MENU_CONFIGS.keySet().stream()
                    .filter(k -> k.contains(menuName) || menuName.contains(k))
                    .findFirst();
            if (matched.isPresent()) {
                config = MENU_CONFIGS.get(matched.get());
            }
        }
        if (config == null) return null;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("menuName", config.menuName);
        result.put("module", config.module);
        result.put("description", config.description);

        // 1. 概览信息
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("controllerClass", config.controllerClass);
        overview.put("basePath", config.basePath);
        overview.put("serviceClass", config.serviceClass);
        overview.put("mapperClass", config.mapperClass);
        overview.put("entityClass", config.entityClass);
        overview.put("tableName", config.tableName);
        overview.put("dataSource", config.dataSource);
        overview.put("frontendPath", config.frontendPath);
        overview.put("apiFilePath", config.apiFilePath);
        overview.put("componentName", config.componentName);
        overview.put("componentMapKey", config.componentMapKey);
        result.put("overview", overview);

        // 2. 数据流步骤
        result.put("dataFlow", config.dataFlow);

        // 3. 接口列表
        result.put("apis", config.apis);

        // 4. 实体字段
        result.put("entityFields", config.entityFields);

        // 5. Service方法调用链
        result.put("serviceMethods", config.serviceMethods);

        // 6. 前端组件结构
        result.put("frontendStructure", config.frontendStructure);

        // 7. Mermaid流程图
        result.put("mermaidDiagrams", config.mermaidDiagrams);

        // 8. 权限标识
        result.put("permissions", config.permissions);

        return result;
    }

    // ========== 配置数据 ==========

    private static final Map<String, MenuAnalysisConfig> MENU_CONFIGS = new LinkedHashMap<>();

    static {
        // ====== 红楼人物 ======
        MenuAnalysisConfig honglouCharacter = new MenuAnalysisConfig();
        honglouCharacter.menuName = "红楼人物";
        honglouCharacter.module = "四大名著-红楼梦";
        honglouCharacter.description = "管理红楼梦人物信息，包括增删改查、角色筛选、人物关系查看";
        honglouCharacter.controllerClass = "HonglouController";
        honglouCharacter.basePath = "/api/classics/honglou";
        honglouCharacter.serviceClass = "HonglouCharacterService";
        honglouCharacter.mapperClass = "HonglouCharacterMapper";
        honglouCharacter.entityClass = "HonglouCharacter";
        honglouCharacter.tableName = "honglou_characters";
        honglouCharacter.dataSource = "rxusysadmin（@SecondDB）";
        honglouCharacter.frontendPath = "ui/src/views/classics/honglou/characters/index.vue";
        honglouCharacter.apiFilePath = "ui/src/api/honglou.js";
        honglouCharacter.componentName = "ClassicsHonglouCharacters";
        honglouCharacter.componentMapKey = "classics/honglou/characters/index";

        honglouCharacter.dataFlow = buildDataFlowList(
            step(1, "前端-路由", "Vue Router", "用户点击菜单 → 路由匹配 → 加载组件",
                "menu.component = 'classics/honglou/characters/index' → componentMap 懒加载 → ClassicsHonglouCharacters 组件"),
            step(2, "前端-页面", "CharacterList.vue", "组件挂载 onMounted() → fetchData()",
                "初始化 page=1, size=10，keyword=''，filterRole=''，调用 API 获取数据"),
            step(3, "前端-API", "honglou.js", "getHonglouCharacterPageApi({page, size, keyword})",
                "request({ url: '/classics/honglou/character/page', method: 'get', params })"),
            step(4, "前端-拦截器", "request.js", "Axios 请求拦截器 → 添加 Token 到请求头",
                "Authorization: Bearer {token}，baseURL 拼接 /api"),
            step(5, "后端-认证", "Sa-Token", "SaTokenConfig → StpInterfaceImpl → 校验登录态和权限",
                "@SaCheckLogin 验证是否登录；@SaCheckPermission 验证操作权限"),
            step(6, "后端-Controller", "HonglouController.characterPage()", "接收参数 → 调用 Service",
                "@GetMapping(\"/character/page\") → characterService.pageQuery(page, size, keyword)"),
            step(7, "后端-Service", "HonglouCharacterService.pageQuery()", "构建查询条件 → 调用 Mapper",
                "LambdaQueryWrapper 模糊搜索 name/nickname/appearanceDescription → baseMapper.selectPage()"),
            step(8, "后端-Mapper", "HonglouCharacterMapper", "MyBatis Plus BaseMapper → SQL 执行",
                "@SecondDB 注解 → 路由到 rxusysadmin 数据源 → SELECT * FROM honglou_characters WHERE ..."),
            step(9, "数据库", "honglou_characters 表", "MySQL 查询执行",
                "返回分页数据（records + total）"),
            step(10, "后端-响应", "Result<PageResult>", "Service → Controller → 封装 Result",
                "Result.ok(PageResult.of(total, records)) → JSON 序列化 → HTTP 响应"),
            step(11, "前端-响应", "Axios 响应拦截器", "接收响应 → 解析 code",
                "code=200 → 返回 data；code≠200 → ElMessage.error() 提示"),
            step(12, "前端-渲染", "CharacterList.vue", "更新 tableData → 表格渲染",
                "Element Plus el-table 展示数据，支持排序、筛选、分页")
        );

        honglouCharacter.apis = buildApiList(
            api("GET", "/api/classics/honglou/character/page", "分页查询红楼人物",
                "page (int, 默认1), size (int, 默认10), keyword (String, 可选)",
                "Result<PageResult<HonglouCharacter>>", "@SaCheckLogin", "getHonglouCharacterPageApi"),
            api("GET", "/api/classics/honglou/character/{id}", "获取人物详情",
                "id (Long, 路径参数)",
                "Result<HonglouCharacter>", "@SaCheckLogin", "getHonglouCharacterDetailApi"),
            api("GET", "/api/classics/honglou/character/role", "按角色筛选人物",
                "role (String, 可选: 主角/重要配角/一般角色)",
                "Result<List<HonglouCharacter>>", "@SaCheckLogin", "getHonglouCharactersByRoleApi"),
            api("GET", "/api/classics/honglou/character/all", "获取所有人物",
                "无",
                "Result<List<HonglouCharacter>>", "@SaCheckLogin", "getAllHonglouCharactersApi"),
            api("POST", "/api/classics/honglou/character", "新增人物",
                "RequestBody HonglouCharacter (name必填, role必填)",
                "Result<?>", "@SaCheckPermission('classics:honglou:character:add')", "addHonglouCharacterApi"),
            api("PUT", "/api/classics/honglou/character", "编辑人物",
                "RequestBody HonglouCharacter (含id)",
                "Result<?>", "@SaCheckPermission('classics:honglou:character:edit')", "updateHonglouCharacterApi"),
            api("DELETE", "/api/classics/honglou/character/{id}", "删除人物",
                "id (Long, 路径参数)",
                "Result<?>", "@SaCheckPermission('classics:honglou:character:delete')", "deleteHonglouCharacterApi"),
            api("DELETE", "/api/classics/honglou/character/batch", "批量删除人物",
                "RequestBody List<Long> ids",
                "Result<?>", "@SaCheckPermission('classics:honglou:character:delete')", "batchDeleteHonglouCharacterApi")
        );

        honglouCharacter.entityFields = buildEntityFieldList(
            field("id", "Long", "主键ID，自增", "1"),
            field("name", "String", "人物姓名", "林黛玉"),
            field("nickname", "String", "别称/昵称", "颦儿、潇湘妃子"),
            field("role", "String", "角色身份（主角/重要配角/一般角色）", "主角"),
            field("appearanceDescription", "String", "外貌描述", "闲静时如姣花照水，行动处似弱柳扶风"),
            field("personalityTraits", "String", "性格特点", "多愁善感、才华横溢"),
            field("fateSummary", "String", "命运概述/判词", "泪尽而逝，魂归离恨天"),
            field("createdTime", "LocalDateTime", "创建时间", "2024-01-01 12:00:00"),
            field("updatedTime", "LocalDateTime", "更新时间", "2024-01-01 12:00:00")
        );

        honglouCharacter.serviceMethods = buildServiceMethodList(
            svcMethod("pageQuery(int page, int size, String keyword)", "分页查询",
                "构建 LambdaQueryWrapper，对 name/nickname/appearanceDescription 模糊搜索，调用 baseMapper.selectPage() 返回 PageResult"),
            svcMethod("listByRole(String role)", "按角色筛选",
                "LambdaQueryWrapper eq role，返回 List<HonglouCharacter>"),
            svcMethod("listAll()", "获取全部",
                "无查询条件，orderByAsc id，返回 List<HonglouCharacter>"),
            svcMethod("save(entity)", "新增（继承自 ServiceImpl）",
                "MyBatis Plus 自动生成 INSERT SQL，自增主键回填"),
            svcMethod("updateById(entity)", "编辑（继承自 ServiceImpl）",
                "MyBatis Plus 自动生成 UPDATE SQL，按 id 匹配"),
            svcMethod("removeById(id)", "删除（继承自 ServiceImpl）",
                "MyBatis Plus 逻辑删除，SET deleted=1")
        );

        honglouCharacter.frontendStructure = buildFrontendStructure(
            "ClassicsHonglouCharacters",
            "ui/src/views/classics/honglou/characters/index.vue",
            "Vue 3 Composition API + Element Plus + Axios",
            List.of(
                "搜索栏：keyword 输入框 + role 下拉筛选 + 搜索/重置按钮",
                "工具栏：新增按钮（需 add 权限）+ 批量删除按钮（需 delete 权限）+ 列设置下拉",
                "数据表格：el-table 展示人物列表，支持排序、多选、查看/编辑/删除操作",
                "分页器：el-pagination 支持切换每页条数（10/20/50/100）",
                "新增/编辑弹窗：el-dialog + el-form，字段包括姓名、别称、角色、外貌、性格、命运",
                "查看弹窗：展示人物详情 + 人物关系列表，可点击关系跳转"
            ),
            List.of(
                "keyword → fetchData() → getHonglouCharacterPageApi() → tableData",
                "filterRole → fetchData() → 前端过滤 tableData",
                "page/size → fetchData() → 重新请求分页",
                "form → handleSubmit() → addHonglouCharacterApi / updateHonglouCharacterApi"
            ),
            List.of(
                "userStore.hasPerm('classics:honglou:character:add') → 新增按钮",
                "userStore.hasPerm('classics:honglou:character:edit') → 编辑按钮",
                "userStore.hasPerm('classics:honglou:character:delete') → 删除按钮"
            )
        );

        honglouCharacter.mermaidDiagrams = buildMermaidDiagrams(
            // 时序图
            "sequenceDiagram\n" +
            "    participant U as 用户\n" +
            "    participant V as 红楼人物页面\n" +
            "    participant A as Axios/request.js\n" +
            "    participant C as HonglouController\n" +
            "    participant S as HonglouCharacterService\n" +
            "    participant M as HonglouCharacterMapper\n" +
            "    participant DB as MySQL(rxusysadmin)\n" +
            "    U->>V: 点击\"红楼人物\"菜单\n" +
            "    V->>V: onMounted() → fetchData()\n" +
            "    V->>A: getHonglouCharacterPageApi({page:1,size:10})\n" +
            "    A->>A: 拦截器添加 Token\n" +
            "    A->>C: GET /api/classics/honglou/character/page\n" +
            "    C->>C: @SaCheckLogin 认证\n" +
            "    C->>S: pageQuery(1, 10, null)\n" +
            "    S->>S: 构建 LambdaQueryWrapper\n" +
            "    S->>M: baseMapper.selectPage(page, wrapper)\n" +
            "    M->>DB: SELECT * FROM honglou_characters WHERE deleted=0\n" +
            "    DB-->>M: 返回分页数据\n" +
            "    M-->>S: IPage<HonglouCharacter>\n" +
            "    S-->>C: PageResult(total, records)\n" +
            "    C-->>A: Result{code:200, data:PageResult}\n" +
            "    A->>A: 响应拦截器解析\n" +
            "    A-->>V: {records:[...], total:N}\n" +
            "    V->>V: 渲染 el-table + el-pagination",

            // 调用关系图
            "flowchart TB\n" +
            "    subgraph 前端层\n" +
            "        A[红楼人物页面<br/>CharacterList.vue] --> B[API封装<br/>honglou.js]\n" +
            "        B --> C[Axios实例<br/>request.js]\n" +
            "        C --> D[请求拦截器<br/>添加Token]\n" +
            "    end\n" +
            "    subgraph 后端层\n" +
            "        D --> E[Sa-Token过滤器]\n" +
            "        E --> F[HonglouController<br/>@RequestMapping /api/classics/honglou]\n" +
            "        F --> G[HonglouCharacterService<br/>extends ServiceImpl]\n" +
            "        F --> H[HonglouCharacterRelationService]\n" +
            "        G --> I[HonglouCharacterMapper<br/>@SecondDB]\n" +
            "        H --> J[HonglouCharacterRelationMapper<br/>@SecondDB]\n" +
            "    end\n" +
            "    subgraph 数据层\n" +
            "        I --> K[(rxusysadmin库<br/>honglou_characters)]\n" +
            "        J --> L[(rxusysadmin库<br/>honglou_character_relations)]\n" +
            "    end\n" +
            "    subgraph 响应返回\n" +
            "        K --> M[Result&lt;PageResult&gt;]\n" +
            "        M --> N[Axios响应拦截器]\n" +
            "        N --> A\n" +
            "    end"
        );

        honglouCharacter.permissions = List.of(
            "classics:honglou:character:add    - 新增人物",
            "classics:honglou:character:edit   - 编辑人物",
            "classics:honglou:character:delete - 删除人物"
        );

        MENU_CONFIGS.put("红楼人物", honglouCharacter);

        // ====== 红楼诗词 ======
        MenuAnalysisConfig honglouPoem = new MenuAnalysisConfig();
        honglouPoem.menuName = "红楼诗词";
        honglouPoem.module = "四大名著-红楼梦";
        honglouPoem.description = "管理红楼梦诗词数据，增删改查";
        honglouPoem.controllerClass = "HonglouController";
        honglouPoem.basePath = "/api/classics/honglou";
        honglouPoem.serviceClass = "HonglouPoemService";
        honglouPoem.mapperClass = "HonglouPoemMapper";
        honglouPoem.entityClass = "HonglouPoem";
        honglouPoem.tableName = "honglou_poems";
        honglouPoem.dataSource = "rxusysadmin（@SecondDB）";
        honglouPoem.frontendPath = "ui/src/views/classics/honglou/poems/index.vue";
        honglouPoem.apiFilePath = "ui/src/api/honglou.js";
        honglouPoem.componentName = "ClassicsHonglouPoems";
        honglouPoem.componentMapKey = "classics/honglou/poems/index";

        honglouPoem.dataFlow = buildDataFlowList(
            step(1, "前端-路由", "Vue Router", "用户点击菜单 → 路由匹配",
                "componentMap['classics/honglou/poems/index'] → ClassicsHonglouPoems"),
            step(2, "前端-页面", "PoemsList.vue", "onMounted() → fetchData()",
                "初始化分页参数，调用 getHonglouPoemPageApi()"),
            step(3, "前端-API", "honglou.js", "getHonglouPoemPageApi({page, size, keyword})",
                "GET /classics/honglou/poem/page"),
            step(4, "后端-Controller", "HonglouController.poemPage()", "接收参数 → poemService.pageQuery()",
                "返回 Result<PageResult<HonglouPoem>>"),
            step(5, "后端-Service", "HonglouPoemService.pageQuery()", "构建查询 → baseMapper.selectPage()",
                "LambdaQueryWrapper 按 title/content/author 模糊搜索"),
            step(6, "后端-Mapper", "HonglouPoemMapper", "@SecondDB → honglou_poems 表",
                "SELECT * FROM honglou_poems WHERE deleted=0"),
            step(7, "前端-渲染", "PoemsList.vue", "tableData 更新 → el-table 渲染",
                "支持查看详情、新增、编辑、删除操作")
        );

        honglouPoem.apis = buildApiList(
            api("GET", "/api/classics/honglou/poem/page", "分页查询红楼诗词", "page, size, keyword", "Result<PageResult<HonglouPoem>>", "@SaCheckLogin", "getHonglouPoemPageApi"),
            api("GET", "/api/classics/honglou/poem/{id}", "诗词详情", "id", "Result<HonglouPoem>", "@SaCheckLogin", "getHonglouPoemDetailApi"),
            api("POST", "/api/classics/honglou/poem", "新增诗词", "RequestBody HonglouPoem", "Result<?>", "@SaCheckPermission", "addHonglouPoemApi"),
            api("PUT", "/api/classics/honglou/poem", "编辑诗词", "RequestBody HonglouPoem", "Result<?>", "@SaCheckPermission", "updateHonglouPoemApi"),
            api("DELETE", "/api/classics/honglou/poem/{id}", "删除诗词", "id", "Result<?>", "@SaCheckPermission", "deleteHonglouPoemApi"),
            api("DELETE", "/api/classics/honglou/poem/batch", "批量删除", "RequestBody List<Long>", "Result<?>", "@SaCheckPermission", "batchDeleteHonglouPoemApi")
        );

        honglouPoem.entityFields = buildEntityFieldList(
            field("id", "Long", "主键ID", ""),
            field("title", "String", "诗词标题", ""),
            field("content", "String", "诗词内容", ""),
            field("author", "String", "作者", ""),
            field("chapter", "String", "出自回目", ""),
            field("createdTime", "LocalDateTime", "创建时间", "")
        );

        honglouPoem.serviceMethods = buildServiceMethodList(
            svcMethod("pageQuery(page, size, keyword)", "分页查询诗词", "按 title/content/author 模糊搜索"),
            svcMethod("save / updateById / removeById", "CRUD操作", "继承自 ServiceImpl")
        );

        honglouPoem.frontendStructure = buildFrontendStructure(
            "ClassicsHonglouPoems", "ui/src/views/classics/honglou/poems/index.vue",
            "Vue 3 + Element Plus", List.of("搜索栏", "诗词表格", "增删改查弹窗"), List.of(), List.of()
        );

        honglouPoem.mermaidDiagrams = buildMermaidDiagrams(
            "sequenceDiagram\n    U->>V: 点击红楼诗词\n    V->>A: getHonglouPoemPageApi()\n    A->>C: GET /api/classics/honglou/poem/page\n    C->>S: poemService.pageQuery()\n    S->>M: baseMapper.selectPage()\n    M->>DB: SELECT * FROM honglou_poems\n    DB-->>M: 数据\n    M-->>S: IPage\n    S-->>C: PageResult\n    C-->>V: Result{code:200}\n    V->>V: 渲染表格",
            "flowchart LR\n    A[PoemsList.vue] --> B[honglou.js]\n    B --> C[HonglouController]\n    C --> D[HonglouPoemService]\n    D --> E[HonglouPoemMapper]\n    E --> F[(honglou_poems)]"
        );

        honglouPoem.permissions = List.of(
            "classics:honglou:poem:add", "classics:honglou:poem:edit", "classics:honglou:poem:delete"
        );

        MENU_CONFIGS.put("红楼诗词", honglouPoem);

        // ====== 红楼关系 ======
        MenuAnalysisConfig honglouRelation = new MenuAnalysisConfig();
        honglouRelation.menuName = "红楼关系";
        honglouRelation.module = "四大名著-红楼梦";
        honglouRelation.description = "管理红楼梦人物关系，查看关系图谱";
        honglouRelation.controllerClass = "HonglouController";
        honglouRelation.basePath = "/api/classics/honglou";
        honglouRelation.serviceClass = "HonglouCharacterRelationService";
        honglouRelation.mapperClass = "HonglouCharacterRelationMapper";
        honglouRelation.entityClass = "HonglouCharacterRelation";
        honglouRelation.tableName = "honglou_character_relations";
        honglouRelation.dataSource = "rxusysadmin（@SecondDB）";
        honglouRelation.frontendPath = "ui/src/views/classics/honglou/relations/index.vue";
        honglouRelation.apiFilePath = "ui/src/api/honglou.js";
        honglouRelation.componentName = "ClassicsHonglouRelations";
        honglouRelation.componentMapKey = "classics/honglou/relations/index";

        honglouRelation.dataFlow = buildDataFlowList(
            step(1, "前端-页面", "RelationsList.vue", "进入页面 → 加载所有人物和关系",
                "getAllHonglouCharactersApi() + getAllHonglouRelationsApi()"),
            step(2, "后端-Controller", "HonglouController", "characterAll() / relationAll()",
                "返回全部人物列表和关系列表"),
            step(3, "前端-渲染", "关系图谱", "使用可视化组件渲染关系网络",
                "节点=人物，连线=关系类型")
        );

        honglouRelation.apis = buildApiList(
            api("GET", "/api/classics/honglou/relation/{characterId}", "按人物查关系", "characterId", "Result<List<HonglouCharacterRelation>>", "@SaCheckLogin", "getHonglouRelationApi"),
            api("GET", "/api/classics/honglou/relation/all", "所有关系", "无", "Result<List<HonglouCharacterRelation>>", "@SaCheckLogin", "getAllHonglouRelationsApi"),
            api("POST", "/api/classics/honglou/relation", "新增关系", "RequestBody", "Result<?>", "@SaCheckPermission", "addHonglouRelationApi"),
            api("PUT", "/api/classics/honglou/relation", "编辑关系", "RequestBody", "Result<?>", "@SaCheckPermission", "updateHonglouRelationApi"),
            api("DELETE", "/api/classics/honglou/relation/{id}", "删除关系", "id", "Result<?>", "@SaCheckPermission", "deleteHonglouRelationApi")
        );

        honglouRelation.entityFields = buildEntityFieldList(
            field("id", "Long", "主键", ""),
            field("characterId", "Long", "人物ID", ""),
            field("toCharacterId", "Long", "关联人物ID", ""),
            field("relationType", "String", "关系类型（亲属/主仆/恋人/朋友/其他）", ""),
            field("relationDesc", "String", "关系描述", "")
        );

        honglouRelation.serviceMethods = buildServiceMethodList(
            svcMethod("listByCharacterId(characterId)", "按人物查关系", "查询某人的所有关系"),
            svcMethod("listAll()", "全部关系", "无筛选返回所有关系")
        );

        honglouRelation.frontendStructure = buildFrontendStructure(
            "ClassicsHonglouRelations", "ui/src/views/classics/honglou/relations/index.vue",
            "Vue 3 + 关系图谱可视化", List.of("关系列表", "关系图谱", "增删改查"), List.of(), List.of()
        );

        honglouRelation.mermaidDiagrams = buildMermaidDiagrams(
            "sequenceDiagram\n    U->>V: 点击红楼关系\n    V->>A: getAllHonglouCharactersApi()\n    V->>A: getAllHonglouRelationsApi()\n    A->>C: GET /api/classics/honglou/character/all\n    A->>C: GET /api/classics/honglou/relation/all\n    C->>S: characterService.listAll()\n    C->>S: relationService.listAll()\n    S->>M: 查询数据库\n    M->>DB: SELECT\n    DB-->>V: 全部数据\n    V->>V: 渲染关系图谱",
            "flowchart LR\n    A[RelationsList.vue] --> B[honglou.js]\n    B --> C[HonglouController]\n    C --> D[CharacterService + RelationService]\n    D --> E[(honglou_characters + honglou_character_relations)]"
        );

        honglouRelation.permissions = List.of(
            "classics:honglou:relation:add", "classics:honglou:relation:edit", "classics:honglou:relation:delete"
        );

        MENU_CONFIGS.put("红楼关系", honglouRelation);

        // ====== 西游人物 ======
        MenuAnalysisConfig xiyouCharacter = new MenuAnalysisConfig();
        xiyouCharacter.menuName = "西游人物";
        xiyouCharacter.module = "四大名著-西游记";
        xiyouCharacter.description = "管理西游记人物信息，包括种族筛选、增删改查";
        xiyouCharacter.controllerClass = "XiyouController";
        xiyouCharacter.basePath = "/api/classics/xiyou";
        xiyouCharacter.serviceClass = "XiyouCharacterService";
        xiyouCharacter.mapperClass = "XiyouCharacterMapper";
        xiyouCharacter.entityClass = "XiyouCharacter";
        xiyouCharacter.tableName = "xiyou_characters";
        xiyouCharacter.dataSource = "rxusysadmin（@SecondDB）";
        xiyouCharacter.frontendPath = "ui/src/views/classics/xiyou/characters/index.vue";
        xiyouCharacter.apiFilePath = "ui/src/api/xiyou.js";
        xiyouCharacter.componentName = "ClassicsXiyouCharacters";
        xiyouCharacter.componentMapKey = "classics/xiyou/characters/index";

        xiyouCharacter.dataFlow = buildDataFlowList(
            step(1, "前端-路由", "Vue Router", "componentMap['classics/xiyou/characters/index'] → ClassicsXiyouCharacters", ""),
            step(2, "前端-页面", "CharactersList.vue", "onMounted() → fetchData()", "初始化分页参数"),
            step(3, "前端-API", "xiyou.js", "getXiyouCharacterPageApi({page, size, keyword})", "GET /classics/xiyou/character/page"),
            step(4, "后端-Controller", "XiyouController.characterPage()", "→ characterService.pageQuery()", "返回 Result<PageResult<XiyouCharacter>>"),
            step(5, "后端-Service", "XiyouCharacterService.pageQuery()", "LambdaQueryWrapper 模糊搜索 → selectPage()", ""),
            step(6, "后端-Mapper", "XiyouCharacterMapper", "@SecondDB → xiyou_characters 表", ""),
            step(7, "前端-渲染", "CharactersList.vue", "tableData → el-table", "支持种族筛选")
        );

        xiyouCharacter.apis = buildApiList(
            api("GET", "/api/classics/xiyou/character/page", "分页查询", "page, size, keyword", "Result<PageResult<XiyouCharacter>>", "@SaCheckLogin", "getXiyouCharacterPageApi"),
            api("GET", "/api/classics/xiyou/character/{id}", "详情", "id", "Result<XiyouCharacter>", "@SaCheckLogin", "getXiyouCharacterDetailApi"),
            api("GET", "/api/classics/xiyou/character/race", "按种族筛选", "race", "Result<List<XiyouCharacter>>", "@SaCheckLogin", "getXiyouCharactersByRaceApi"),
            api("POST", "/api/classics/xiyou/character", "新增", "RequestBody", "Result<?>", "@SaCheckPermission", "addXiyouCharacterApi"),
            api("PUT", "/api/classics/xiyou/character", "编辑", "RequestBody", "Result<?>", "@SaCheckPermission", "updateXiyouCharacterApi"),
            api("DELETE", "/api/classics/xiyou/character/{id}", "删除", "id", "Result<?>", "@SaCheckPermission", "deleteXiyouCharacterApi"),
            api("DELETE", "/api/classics/xiyou/character/batch", "批量删除", "RequestBody List<Long>", "Result<?>", "@SaCheckPermission", "batchDeleteXiyouCharacterApi")
        );

        xiyouCharacter.entityFields = buildEntityFieldList(
            field("id", "Long", "主键ID", ""),
            field("name", "String", "人物名称", ""),
            field("nickname", "String", "别称", ""),
            field("race", "String", "种族（仙/佛/妖/人/神）", ""),
            field("description", "String", "描述", ""),
            field("createdTime", "LocalDateTime", "创建时间", "")
        );

        xiyouCharacter.serviceMethods = buildServiceMethodList(
            svcMethod("pageQuery(page, size, keyword)", "分页查询", "LambdaQueryWrapper 模糊搜索"),
            svcMethod("listByRace(race)", "按种族筛选", "eq race 字段"),
            svcMethod("save/updateById/removeById", "CRUD", "继承自 ServiceImpl")
        );

        xiyouCharacter.frontendStructure = buildFrontendStructure(
            "ClassicsXiyouCharacters", "ui/src/views/classics/xiyou/characters/index.vue",
            "Vue 3 + Element Plus", List.of("搜索栏（含种族筛选）", "人物表格", "增删改查"), List.of(), List.of()
        );

        xiyouCharacter.mermaidDiagrams = buildMermaidDiagrams(
            "sequenceDiagram\n    U->>V: 点击西游人物\n    V->>A: getXiyouCharacterPageApi()\n    A->>C: GET /api/classics/xiyou/character/page\n    C->>S: characterService.pageQuery()\n    S->>M: baseMapper.selectPage()\n    M->>DB: SELECT * FROM xiyou_characters\n    DB-->>V: 数据\n    V->>V: 渲染",
            "flowchart LR\n    A[CharactersList.vue] --> B[xiyou.js]\n    B --> C[XiyouController]\n    C --> D[XiyouCharacterService]\n    D --> E[XiyouCharacterMapper]\n    E --> F[(xiyou_characters)]"
        );

        xiyouCharacter.permissions = List.of(
            "classics:xiyou:character:add", "classics:xiyou:character:edit", "classics:xiyou:character:delete"
        );

        MENU_CONFIGS.put("西游人物", xiyouCharacter);

        // ====== 三国人物 ======
        MenuAnalysisConfig sanguoCharacter = new MenuAnalysisConfig();
        sanguoCharacter.menuName = "三国人物";
        sanguoCharacter.module = "四大名著-三国演义";
        sanguoCharacter.description = "管理三国人物，支持按国家筛选";
        sanguoCharacter.controllerClass = "SanguoController";
        sanguoCharacter.basePath = "/api/classics/sanguo";
        sanguoCharacter.serviceClass = "SanguoCharacterService";
        sanguoCharacter.mapperClass = "SanguoCharacterMapper";
        sanguoCharacter.entityClass = "SanguoCharacter";
        sanguoCharacter.tableName = "sanguo_characters";
        sanguoCharacter.dataSource = "rxusysadmin（@SecondDB）";
        sanguoCharacter.frontendPath = "ui/src/views/classics/sanguo/characters/index.vue";
        sanguoCharacter.apiFilePath = "ui/src/api/sanguo.js";
        sanguoCharacter.componentName = "ClassicsSanguoCharacters";
        sanguoCharacter.componentMapKey = "classics/sanguo/characters/index";

        sanguoCharacter.dataFlow = buildDataFlowList(
            step(1, "前端-路由", "Vue Router", "componentMap['classics/sanguo/characters/index']", ""),
            step(2, "前端-页面", "CharactersList.vue", "onMounted() → fetchData()", ""),
            step(3, "前端-API", "sanguo.js", "getSanguoCharacterPageApi()", "GET /classics/sanguo/character/page"),
            step(4, "后端-Controller", "SanguoController.characterPage()", "→ characterService.pageQuery()", ""),
            step(5, "后端-Service", "SanguoCharacterService.pageQuery()", "LambdaQueryWrapper → selectPage()", ""),
            step(6, "后端-Mapper", "SanguoCharacterMapper", "@SecondDB → sanguo_characters 表", ""),
            step(7, "前端-渲染", "CharactersList.vue", "el-table + 国家筛选", "")
        );

        sanguoCharacter.apis = buildApiList(
            api("GET", "/api/classics/sanguo/character/page", "分页查询", "page, size, keyword", "Result<PageResult<SanguoCharacter>>", "@SaCheckLogin", "getSanguoCharacterPageApi"),
            api("GET", "/api/classics/sanguo/character/{id}", "详情", "id", "Result<SanguoCharacter>", "@SaCheckLogin", "getSanguoCharacterDetailApi"),
            api("GET", "/api/classics/sanguo/character/country", "按国家筛选", "country (魏/蜀/吴/其他)", "Result<List<SanguoCharacter>>", "@SaCheckLogin", "getSanguoCharactersByCountryApi"),
            api("POST", "/api/classics/sanguo/character", "新增", "RequestBody", "Result<?>", "@SaCheckPermission", "addSanguoCharacterApi"),
            api("PUT", "/api/classics/sanguo/character", "编辑", "RequestBody", "Result<?>", "@SaCheckPermission", "updateSanguoCharacterApi"),
            api("DELETE", "/api/classics/sanguo/character/{id}", "删除", "id", "Result<?>", "@SaCheckPermission", "deleteSanguoCharacterApi"),
            api("DELETE", "/api/classics/sanguo/character/batch", "批量删除", "RequestBody List<Long>", "Result<?>", "@SaCheckPermission", "batchDeleteSanguoCharacterApi")
        );

        sanguoCharacter.entityFields = buildEntityFieldList(
            field("id", "Long", "主键ID", ""),
            field("name", "String", "人物姓名", ""),
            field("country", "String", "所属国家（魏/蜀/吴/其他）", ""),
            field("description", "String", "描述", ""),
            field("createdTime", "LocalDateTime", "创建时间", "")
        );

        sanguoCharacter.serviceMethods = buildServiceMethodList(
            svcMethod("pageQuery(page, size, keyword)", "分页查询", "LambdaQueryWrapper 模糊搜索"),
            svcMethod("listByCountry(country)", "按国家筛选", "eq country 字段"),
            svcMethod("save/updateById/removeById", "CRUD", "继承自 ServiceImpl")
        );

        sanguoCharacter.frontendStructure = buildFrontendStructure(
            "ClassicsSanguoCharacters", "ui/src/views/classics/sanguo/characters/index.vue",
            "Vue 3 + Element Plus", List.of("搜索栏（含国家筛选）", "人物表格", "增删改查"), List.of(), List.of()
        );

        sanguoCharacter.mermaidDiagrams = buildMermaidDiagrams(
            "sequenceDiagram\n    U->>V: 点击三国人物\n    V->>A: getSanguoCharacterPageApi()\n    A->>C: GET /api/classics/sanguo/character/page\n    C->>S: characterService.pageQuery()\n    S->>M: baseMapper.selectPage()\n    M->>DB: SELECT * FROM sanguo_characters\n    DB-->>V: 数据\n    V->>V: 渲染",
            "flowchart LR\n    A[CharactersList.vue] --> B[sanguo.js]\n    B --> C[SanguoController]\n    C --> D[SanguoCharacterService]\n    D --> E[SanguoCharacterMapper]\n    E --> F[(sanguo_characters)]"
        );

        sanguoCharacter.permissions = List.of(
            "classics:sanguo:character:add", "classics:sanguo:character:edit", "classics:sanguo:character:delete"
        );

        MENU_CONFIGS.put("三国人物", sanguoCharacter);

        // ====== 水浒章节 ======
        MenuAnalysisConfig shuihuChapter = new MenuAnalysisConfig();
        shuihuChapter.menuName = "水浒章节";
        shuihuChapter.module = "四大名著-水浒传";
        shuihuChapter.description = "管理水浒传章节内容";
        shuihuChapter.controllerClass = "ShuihuController";
        shuihuChapter.basePath = "/api/classics/shuihu";
        shuihuChapter.serviceClass = "ShuihuChapterService";
        shuihuChapter.mapperClass = "ShuihuChapterMapper";
        shuihuChapter.entityClass = "ShuihuChapter";
        shuihuChapter.tableName = "shuihu_chapters";
        shuihuChapter.dataSource = "rxusysadmin（@SecondDB）";
        shuihuChapter.frontendPath = "ui/src/views/classics/shuihu/chapters/index.vue";
        shuihuChapter.apiFilePath = "ui/src/api/shuihu.js";
        shuihuChapter.componentName = "ClassicsShuihuChapters";
        shuihuChapter.componentMapKey = "classics/shuihu/chapters/index";

        shuihuChapter.dataFlow = buildDataFlowList(
            step(1, "前端-路由", "Vue Router", "componentMap['classics/shuihu/chapters/index']", ""),
            step(2, "前端-页面", "ChaptersList.vue", "onMounted() → fetchData()", ""),
            step(3, "前端-API", "shuihu.js", "getShuihuChapterPageApi()", "GET /classics/shuihu/chapter/page"),
            step(4, "后端-Controller", "ShuihuController.chapterPage()", "→ chapterService.pageQuery()", ""),
            step(5, "后端-Service", "ShuihuChapterService.pageQuery()", "LambdaQueryWrapper → selectPage()", ""),
            step(6, "后端-Mapper", "ShuihuChapterMapper", "@SecondDB → shuihu_chapters 表", ""),
            step(7, "前端-渲染", "ChaptersList.vue", "el-table 渲染", "")
        );

        shuihuChapter.apis = buildApiList(
            api("GET", "/api/classics/shuihu/chapter/page", "分页查询", "page, size, keyword", "Result<PageResult<ShuihuChapter>>", "@SaCheckLogin", "getShuihuChapterPageApi"),
            api("GET", "/api/classics/shuihu/chapter/{id}", "详情", "id", "Result<ShuihuChapter>", "@SaCheckLogin", "getShuihuChapterDetailApi"),
            api("POST", "/api/classics/shuihu/chapter", "新增", "RequestBody", "Result<?>", "@SaCheckPermission", "addShuihuChapterApi"),
            api("PUT", "/api/classics/shuihu/chapter", "编辑", "RequestBody", "Result<?>", "@SaCheckPermission", "updateShuihuChapterApi"),
            api("DELETE", "/api/classics/shuihu/chapter/{id}", "删除", "id", "Result<?>", "@SaCheckPermission", "deleteShuihuChapterApi"),
            api("DELETE", "/api/classics/shuihu/chapter/batch", "批量删除", "RequestBody List<Long>", "Result<?>", "@SaCheckPermission", "batchDeleteShuihuChapterApi")
        );

        shuihuChapter.entityFields = buildEntityFieldList(
            field("id", "Long", "主键ID", ""),
            field("chapterNumber", "Integer", "章节序号", ""),
            field("title", "String", "章节标题", ""),
            field("summary", "String", "章节概要", ""),
            field("createdTime", "LocalDateTime", "创建时间", "")
        );

        shuihuChapter.serviceMethods = buildServiceMethodList(
            svcMethod("pageQuery(page, size, keyword)", "分页查询", "LambdaQueryWrapper 模糊搜索"),
            svcMethod("save/updateById/removeById", "CRUD", "继承自 ServiceImpl")
        );

        shuihuChapter.frontendStructure = buildFrontendStructure(
            "ClassicsShuihuChapters", "ui/src/views/classics/shuihu/chapters/index.vue",
            "Vue 3 + Element Plus", List.of("搜索栏", "章节列表", "增删改查"), List.of(), List.of()
        );

        shuihuChapter.mermaidDiagrams = buildMermaidDiagrams(
            "sequenceDiagram\n    U->>V: 点击水浒章节\n    V->>A: getShuihuChapterPageApi()\n    A->>C: GET /api/classics/shuihu/chapter/page\n    C->>S: chapterService.pageQuery()\n    S->>M: baseMapper.selectPage()\n    M->>DB: SELECT * FROM shuihu_chapters\n    DB-->>V: 数据\n    V->>V: 渲染",
            "flowchart LR\n    A[ChaptersList.vue] --> B[shuihu.js]\n    B --> C[ShuihuController]\n    C --> D[ShuihuChapterService]\n    D --> E[ShuihuChapterMapper]\n    E --> F[(shuihu_chapters)]"
        );

        shuihuChapter.permissions = List.of(
            "classics:shuihu:chapter:add", "classics:shuihu:chapter:edit", "classics:shuihu:chapter:delete"
        );

        MENU_CONFIGS.put("水浒章节", shuihuChapter);
    }

    // ========== 辅助构建方法 ==========

    @SafeVarargs
    private static List<Map<String, String>> buildDataFlowList(Map<String, String>... steps) {
        return List.of(steps);
    }

    private static Map<String, String> step(int seq, String layer, String component, String action, String detail) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("seq", String.valueOf(seq));
        m.put("layer", layer);
        m.put("component", component);
        m.put("action", action);
        m.put("detail", detail);
        return m;
    }

    @SafeVarargs
    private static List<Map<String, String>> buildApiList(Map<String, String>... apis) {
        return List.of(apis);
    }

    private static Map<String, String> api(String method, String path, String desc, String params, String returns, String auth, String frontendFunc) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("method", method);
        m.put("path", path);
        m.put("description", desc);
        m.put("params", params);
        m.put("returns", returns);
        m.put("auth", auth);
        m.put("frontendFunc", frontendFunc);
        return m;
    }

    @SafeVarargs
    private static List<Map<String, String>> buildEntityFieldList(Map<String, String>... fields) {
        return List.of(fields);
    }

    private static Map<String, String> field(String name, String type, String desc, String example) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("type", type);
        m.put("description", desc);
        m.put("example", example);
        return m;
    }

    @SafeVarargs
    private static List<Map<String, String>> buildServiceMethodList(Map<String, String>... methods) {
        return List.of(methods);
    }

    private static Map<String, String> svcMethod(String signature, String desc, String detail) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("signature", signature);
        m.put("description", desc);
        m.put("detail", detail);
        return m;
    }

    private static Map<String, Object> buildFrontendStructure(String componentName, String filePath, String tech,
                                                               List<String> sections, List<String> stateFlow, List<String> permissionControls) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("componentName", componentName);
        m.put("filePath", filePath);
        m.put("techStack", tech);
        m.put("sections", sections);
        m.put("stateFlow", stateFlow);
        m.put("permissionControls", permissionControls);
        return m;
    }

    private static Map<String, String> buildMermaidDiagrams(String sequenceDiagram, String flowchart) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("sequenceDiagram", sequenceDiagram);
        m.put("flowchart", flowchart);
        return m;
    }

    // ========== 内部配置类 ==========

    private static class MenuAnalysisConfig {
        String menuName;
        String module;
        String description;
        String controllerClass;
        String basePath;
        String serviceClass;
        String mapperClass;
        String entityClass;
        String tableName;
        String dataSource;
        String frontendPath;
        String apiFilePath;
        String componentName;
        String componentMapKey;
        List<Map<String, String>> dataFlow;
        List<Map<String, String>> apis;
        List<Map<String, String>> entityFields;
        List<Map<String, String>> serviceMethods;
        Map<String, Object> frontendStructure;
        Map<String, String> mermaidDiagrams;
        List<String> permissions;
    }
}
