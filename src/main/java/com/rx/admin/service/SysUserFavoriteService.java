package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.entity.SysUserFavorite;
import com.rx.admin.mapper.SysUserFavoriteMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserFavoriteService extends ServiceImpl<SysUserFavoriteMapper, SysUserFavorite> {

    public List<SysUserFavorite> getUserFavorites(Long userId) {
        LambdaQueryWrapper<SysUserFavorite> w = new LambdaQueryWrapper<>();
        w.eq(SysUserFavorite::getUserId, userId).orderByAsc(SysUserFavorite::getSortOrder);
        return list(w);
    }

    public SysUserFavorite toggleFavorite(Long userId, String name, String path, String icon, Long menuId) {
        LambdaQueryWrapper<SysUserFavorite> w = new LambdaQueryWrapper<>();
        w.eq(SysUserFavorite::getUserId, userId).eq(SysUserFavorite::getPath, path);
        SysUserFavorite exist = getOne(w);
        if (exist != null) {
            removeById(exist.getId());
            return null;
        }
        SysUserFavorite f = new SysUserFavorite();
        f.setUserId(userId);
        f.setName(name);
        f.setPath(path);
        f.setIcon(icon);
        f.setMenuId(menuId);
        f.setSortOrder(0);
        save(f);
        return f;
    }

    public void updateSort(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            SysUserFavorite f = new SysUserFavorite();
            f.setId(ids.get(i));
            f.setSortOrder(i);
            updateById(f);
        }
    }
}
