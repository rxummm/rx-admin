package com.rx.admin.controller;

import com.rx.admin.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "代码生成器")
@RestController
@RequestMapping("/api/tool/gen")
@RequiredArgsConstructor
public class GenController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/tables")
    public Result<List<Map<String, Object>>> tables() {
        return Result.ok(jdbcTemplate.queryForList(
            "SELECT TABLE_NAME tableName, TABLE_COMMENT tableComment, CREATE_TIME createTime " +
            "FROM information_schema.TABLES WHERE TABLE_SCHEMA='rx_admin' ORDER BY CREATE_TIME DESC"));
    }

    @GetMapping("/columns")
    public Result<List<Map<String, Object>>> columns(@RequestParam String table) {
        return Result.ok(jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME columnName, DATA_TYPE dataType, COLUMN_COMMENT columnComment, " +
            "IS_NULLABLE isNullable, COLUMN_KEY columnKey, CHARACTER_MAXIMUM_LENGTH maxLen " +
            "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='rx_admin' AND TABLE_NAME=? ORDER BY ORDINAL_POSITION", table));
    }

    @PostMapping("/preview")
    public Result<Map<String, Object>> preview(@RequestBody Map<String, Object> params) {
        return generateCode(params, false);
    }

    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestBody Map<String, Object> params) {
        return generateCode(params, true);
    }

    private Result<Map<String, Object>> generateCode(Map<String, Object> params, boolean writeToFile) {
        String tableName = (String) params.getOrDefault("tableName", "");
        String packageName = (String) params.getOrDefault("packageName", "com.rx.admin");
        String moduleName = (String) params.getOrDefault("moduleName", "");
        if (tableName.isBlank() || moduleName.isBlank()) return Result.fail("表名和模块名不能为空");

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME columnName, DATA_TYPE dataType, COLUMN_COMMENT columnComment, " +
            "IS_NULLABLE isNullable, COLUMN_KEY columnKey FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA='rx_admin' AND TABLE_NAME=? ORDER BY ORDINAL_POSITION", tableName);

        String entityName = toCamel(tableName, true);
        String pathName = toPathName(tableName);
        Set<String> baseFields = Set.of("id", "deleted", "create_time", "createTime", "update_time", "updateTime");

        // 过滤基础字段
        List<Map<String, Object>> filtered = columns.stream()
                .filter(c -> !baseFields.contains(c.get("columnName")))
                .collect(Collectors.toList());

        // Entity
        StringBuilder entity = new StringBuilder();
        entity.append("package ").append(packageName).append(".entity;\n\n");
        entity.append("import com.baomidou.mybatisplus.annotation.*;\n");
        entity.append("import com.rx.admin.common.base.BaseEntity;\n");
        entity.append("import lombok.Getter;\nimport lombok.Setter;\n\n");
        entity.append("@Getter @Setter\n@TableName(\"").append(tableName).append("\")\n");
        entity.append("public class ").append(entityName).append(" extends BaseEntity {\n");
        for (Map<String, Object> col : filtered) {
            String colName = (String) col.get("columnName");
            String javaType = toJavaType((String) col.get("dataType"));
            entity.append("    @TableField(\"").append(colName).append("\")\n");
            entity.append("    private ").append(javaType).append(" ").append(toCamel(colName, false)).append(";\n");
        }
        entity.append("}\n");

        // Mapper
        String mapper = "package " + packageName + ".mapper;\n\n" +
            "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n" +
            "import " + packageName + ".entity." + entityName + ";\n" +
            "import org.apache.ibatis.annotations.Mapper;\n\n" +
            "@Mapper\npublic interface " + entityName + "Mapper extends BaseMapper<" + entityName + "> {}\n";

        // Service
        String service = "package " + packageName + ".service;\n\n" +
            "import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n" +
            "import " + packageName + ".entity." + entityName + ";\n" +
            "import " + packageName + ".mapper." + entityName + "Mapper;\n" +
            "import org.springframework.stereotype.Service;\n\n" +
            "@Service\npublic class " + entityName + "Service extends ServiceImpl<" + entityName + "Mapper, " + entityName + "> {}\n";

        // Controller
        String controller = "package " + packageName + ".controller;\n\n" +
            "import cn.dev33.satoken.annotation.SaCheckPermission;\n" +
            "import com.rx.admin.common.base.BaseCrudController;\n" +
            "import " + packageName + ".entity." + entityName + ";\n" +
            "import " + packageName + ".service." + entityName + "Service;\n" +
            "import org.springframework.web.bind.annotation.*;\n\n" +
            "@RestController\n@RequestMapping(\"/api/" + moduleName + "/" + pathName + "\")\n" +
            "public class " + entityName + "Controller extends BaseCrudController<" + entityName + "Service, " + entityName + "> {}\n";

        // Vue page (simple crud)
        String vue = "<template>\n" +
            "  <div class=\"page-container\">\n" +
            "    <div class=\"search-bar\">\n" +
            "      <el-input v-model=\"keyword\" placeholder=\"搜索\" style=\"width:200px\" @keyup.enter=\"fetchData\" clearable />\n" +
            "      <el-button type=\"primary\" @click=\"fetchData\">搜索</el-button>\n" +
            "      <el-button @click=\"resetSearch\">重置</el-button>\n" +
            "      <div style=\"flex:1\" />\n" +
            "      <el-button type=\"primary\" @click=\"handleAdd\">新增</el-button>\n" +
            "    </div>\n" +
            "    <div class=\"table-container\">\n" +
            "      <el-table :data=\"tableData\" v-loading=\"loading\" border stripe>\n" +
            "        <el-table-column type=\"index\" width=\"60\" label=\"#\" />\n" +
            "        <!-- TODO: 添加业务列 -->\n" +
            "        <el-table-column prop=\"createTime\" label=\"创建时间\" width=\"180\" />\n" +
            "        <el-table-column label=\"操作\" width=\"180\" fixed=\"right\">\n" +
            "          <template #default=\"{ row }\">\n" +
            "            <el-button link type=\"primary\" @click=\"handleEdit(row)\">编辑</el-button>\n" +
            "            <el-popconfirm title=\"确认删除?\" @confirm=\"handleDelete(row)\">\n" +
            "              <template #reference><el-button link type=\"danger\">删除</el-button></template>\n" +
            "            </el-popconfirm>\n" +
            "          </template>\n" +
            "        </el-table-column>\n" +
            "      </el-table>\n" +
            "      <el-pagination class=\"page-pagination\" v-model:current-page=\"page\" v-model:page-size=\"size\"\n" +
            "        :total=\"total\" :page-sizes=\"[10,20,50]\" layout=\"total,sizes,prev,pager,next\" @change=\"fetchData\" />\n" +
            "    </div>\n" +
            "    <el-dialog v-model=\"dialogVisible\" :title=\"isEdit?'编辑':'新增'\" width=\"500px\">\n" +
            "      <el-form :model=\"form\" label-width=\"100px\">\n" +
            "        <!-- TODO: 添加表单字段 -->\n" +
            "      </el-form>\n" +
            "      <template #footer>\n" +
            "        <el-button @click=\"dialogVisible=false\">取消</el-button>\n" +
            "        <el-button type=\"primary\" @click=\"handleSubmit\">保存</el-button>\n" +
            "      </template>\n" +
            "    </el-dialog>\n" +
            "  </div>\n" +
            "</template>\n\n" +
            "<script setup>\n" +
            "import { ref } from 'vue'\n" +
            "import { ElMessage } from 'element-plus'\n" +
            "import { get" + entityName + "PageApi, add" + entityName + "Api, update" + entityName + "Api, delete" + entityName + "Api } from '@/api/" + pathName + "'\n\n" +
            "const tableData = ref([]); const loading = ref(false); const keyword = ref('')\n" +
            "const page = ref(1); const size = ref(10); const total = ref(0)\n" +
            "const dialogVisible = ref(false); const isEdit = ref(false); const form = ref({})\n\n" +
            "const fetchData = async () => { loading.value = true; try {\n" +
            "  const res = await get" + entityName + "PageApi({ page: page.value, size: size.value, keyword: keyword.value })\n" +
            "  tableData.value = res.data?.records || []; total.value = res.data?.total || 0 \n" +
            "} finally { loading.value = false } }\n\n" +
            "const resetSearch = () => { keyword.value = ''; fetchData() }\n" +
            "const handleAdd = () => { isEdit.value = false; form.value = {}; dialogVisible.value = true }\n" +
            "const handleEdit = (row) => { isEdit.value = true; form.value = { ...row }; dialogVisible.value = true }\n" +
            "const handleDelete = async (row) => { await delete" + entityName + "Api(row.id); ElMessage.success('删除成功'); fetchData() }\n" +
            "const handleSubmit = async () => { \n" +
            "  if (isEdit.value) { await update" + entityName + "Api(form.value) } else { await add" + entityName + "Api(form.value) }\n" +
            "  ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; fetchData()\n" +
            "}\n" +
            "fetchData()\n" +
            "</script>\n";

        // API
        String apiJs = "import request from '@/utils/request'\n" +
            "const BASE = '/" + moduleName + "/" + pathName + "'\n" +
            "export const get" + entityName + "PageApi = params => request.get(BASE+'/page', { params })\n" +
            "export const get" + entityName + "ByIdApi = id => request.get(BASE+'/'+id)\n" +
            "export const add" + entityName + "Api = data => request.post(BASE, data)\n" +
            "export const update" + entityName + "Api = data => request.put(BASE, data)\n" +
            "export const delete" + entityName + "Api = id => request.delete(BASE+'/'+id)\n";

        List<Map<String, Object>> files = List.of(
            Map.of("name", entityName + ".java", "content", entity.toString(), "type", "entity"),
            Map.of("name", entityName + "Mapper.java", "content", mapper, "type", "mapper"),
            Map.of("name", entityName + "Service.java", "content", service, "type", "service"),
            Map.of("name", entityName + "Controller.java", "content", controller, "type", "controller"),
            Map.of("name", "index.vue", "content", vue, "type", "vue"),
            Map.of("name", pathName + ".js", "content", apiJs, "type", "api")
        );

        return Result.ok(Map.of("files", files, "entityName", entityName, "moduleName", moduleName));
    }

    private String toCamel(String name, boolean upperFirst) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = upperFirst;
        for (char c : name.toCharArray()) {
            if (c == '_') { nextUpper = true; continue; }
            sb.append(nextUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
            nextUpper = false;
        }
        return sb.toString();
    }

    private String toPathName(String table) {
        return table.toLowerCase().replace("sys_", "").replace("_", "-");
    }

    private String toJavaType(String dbType) {
        if (dbType == null) return "String";
        return switch (dbType.toLowerCase()) {
            case "bigint" -> "Long";
            case "int", "tinyint", "smallint" -> "Integer";
            case "datetime", "timestamp" -> "java.time.LocalDateTime";
            case "date" -> "java.time.LocalDate";
            case "decimal", "float", "double" -> "java.math.BigDecimal";
            case "text", "longtext", "mediumtext" -> "String";
            default -> "String";
        };
    }
}
