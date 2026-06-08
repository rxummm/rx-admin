package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.classics.Author;
import com.rx.admin.mapper.classics.AuthorMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthorService extends ServiceImpl<AuthorMapper, Author> {

    public PageResult<Author> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<Author> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Author::getName, keyword)
                    .or().like(Author::getCourtesyName, keyword)
                    .or().like(Author::getPseudonym, keyword)
                    .or().like(Author::getBiography, keyword));
        }
        wrapper.orderByAsc(Author::getSortOrder, Author::getId);
        IPage<Author> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * 按朝代统计作者数量
     */
    public Map<Long, Long> countByDynasty() {
        List<Author> authors = list();
        return authors.stream()
                .filter(a -> a.getDynastyId() != null)
                .collect(Collectors.groupingBy(Author::getDynastyId, Collectors.counting()));
    }

    public List<Author> listAll() {
        LambdaQueryWrapper<Author> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Author::getSortOrder, Author::getId);
        return list(wrapper);
    }
}
