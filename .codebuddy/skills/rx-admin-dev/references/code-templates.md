# 代码模板合集

## Entity 模板

```java
package com.rx.admin.entity.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_xxx")
public class SysXxx extends BaseEntity {

    private String name;
    private String description;
    private Integer status;     // 1=启用, 0=禁用
    private Integer sortOrder;  // 排序

}
```

## Mapper 模板

```java
package com.rx.admin.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.entity.system.SysXxx;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysXxxMapper extends BaseMapper<SysXxx> {
    // 简单 CRUD 使用 BaseMapper 内置方法
    // 复杂查询用 @Select 注解, 禁止创建 XML

    @Select("SELECT ... FROM sys_xxx WHERE ...")
    List<SysXxx> customQuery(Long param);
}
```

## Service 接口模板

```java
package com.rx.admin.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.system.SysXxx;
import java.util.List;

public interface SysXxxService extends IService<SysXxx> {

    PageResult<SysXxx> pageQuery(int page, int size, String keyword);

    void add(SysXxx entity);

    void update(SysXxx entity);

    void delete(List<Long> ids);
}
```

## Service 实现模板

```java
package com.rx.admin.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.system.SysXxx;
import com.rx.admin.mapper.system.SysXxxMapper;
import com.rx.admin.service.system.SysXxxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysXxxServiceImpl
        extends ServiceImpl<SysXxxMapper, SysXxx>
        implements SysXxxService {

    private final SysXxxMapper mapper;

    @Override
    public PageResult<SysXxx> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<SysXxx> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysXxx::getName, keyword);
        }
        wrapper.orderByDesc(SysXxx::getCreateTime);

        Page<SysXxx> pageResult = page(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), page, size, pageResult.getRecords());
    }

    @Override
    @Transactional
    public void add(SysXxx entity) {
        save(entity);
    }

    @Override
    @Transactional
    public void update(SysXxx entity) {
        updateById(entity);
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        removeBatchByIds(ids);
    }
}
```

## Controller 模板

```java
package com.rx.admin.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.PageResult;
import com.rx.admin.common.Result;
import com.rx.admin.entity.system.SysXxx;
import com.rx.admin.service.system.SysXxxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "模块名")
@RestController
@RequestMapping("/api/system/xxx")
@RequiredArgsConstructor
public class SysXxxController {

    private final SysXxxService service;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    @SaCheckPermission("system:xxx:query")
    public Result<PageResult<SysXxx>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(service.pageQuery(page, size, keyword));
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("/{id}")
    @SaCheckPermission("system:xxx:query")
    public Result<SysXxx> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    @SaCheckPermission("system:xxx:add")
    public Result<?> add(@RequestBody SysXxx entity) {
        service.add(entity);
        return Result.success();
    }

    @Operation(summary = "修改")
    @PutMapping
    @SaCheckPermission("system:xxx:edit")
    public Result<?> update(@RequestBody SysXxx entity) {
        service.update(entity);
        return Result.success();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:xxx:delete")
    public Result<?> delete(@PathVariable List<Long> ids) {
        service.delete(ids);
        return Result.success();
    }
}
```

## Vue 页面模板

```vue
<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="关键词搜索" clearable style="width: 200px" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="page-pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @change="fetchData"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { xxxApi } from '@/api/xxx'
import { useTablePage } from '@/composables/useTablePage'

defineOptions({ name: 'ModulePage' }) // 必须声明, 用于 keep-alive

const {
  loading, keyword, page, size, total, tableData,
  fetchData, handleSearch
} = useTablePage(xxxApi.getPage)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = reactive({ name: '' })
const rules = reactive({
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
})

const handleAdd = () => {
  dialogTitle.value = '新增'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (form.id) {
      await xxxApi.update(form)
    } else {
      await xxxApi.add(form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  } catch { return }
  await xxxApi.delete([row.id])
  ElMessage.success('删除成功')
  fetchData()
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, { name: '' })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
// 使用 CSS 变量, 不硬编码颜色
</style>
```

## Vue API 模块模板

```javascript
// api/xxx.js
import request from '@/utils/request'

export function getXxxPage(params) {
  return request({ url: '/system/xxx/page', method: 'get', params })
}

export function getXxxById(id) {
  return request({ url: `/system/xxx/${id}`, method: 'get' })
}

export function addXxx(data) {
  return request({ url: '/system/xxx', method: 'post', data })
}

export function updateXxx(data) {
  return request({ url: '/system/xxx', method: 'put', data })
}

export function deleteXxx(ids) {
  return request({ url: `/system/xxx/${ids}`, method: 'delete' })
}
```
