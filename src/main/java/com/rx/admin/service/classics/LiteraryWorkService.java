package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.classics.Author;
import com.rx.admin.entity.classics.Dynasty;
import com.rx.admin.entity.classics.Genre;
import com.rx.admin.entity.classics.LiteraryWork;
import com.rx.admin.mapper.classics.AuthorMapper;
import com.rx.admin.mapper.classics.DynastyMapper;
import com.rx.admin.mapper.classics.GenreMapper;
import com.rx.admin.mapper.classics.LiteraryWorkMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LiteraryWorkService extends ServiceImpl<LiteraryWorkMapper, LiteraryWork> {

    private final AuthorMapper authorMapper;
    private final DynastyMapper dynastyMapper;
    private final GenreMapper genreMapper;

    public LiteraryWorkService(AuthorMapper authorMapper,
                               DynastyMapper dynastyMapper,
                               GenreMapper genreMapper) {
        this.authorMapper = authorMapper;
        this.dynastyMapper = dynastyMapper;
        this.genreMapper = genreMapper;
    }

    /**
     * 分页查询（支持按标题/关键词模糊搜索 + 朝代/体裁/作者筛选）
     */
    public PageResult<LiteraryWork> pageQuery(int page, int size, String keyword,
                                               Long dynastyId, Long genreId, Long authorId) {
        LambdaQueryWrapper<LiteraryWork> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(LiteraryWork::getTitle, keyword)
                    .or().like(LiteraryWork::getKeywords, keyword)
                    .or().like(LiteraryWork::getSummary, keyword));
        }
        if (dynastyId != null) {
            wrapper.eq(LiteraryWork::getDynastyId, dynastyId);
        }
        if (genreId != null) {
            wrapper.eq(LiteraryWork::getGenreId, genreId);
        }
        if (authorId != null) {
            wrapper.eq(LiteraryWork::getAuthorId, authorId);
        }
        wrapper.orderByAsc(LiteraryWork::getSortOrder, LiteraryWork::getId);
        IPage<LiteraryWork> iPage = page(new Page<>(page, size), wrapper);

        // 填充关联名称
        fillAssociations(iPage.getRecords());

        return PageResult.of(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * 获取单个作品详情（含关联名称）
     */
    public LiteraryWork getDetail(Long id) {
        LiteraryWork work = getById(id);
        if (work != null) {
            fillAssociation(work);
        }
        return work;
    }

    /**
     * 统计正文真实字数（去除HTML标签和空白字符）
     */
    private int countRealWords(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        // 去除HTML标签
        String plainText = content.replaceAll("<[^>]+>", "");
        // 去除所有空白字符（空格、换行、制表等）
        plainText = plainText.replaceAll("\\s+", "");
        // 去除标点符号（中文和英文标点）
        plainText = plainText.replaceAll("[\\p{P}\\p{S}]", "");
        return plainText.length();
    }

    /**
     * 保存前清理：将空字符串的JSON字段置为null，避免MySQL JSON列报错
     */
    private void cleanJsonFields(LiteraryWork work) {
        if (work.getAnnotations() != null && work.getAnnotations().isEmpty()) {
            work.setAnnotations(null);
        }
    }

    /**
     * 新增作品（自动统计字数）
     */
    public boolean saveWithWordCount(LiteraryWork work) {
        cleanJsonFields(work);
        if (work.getContent() != null) {
            work.setWordCount(countRealWords(work.getContent()));
        }
        return save(work);
    }

    /**
     * 更新作品（自动统计字数）
     */
    public boolean updateWithWordCount(LiteraryWork work) {
        cleanJsonFields(work);
        if (work.getContent() != null) {
            work.setWordCount(countRealWords(work.getContent()));
        }
        return updateById(work);
    }

    /**
     * 批量重新统计所有作品的真实字数并更新到数据库
     */
    public int recountAllWordCount() {
        List<LiteraryWork> allWorks = list();
        int updated = 0;
        for (LiteraryWork work : allWorks) {
            int realCount = countRealWords(work.getContent());
            if (!Integer.valueOf(realCount).equals(work.getWordCount())) {
                work.setWordCount(realCount);
                updateById(work);
                updated++;
            }
        }
        return updated;
    }

    /**
     * 获取所有作品
     */
    public List<LiteraryWork> listAll() {
        LambdaQueryWrapper<LiteraryWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(LiteraryWork::getSortOrder, LiteraryWork::getId);
        List<LiteraryWork> list = list(wrapper);
        fillAssociations(list);
        return list;
    }

    /**
     * 批量填充关联名称
     */
    private void fillAssociations(List<LiteraryWork> list) {
        if (list.isEmpty()) return;

        // 收集所有关联ID
        List<Long> authorIds = list.stream().map(LiteraryWork::getAuthorId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> dynastyIds = list.stream().map(LiteraryWork::getDynastyId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> genreIds = list.stream().map(LiteraryWork::getGenreId).filter(id -> id != null).distinct().collect(Collectors.toList());

        // 批量查询关联表
        Map<Long, String> authorMap = authorIds.isEmpty() ? Map.of() :
                authorMapper.selectBatchIds(authorIds).stream().collect(Collectors.toMap(Author::getId, Author::getName, (a, b) -> a));
        Map<Long, String> dynastyMap = dynastyIds.isEmpty() ? Map.of() :
                dynastyMapper.selectBatchIds(dynastyIds).stream().collect(Collectors.toMap(Dynasty::getId, Dynasty::getName, (a, b) -> a));
        Map<Long, String> genreMap = genreIds.isEmpty() ? Map.of() :
                genreMapper.selectBatchIds(genreIds).stream().collect(Collectors.toMap(Genre::getId, Genre::getName, (a, b) -> a));

        // 填充
        for (LiteraryWork work : list) {
            work.setAuthorName(work.getAuthorId() != null ? authorMap.get(work.getAuthorId()) : null);
            work.setDynastyName(work.getDynastyId() != null ? dynastyMap.get(work.getDynastyId()) : null);
            work.setGenreName(work.getGenreId() != null ? genreMap.get(work.getGenreId()) : null);
        }
    }

    /**
     * 按朝代统计作品数量
     */
    public Map<Long, Long> countByDynasty() {
        List<LiteraryWork> works = list();
        return works.stream()
                .filter(w -> w.getDynastyId() != null)
                .collect(Collectors.groupingBy(LiteraryWork::getDynastyId, Collectors.counting()));
    }

    /**
     * 按体裁统计作品数量
     */
    public Map<Long, Long> countByGenre() {
        List<LiteraryWork> works = list();
        return works.stream()
                .filter(w -> w.getGenreId() != null)
                .collect(Collectors.groupingBy(LiteraryWork::getGenreId, Collectors.counting()));
    }

    /**
     * 按来源统计作品数量
     */
    public Map<String, Long> countBySource() {
        List<LiteraryWork> works = list();
        return works.stream()
                .filter(w -> w.getSource() != null && !w.getSource().isEmpty())
                .collect(Collectors.groupingBy(LiteraryWork::getSource, Collectors.counting()));
    }

    /**
     * 按难度等级统计作品数量
     */
    public Map<Integer, Long> countByDifficultyLevel() {
        List<LiteraryWork> works = list();
        return works.stream()
                .filter(w -> w.getDifficultyLevel() != null)
                .collect(Collectors.groupingBy(LiteraryWork::getDifficultyLevel, Collectors.counting()));
    }

    /**
     * 按作者统计作品数量（Top作品数排行）
     */
    public Map<Long, Long> countByAuthor() {
        List<LiteraryWork> works = list();
        return works.stream()
                .filter(w -> w.getAuthorId() != null)
                .collect(Collectors.groupingBy(LiteraryWork::getAuthorId, Collectors.counting()));
    }

    /**
     * 获取浏览量Top排行（按viewCount降序）
     */
    public List<LiteraryWork> topByViewCount(int limit) {
        LambdaQueryWrapper<LiteraryWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LiteraryWork::getViewCount);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }

    /**
     * 获取总字数
     */
    public long sumWordCount() {
        List<LiteraryWork> works = list();
        return works.stream()
                .filter(w -> w.getWordCount() != null)
                .mapToLong(LiteraryWork::getWordCount)
                .sum();
    }

    /**
     * 获取总浏览量
     */
    public long sumViewCount() {
        List<LiteraryWork> works = list();
        return works.stream()
                .filter(w -> w.getViewCount() != null)
                .mapToLong(LiteraryWork::getViewCount)
                .sum();
    }

    private void fillAssociation(LiteraryWork work) {
        if (work.getAuthorId() != null) {
            Author author = authorMapper.selectById(work.getAuthorId());
            if (author != null) work.setAuthorName(author.getName());
        }
        if (work.getDynastyId() != null) {
            Dynasty dynasty = dynastyMapper.selectById(work.getDynastyId());
            if (dynasty != null) work.setDynastyName(dynasty.getName());
        }
        if (work.getGenreId() != null) {
            Genre genre = genreMapper.selectById(work.getGenreId());
            if (genre != null) work.setGenreName(genre.getName());
        }
    }
}
