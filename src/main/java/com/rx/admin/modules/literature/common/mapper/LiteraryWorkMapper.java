package com.rx.admin.modules.literature.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.framework.datasource.SecondDB;
import com.rx.admin.modules.literature.common.entity.LiteraryWork;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@SecondDB
public interface LiteraryWorkMapper extends BaseMapper<LiteraryWork> {

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM literary_work")
    long sumViewCount();

    @Select("SELECT COALESCE(SUM(word_count), 0) FROM literary_work")
    long sumWordCount();

    @Select("SELECT dynasty_id, COUNT(*) as cnt FROM literary_work WHERE dynasty_id IS NOT NULL GROUP BY dynasty_id")
    List<Map<String, Object>> countByDynasty();

    @Select("SELECT genre_id, COUNT(*) as cnt FROM literary_work WHERE genre_id IS NOT NULL GROUP BY genre_id")
    List<Map<String, Object>> countByGenre();

    @Select("SELECT source, COUNT(*) as cnt FROM literary_work WHERE source IS NOT NULL AND source != '' GROUP BY source")
    List<Map<String, Object>> countBySource();

    @Select("SELECT difficulty_level, COUNT(*) as cnt FROM literary_work WHERE difficulty_level IS NOT NULL GROUP BY difficulty_level")
    List<Map<String, Object>> countByDifficultyLevel();

    @Select("SELECT author_id, COUNT(*) as cnt FROM literary_work WHERE author_id IS NOT NULL GROUP BY author_id")
    List<Map<String, Object>> countByAuthor();
}
