package com.xl.tree.builddemo;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author tanjl11
 * @date 2022/01/11 11:30
 */
public interface PathNode<T extends PathNode<T>> {
    /**
     * 数据库的id
     *
     * @return
     */
    Long dataBaseId();

    /**
     * 父节点
     *
     * @return
     */
    Serializable pid();

    /**
     * 当前节点
     *
     * @return
     */
    Serializable nodeId();

    /**
     * 子节点
     *
     * @return
     */
    List<T> getSubNodes();

    /**
     * 是否末尾
     *
     * @return
     */
    boolean isLeaf();

    /**
     * 层级，查询时候用
     *
     * @return
     */
    int level();

    /**
     * 路径，查询时候用
     */
    String fullPath();

    /**
     * 设置子节点
     *
     * @param sons
     */
    void setSubNodes(List<T> sons);

    /**
     * 生失效时间,主要是在构建树{@link #build(PathNode, Collection, Predicate, Together)}时用于判断时间是否符合
     *
     * @return
     */
    Date from();

    Date to();

    /**
     * 构造树
     *
     * @param cur        当前处理节点
     * @param candidates 候选节点
     * @param sonFilter  过滤条件
     * @param together   父类和字类
     * @param <T>
     * @return
     */
    static <T extends PathNode<T>> T build(T cur, Collection<T> candidates, Predicate<T> sonFilter, Together<T, T> together) {
        if (cur.isLeaf()) {
            return cur;
        }
        if (candidates.stream().anyMatch(e -> e.nodeId().equals(e.pid()))) {
            throw new IllegalStateException("root should exclude from candidates");
        }
        List<T> son = new ArrayList<>();
        for (T candidate : candidates) {
            //候选的父id=当前处理的节点id
            if (candidate.pid().equals(cur.nodeId())) {
                boolean pass = true;
                if (sonFilter != null) {
                    pass = sonFilter.test(candidate);
                }
                if (together != null) {
                    pass = pass && together.accept(cur, candidate);
                }
                if (pass) {
                    son.add(build(candidate, candidates, sonFilter, together));
                }
            }
        }
        cur.setSubNodes(son);
        return cur;
    }

    /**
     * 深度遍历
     *
     * @param treeBin
     * @param consumer
     * @param <T>
     */
    static <T extends PathNode<T>> void foreachInPreOrder(T treeBin, Consumer<T> consumer) {
        foreachInDFS(treeBin, e -> {
            consumer.accept(e);
            return true;
        }, null);
    }

    static <T extends PathNode<T>> void foreachInDFS(T treeBin, Function<T, Boolean> pre, Consumer<T> post) {
        if (pre != null) {
            Boolean isContinue = pre.apply(treeBin);
            if (!isContinue) {
                return;
            }
        }
        List<T> nodes = treeBin.getSubNodes();
        if (nodes != null && !nodes.isEmpty()) {
            for (T node : nodes) {
                foreachInDFS(node, pre, post);
            }
        }
        if (post != null) {
            post.accept(treeBin);
        }

    }

    static <T extends PathNode<T>> void foreachInPostOrder(T treeBin, Consumer<T> consuemr) {
        foreachInDFS(treeBin, null, consuemr);
    }

    /**
     * 广度遍历
     *
     * @param consumer
     * @param treeBin
     * @param <T>
     */
    static <T extends PathNode<T>> void foreachInBfs(Consumer<T> consumer, T treeBin) {
        Queue<T> queue = new LinkedList<>();
        queue.offer(treeBin);
        while (!(queue.isEmpty())) {
            int curSize = queue.size();
            for (int i = 0; i < curSize; i++) {
                T poll = queue.poll();
                consumer.accept(poll);
                if (poll != null && poll.getSubNodes() != null) {
                    poll.getSubNodes().forEach(queue::offer);
                }
            }
        }
    }

    /**
     * 树里面唯一属性
     *
     * @return
     */
    default String getUniqueKey() {
        return fullPath();
    }

    @FunctionalInterface
    interface Together<P, S> {
        /**
         * 父，子处理
         *
         * @param parent
         * @param son
         */
        boolean accept(P parent, S son);
    }

}
