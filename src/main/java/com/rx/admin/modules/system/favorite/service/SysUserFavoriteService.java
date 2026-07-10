package com.rx.admin.modules.system.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.system.favorite.entity.SysUserFavorite;
import com.rx.admin.modules.system.favorite.mapper.SysUserFavoriteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@SuppressWarnings("null")
public class SysUserFavoriteService extends ServiceImpl<SysUserFavoriteMapper, SysUserFavorite> {

    public List<SysUserFavorite> getUserFavorites(Long userId) {
        LambdaQueryWrapper<SysUserFavorite> w = new LambdaQueryWrapper<>();
        w.eq(SysUserFavorite::getUserId, userId).orderByAsc(SysUserFavorite::getSortOrder);
        return list(w);
    }

    public SysUserFavorite toggleFavorite(Long userId, String name, String path, String icon, Long menuId) {
        try {
            LambdaQueryWrapper<SysUserFavorite> w = new LambdaQueryWrapper<>();
            w.eq(SysUserFavorite::getUserId, userId).eq(SysUserFavorite::getPath, path);
            // 使用 getOne(w, false) 避免多条记录时抛异常
            SysUserFavorite exist = getOne(w, false);
            if (exist != null) {
                // 按条件删除而非按 ID 删除，避免 id 不匹配或重复记录导致的问题
                boolean removed = remove(w);
                log.info("取消收藏: userId={}, path={}, removed={}, existId={}", userId, path, removed, exist.getId());
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
            log.info("添加收藏: userId={}, path={}, id={}", userId, path, f.getId());
            return f;
        } catch (Exception e) {
            log.error("toggleFavorite异常: userId={}, path={}", userId, path, e);
            throw e;
        }
    }

    public void updateSort(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            SysUserFavorite f = new SysUserFavorite();
            f.setId(ids.get(i));
            f.setSortOrder(i);
            updateById(f);
        }
    }

    public void deleteFavoriteBatch(List<Long> ids) {
        removeByIds(ids);
    }
}
