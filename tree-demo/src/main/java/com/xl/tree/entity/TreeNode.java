package com.xl.tree.entity;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.xl.mphelper.util.BeanCopyUtil;
import org.slf4j.MDC;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.objenesis.ObjenesisStd;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author tanjl11
 * @date 2021/09/27 14:32
 */
public interface TreeNode<T extends TreeNode<T>> {
    /**
     * 父节点
     *
     * @return
     */
    Serializable getTreePid();

    /**
     * 当前节点
     *
     * @return
     */
    Serializable getTreeId();

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
     * 设置子节点
     *
     * @param sons
     */
    void setSubNodes(List<T> sons);

    static <T extends TreeNode<T>> T build(T cur, Collection<T> candidates, Predicate<T> sonFilter, Together<T, T> together) {
        return build(cur, candidates, sonFilter, together, false);
    }

    /**
     * 构造树
     *
     * @param cur 当前处理节点
     * @param candidates 候选节点
     * @param sonFilter 过滤条件
     * @param together 父类和字类
     * @param <T>
     * @return
     */
    static <T extends TreeNode<T>> T build(T cur, Collection<T> candidates, Predicate<T> sonFilter, Together<T, T> together, boolean needClone) {
        if (cur.isLeaf()) {
            return cur;
        }
        if (candidates.stream().anyMatch(e -> e.getTreeId().equals(e.getTreePid()))) {
            throw new IllegalStateException("root should exclude from candidates");
        }
        List<T> son = new ArrayList<>();
        for (T candidate : candidates) {
           //候选的父id=当前处理的节点id
            if (candidate.getTreePid().equals(cur.getTreeId())) {
                T point = candidate;
                if (needClone) {
                    point = BeanCopyUtil.copy(candidate, candidate.getClass());
                }
                boolean pass = true;
                if (sonFilter != null) {
                    pass = sonFilter.test(point) && pass;
                }
                if (pass) {
                    if (together != null) {
                        pass=together.accept(cur, point);
                    }
                    if(pass){
                        son.add(build(point, candidates, sonFilter, together, needClone));
                    }
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
    static <T extends TreeNode<T>> void foreachInPreOrder(T treeBin, Consumer<T> consumer) {
        foreachInDFS(treeBin, e -> {
            consumer.accept(e);
            return true;
        }, null);
    }

    static <T extends TreeNode<T>> void foreachInDFS(T treeBin, Function<T, Boolean> pre, Consumer<T> post) {
        if (pre != null) {
            Boolean isContinue = pre.apply(treeBin);
            if (!isContinue) {
                return;
            }
        }
        List<T> nodes = treeBin.getSubNodes();
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (T node : nodes) {
                foreachInDFS(node, pre, post);
            }
        }
        if (post != null) {
            post.accept(treeBin);
        }

    }

    static <T extends TreeNode<T>> void foreachInPostOrder(T treeBin, Consumer<T> consuemr) {
        foreachInDFS(treeBin, null, consuemr);
    }

    /**
     * 广度遍历
     *
     * @param consumer
     * @param treeBin
     * @param <T>
     */
    static <T extends TreeNode<T>> void foreachInBfs(Consumer<T> consumer, T treeBin) {
        Queue<T> queue = new LinkedList<>();
        queue.offer(treeBin);
        while (!(queue.isEmpty())) {
            int curSize = queue.size();
            for (int i = 0; i < curSize; i++) {
                T poll = queue.poll();
                consumer.accept(poll);
                if (CollectionUtils.isNotEmpty(poll.getSubNodes())) {
                    poll.getSubNodes().forEach(queue::offer);
                }
            }
        }
    }

    /**
     * 找最近的父节点
     *
     * @param cur
     * @param node1
     * @param node2
     * @param <T>
     * @return
     */
    static <T extends TreeNode<T>> T findNearlyFather(T cur, T node1, T node2) {
        if (cur == null) {
            return null;
        }
        if (node1.getUniqueKey().equals(cur.getUniqueKey()) || node2.getUniqueKey().equals(cur.getUniqueKey())) {
            return cur;
        }
        List<T> subNodes = cur.getSubNodes();
        if (!CollectionUtils.isEmpty(subNodes)) {
            T v1 = null;
            T v2 = null;
            String visitedKey = null;
            for (T subNode : subNodes) {
                v1 = findNearlyFather(subNode, node1, node2);
                if (v1 != null) {
                    visitedKey = v1.getUniqueKey();
                    break;
                }
            }
            for (T subNode : subNodes) {
                if (!visitedKey.equals(subNode.getUniqueKey())) {
                    v2 = findNearlyFather(subNode, node1, node2);
                    if (v2 != null) {
                        //防止父节点拿了
                        if (v2.getUniqueKey().equals(v1.getUniqueKey())) {
                            v2 = null;
                        }
                        break;
                    }
                }
            }
            if (v1 != null && v2 != null) {
                return cur;
            }
            if (v1 == null) {
                return v2;
            }
            if (v2 == null) {
                return v1;
            }
        }
        return null;
    }


    static <T extends TreeNode<T>> boolean isSubTree(T root, T sub) {
        return isSubTree(root, sub, false, null);
    }

    static <T extends TreeNode<T>> boolean isSameTree(T one, T two) {
        return isSameTree(one, two, false, null);
    }

    /**
     * 是否子树
     *
     * @param root
     * @param sub
     * @param isSort
     * @param sort
     * @param <T>
     * @return
     */
    static <T extends TreeNode<T>> boolean isSubTree(T root, T sub, boolean isSort, Comparator<T> sort) {
        if (sub == null) {
            return true;
        }
        if (root == null) {
            return false;
        }
        if (isSameTree(root, sub, isSort, sort)) {
            return true;
        }
        List<T> subNodes = root.getSubNodes();
        if (CollectionUtils.isEmpty(subNodes)) {
            return false;
        }
        for (T subNode : subNodes) {
            boolean subTree = isSubTree(subNode, sub, isSort, sort);
            if (!subTree) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否是同一个颗树
     *
     * @param one    根节点1
     * @param two    根节点2
     * @param isSort 是否对子集排序
     * @param sort   排序策略
     * @param <T>
     * @return
     */
    static <T extends TreeNode<T>> boolean isSameTree(T one, T two, boolean isSort, Comparator<T> sort) {
        if (one == null && two == null) {
            return true;
        }
        if (one == null || two == null) {
            return false;
        }
        if (!one.getTreeId().equals(two.getTreeId()) && !one.getTreePid().equals(two.getTreePid())) {
            return false;
        }
        List<T> child1 = one.getSubNodes();
        List<T> child2 = two.getSubNodes();
        if (CollectionUtils.isEmpty(child1) && CollectionUtils.isEmpty(child2)) {
            return true;
        }
        if (child1.size() != child2.size()) {
            return false;
        }
        if (isSort) {
            Comparator<T> comparator = sort == null ? (o1, o2) -> {
                String uniqueKey1 = o1.getUniqueKey();
                String uniqueKey2 = o2.getUniqueKey();
                return uniqueKey1.compareTo(uniqueKey2);
            } : sort;
            child1.sort(comparator);
            child2.sort(comparator);
        }
        for (int i = 0; i < child1.size(); i++) {
            T t1 = child1.get(i);
            T t2 = child2.get(i);
            if (!isSameTree(t1, t2, isSort, sort)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 树里面唯一属性
     *
     * @return
     */
    default String getUniqueKey() {
        return getTreeId() + "_" + getTreePid();
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

    static class DemoNode implements TreeNode<DemoNode> {

        private Integer pid;
        private Integer id;
        private List<DemoNode> subNodes;

        @Override
        public Serializable getTreePid() {
            return pid;
        }

        @Override
        public Serializable getTreeId() {
            return id;
        }

        @Override
        public List<DemoNode> getSubNodes() {
            return subNodes;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public void setSubNodes(List<DemoNode> sons) {
            this.subNodes = sons;
        }

        static DemoNode build(Integer id, Integer pid) {
            DemoNode demoNode = new DemoNode();
            demoNode.id = id;
            demoNode.pid = pid;
            return demoNode;
        }

        @Override
        public String toString() {
            return "DemoNode{ id=" + id + "}";
        }
    }


    public static void main(String[] args) {
        List<DemoNode> sums = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            DemoNode temp = DemoNode.build(i, i / 2);
            sums.add(temp);
        }
        DemoNode root = DemoNode.build(0, 0);
        DemoNode root1 = TreeNode.build(root, sums, null, null);
        TreeNode.foreachInPreOrder(root1, System.out::println);
        System.out.println("===============================");
        TreeNode.foreachInBfs(System.out::println, root1);
        boolean sameTree = isSameTree(sums.get(1), sums.get(2));
        System.out.println(sameTree);
        boolean subTree = isSubTree(sums.get(2), sums.get(6));
        System.out.println(subTree);
        DemoNode nearlyFather = findNearlyFather(root1, sums.get(7), sums.get(8));
        System.out.println(nearlyFather);
    }
}
