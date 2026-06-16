package com.rx.admin.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树形结构工具类
 *
 * @author RX Admin
 * @since 2026-06-13
 */
public class TreeUtils {

    private TreeUtils() {}

    /**
     * 构建树形结构
     *
     * @param list          扁平列表
     * @param idGetter      ID 获取器
     * @param pidGetter     父 ID 获取器
     * @param childrenSetter 子节点设置器
     * @param <T>          实体类型
     * @return 树根节点列表（parentId == 0 或 null 的节点）
     */
    public static <T> List<T> buildTree(
            List<T> list,
            Function<T, Long> idGetter,
            Function<T, Long> pidGetter,
            BiConsumer<T, List<T>> childrenSetter) {

        Map<Long, List<T>> parentMap = list.stream()
                .collect(Collectors.groupingBy(pidGetter));
        List<T> roots = new ArrayList<>();
        for (T node : list) {
            Long pid = pidGetter.apply(node);
            if (pid == null || pid == 0) {
                roots.add(node);
            }
            childrenSetter.accept(node, parentMap.getOrDefault(idGetter.apply(node), new ArrayList<>()));
        }
        return roots;
    }

    /**
     * 收集所有子节点 ID（含自身）
     */
    public static <T> List<Long> collectDescendantIds(
            Long id,
            List<T> all,
            Function<T, Long> idGetter,
            Function<T, Long> pidGetter) {

        List<Long> result = new ArrayList<>();
        result.add(id);
        collectChildrenIds(id, all, idGetter, pidGetter, result);
        return result;
    }

    private static <T> void collectChildrenIds(
            Long parentId,
            List<T> all,
            Function<T, Long> idGetter,
            Function<T, Long> pidGetter,
            List<Long> result) {

        for (T node : all) {
            if (parentId.equals(pidGetter.apply(node))) {
                Long childId = idGetter.apply(node);
                result.add(childId);
                collectChildrenIds(childId, all, idGetter, pidGetter, result);
            }
        }
    }
}