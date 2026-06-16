package com.rx.admin.modules.as400.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.as400.entity.IServiceAuthority;
import com.rx.admin.modules.as400.entity.IServiceCategory;
import com.rx.admin.modules.as400.entity.IServiceColumn;
import com.rx.admin.modules.as400.entity.IServiceExample;
import com.rx.admin.modules.as400.entity.IServiceItem;
import com.rx.admin.modules.as400.entity.IServiceParameter;
import com.rx.admin.modules.as400.mapper.IServiceAuthorityMapper;
import com.rx.admin.modules.as400.mapper.IServiceCategoryMapper;
import com.rx.admin.modules.as400.mapper.IServiceColumnMapper;
import com.rx.admin.modules.as400.mapper.IServiceExampleMapper;
import com.rx.admin.modules.as400.mapper.IServiceItemMapper;
import com.rx.admin.modules.as400.mapper.IServiceParameterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * i-Service 核心服务
 * 管理分类、服务及所有子表数据
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class IServiceCategoryService extends ServiceImpl<IServiceCategoryMapper, IServiceCategory> {

    private final IServiceItemMapper itemMapper;
    private final IServiceParameterMapper paramMapper;
    private final IServiceColumnMapper columnMapper;
    private final IServiceExampleMapper exampleMapper;
    private final IServiceAuthorityMapper authorityMapper;

    /**
     * 查询所有分类及其下的服务列表（含子表）
     */
    public List<IServiceCategory> listWithItems() {
        List<IServiceCategory> categories = list(new LambdaQueryWrapper<IServiceCategory>()
                .orderByAsc(IServiceCategory::getSort));
        for (IServiceCategory category : categories) {
            category.setItems(listItemsByCategory(category.getId()));
        }
        return categories;
    }

    /**
     * 按分类编码获取完整的分类树
     */
    public IServiceCategory getByCodeWithItems(String code) {
        IServiceCategory category = getOne(new LambdaQueryWrapper<IServiceCategory>()
                .eq(IServiceCategory::getCode, code));
        if (category != null) {
            category.setItems(listItemsByCategory(category.getId()));
        }
        return category;
    }

    /**
     * 查询单个服务的完整信息（含所有子表）
     */
    public IServiceItem getItemDetail(Long itemId) {
        IServiceItem item = itemMapper.selectById(itemId);
        if (item != null) {
            fillItemDetails(item);
        }
        return item;
    }

    /**
     * 根据分类ID获取服务列表（基础）
     */
    private List<IServiceItem> listItemsByCategory(Long categoryId) {
        return itemMapper.selectList(new LambdaQueryWrapper<IServiceItem>()
                .eq(IServiceItem::getCategoryId, categoryId)
                .orderByAsc(IServiceItem::getSort));
    }

    /**
     * 为服务填充所有子表数据
     */
    private void fillItemDetails(IServiceItem item) {
        item.setParameters(paramMapper.selectList(
                new LambdaQueryWrapper<IServiceParameter>()
                        .eq(IServiceParameter::getServiceId, item.getId())
                        .orderByAsc(IServiceParameter::getSort)));
        item.setColumns(columnMapper.selectList(
                new LambdaQueryWrapper<IServiceColumn>()
                        .eq(IServiceColumn::getServiceId, item.getId())
                        .orderByAsc(IServiceColumn::getSort)));
        item.setExamples(exampleMapper.selectList(
                new LambdaQueryWrapper<IServiceExample>()
                        .eq(IServiceExample::getServiceId, item.getId())
                        .orderByAsc(IServiceExample::getSort)));
        item.setAuthorities(authorityMapper.selectList(
                new LambdaQueryWrapper<IServiceAuthority>()
                        .eq(IServiceAuthority::getServiceId, item.getId())
                        .orderByAsc(IServiceAuthority::getSort)));
    }
}
