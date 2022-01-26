package com.xl.tree.builddemo;


import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author tanjl11
 * @date 2022/01/11 11:41
 * 该类作为示例
 * F 邻接表
 * T 链路
 * 核心方法是树构建{@link #doBuildBom(AdjacencyList)} 层序构建树
 * 和树展示{@link #baseQuery(PathNode, Collection, Date)} 递归构建树
 * 树修改较为麻烦,目前建议定时更新bom结构，即删除-重建，如果需要近实时处理结构，需要做到快速的路径构建和修改或更换适配邻接表查询的数据源
 * {@link PathNode}该类的foreach方法用于遍历树
 * 其他方法交由子类实现
 */
public abstract class PathNodeTemplate<F extends AdjacencyList<F>, T extends PathNode<T>> {
    private static ThreadLocal<Integer> curLevel = new ThreadLocal<>();

    /**
     * 查找邻接表子类
     *
     * @param parent
     * @return
     */
    protected abstract List<F> findChildrenByParent(Collection<F> parent, Map<String, Object> auxMap);

    /**
     * 处理新增pathNode
     *
     * @param newNodes
     * @param auxMap
     */
    protected abstract void dealWithNewNodes(Collection<T> newNodes, Map<String, Object> auxMap);

    /**
     * 处理尾节点pathNode,主要更新是否末尾节点的信息,用于修改
     *
     * @param updateNode
     * @param auxMap
     */
    protected abstract void dealWithEndNodes(Collection<T> updateNode, Map<String, Object> auxMap);

    /**
     * 转成pathNode
     * 属性赋值
     * 返回空值不处理
     * @param parent
     * @param cur
     * @param auxMap
     * @return
     */
    protected abstract T convertToPathNode(F parent, F cur, Map<String, Object> auxMap);

    /**
     * 返回空值不处理
     * @param endNode
     * @param auxMap
     * @return
     */
    protected T handleEndNode(T endNode, Map<String, Object> auxMap) {
        return endNode;
    }

    /**
     * subBomMap存放 父类标识-子集
     * 通过子类转成父类标识用于获取子集得参数
     *
     * @param node
     */
    protected Serializable getParentSign(F node) {
        return node.pid();
    }

    /**
     * subBomMap存放 父类标识-子集
     * 通过本身转成父类标识用于获取子集得参数
     *
     * @return
     */
    protected Serializable getChildrenSign(F cur) {
        return cur.nodeId();
    }

    protected static Integer getBuildingLevel() {
        return curLevel.get()+1;
    }

    public static boolean beforeOrEquals(Date source, Date other) {
        return source.before(other) || source.equals(other);
    }

    public static boolean afterOrEquals(Date source, Date other) {
        return source.after(other) || source.equals(other);
    }
    /**
     * 最基础的树级展示
     *
     * @param root
     * @param sub
     * @param cur
     * @return
     */
    public T baseQuery(T root, Collection<T> sub, Date cur) {
        return PathNode.build(root, sub, new Predicate<T>() {
            @Override
            public boolean test(T t) {
                boolean past = true;
                if (cur != null) {
                    Date to = t.to();
                    Date from = t.from();
                    //失效日期判断，1.数据的失效日期必晚于查询的失效日期
                    past = (to == null || afterOrEquals(to, cur)
                            //开始日期判断，1.数据的生效日期必早于查询日期
                            && (from == null || beforeOrEquals(from, cur)));
                }
                return past;
            }
        }, new PathNode.Together<T, T>() {
            @Override
            public boolean accept(T parent, T son) {
                if (son.level() > 1) {
                    //过滤掉父类相等但爷爷失效的节点
                    return son.fullPath().startsWith(parent.fullPath());
                }
                return true;
            }
        });
    }



    /**
     * 传入邻接表根节点,以下是基本的处理流程
     *
     * @param root 根节点
     */
    public Collection<T> doBuildBom(F root) {
        Map<String, Object> auxMap = new HashMap<>();
        HashSet<String> handleSet = new HashSet<>();
        if (root.fullPath() == null) {
            root.setFullPath(root.nodeId() + "");
        }
        Queue<F> queue = new LinkedList<>();
        queue.offer(root);
        int level = 0;
        //存放已经处理的邻接表信息
        Map<String, Long> hasHandledMap = new HashMap<>(1500);
        //本次bom生成
        Map<Long, T> addMap = new HashMap<>(1500);
        List<T> updateList = new ArrayList<>(500);
        while (!queue.isEmpty()) {
            curLevel.set(level);
            //先获取下一层的子节点
            List<F> children = findChildrenByParent(queue, auxMap);
            //当前层要处理的元素和子集的映射
            Map<Serializable, List<F>> subBom = new HashMap<>(queue.size());
            for (F child : children) {
                Serializable subBomSign = getParentSign(child);
                //如果邻接表元素尚未处理，进行处理
                if (handleSet.add(child.getUniqueKey())) {
                    List<F> sub = subBom.get(subBomSign);
                    if (sub == null) {
                        sub = new ArrayList<>(100);
                    }
                    sub.add(child);
                    subBom.put(subBomSign, sub);
                }
            }
            //构建好子集后，对子类进行处理
            int curSize = queue.size();
            for (int i = 0; i < curSize; i++) {
                F curParent = queue.poll();
                //addMap存放应该处理的数据
                if (level > 0) {
                    T bom = addMap.get(curParent.pathDataBaseId());
                    //如果当前是空的，不符合业务条件，不再进行展开
                    if (bom == null) {
                        continue;
                    }
                }
                List<F> subList = subBom.get(getChildrenSign(curParent));
                if (subList == null || subList.isEmpty()) {
                    //末尾节点
                    Long pathId = curParent.pathDataBaseId();
                    T t = addMap.get(pathId);
                    T t1 = handleEndNode(t, auxMap);
                    if (t1 == null) {
                        addMap.remove(pathId);
                    } else {
                        updateList.add(t1);
                    }
                    continue;
                }
                for (F f : subList) {
                    //生成一个路径节点
                    f.setFullPath(curParent.fullPath() + "|" + f.nodeId());
                    T pathNode = convertToPathNode(curParent, f, auxMap);
                    if(pathNode!=null){
                        String uniqueKey = pathNode.getUniqueKey();
                        if (!hasHandledMap.containsKey(uniqueKey)) {
                            hasHandledMap.put(uniqueKey, pathNode.dataBaseId());
                            addMap.put(pathNode.dataBaseId(), pathNode);
                            queue.offer(f);
                        } else {
                            //处理重复的情况
                            f.setPathDataBaseId(hasHandledMap.get(uniqueKey));
                        }
                    }
                }
            }
            subBom.clear();
            level++;
        }
        //处理完后,如果单个派生的物料很多，可以分批插入
        if (!addMap.isEmpty()) {
            List<T> temp = new ArrayList<>(1000);
            for (T value : addMap.values()) {
                temp.add(value);
                if (temp.size() == 1000) {
                    dealWithNewNodes(temp, auxMap);
                    temp.clear();
                }
            }
            if (!temp.isEmpty()) {
                dealWithNewNodes(temp, auxMap);
                temp.clear();
            }
        }
        if (!updateList.isEmpty()) {
            List<T> temp = new ArrayList<>(1000);
            for (T value : updateList) {
                temp.add(value);
                if (temp.size() == 1000) {
                    dealWithEndNodes(temp, auxMap);
                    temp.clear();
                }
            }
            if (!temp.isEmpty()) {
                dealWithEndNodes(updateList, auxMap);
                temp.clear();
            }
        }
        auxMap.clear();
        curLevel.remove();
        return addMap.values();
    }
}
