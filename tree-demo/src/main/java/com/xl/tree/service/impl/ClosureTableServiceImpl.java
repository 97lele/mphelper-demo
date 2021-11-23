package com.xl.tree.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xl.tree.entity.*;
import com.xl.tree.mapper.ClosureTableMapper;
import com.xl.tree.mapper.ClosureTableRefMapper;
import com.xl.tree.service.TreeNodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author tanjl11
 * @date 2021/11/22 14:51
 * 闭包表
 */
@Service
public class ClosureTableServiceImpl implements TreeNodeService<ClosureTable> {
    @Resource
    private ClosureTableMapper closureTableMapper;
    @Resource
    private ClosureTableRefMapper closureTableRefMapper;

    @Override
    public void insertNodes(Collection<AdjacencyList> lists) {
        List<AdjacencyList> roots = new ArrayList<>();
        List<ClosureTable> tables = new ArrayList<>(lists.size());
        Iterator<AdjacencyList> iterator = lists.iterator();
        while (iterator.hasNext()) {
            AdjacencyList cur = iterator.next();
            tables.add(ClosureTable.builder()
                    .pid(cur.getPid())
                    .nodeId(cur.getNodeId())
                    .content(cur.getContent()).build()
            );
            if (cur.getNodeId().equals(cur.getPid())) {
                roots.add(cur);
                iterator.remove();
            }
        }
        for (AdjacencyList p : lists) {
            boolean isEnd = true;
            for (AdjacencyList s : lists) {
                if (s.getPid().equals(p.getNodeId())) {
                    isEnd = false;
                    break;
                }
            }
            p.setEnd(isEnd);
        }
        List<ClosureTableRef> refs = new ArrayList<>();
        Set<String> visitedSet = new HashSet<>();
        for (AdjacencyList root : roots) {
            root.setFullPath(root.getNodeId() + "");
            root.setLevel(0);
            //构建树的同时，把全路径赋值
            TreeNode.build(root, lists, null, (p, s) -> {
                s.setFullPath(p.getFullPath() + "|" + s.getNodeId());
                s.setLevel(p.getLevel() + 1);
            }, true);
            TreeNode.foreachInBfs(e -> {
                //判断之前有没有走过这个节点
                if (visitedSet.add(e.getNodeId() + "")) {
                    TreeNode.foreachInPreOrder(e, q -> {
                        if (!e.getNodeId().equals(q.getNodeId())) {
                            ClosureTableRef ref = ClosureTableRef.builder()
                                    .rootId(e.getNodeId())
                                    .isLeaf(q.isEnd())
                                    .pid(q.getPid())
                                    .depth(q.getLevel() - e.getLevel())
                                    .nodeId(q.getNodeId())
                                    .build();
                            refs.add(ref);
                        }
                    });
                }
            }, root);
        }
        closureTableMapper.insertBatch(tables);
        closureTableRefMapper.insertBatch(refs);
    }

    @Override
    public List<ClosureTable> queryChildren(Long parentId) {
        return closureTableMapper.queryChildren(parentId);
    }

    @Override
    public List<ClosureTable> queryAllChildren(Long parentId) {
        List<ClosureTableRef> closureTableRefs = closureTableRefMapper.selectList(Wrappers.lambdaQuery(ClosureTableRef.class)
                .eq(ClosureTableRef::getRootId, parentId)
        );
        //获取所有子集，给出对应的组合
        List<ClosureTable> params = new ArrayList<>();
        for (ClosureTableRef closureTableRef : closureTableRefs) {
            params.add(ClosureTable.builder().nodeId(closureTableRef.getNodeId())
                    .pid(closureTableRef.getPid()).build()
            );
        }
        return closureTableMapper.selectIdsByComposeKeys(params);
    }

    @Override
    public void removeNodes(AdjacencyList node) {
        Long id = node.getNodeId();
        Long pid = node.getPid();
        closureTableMapper.delete(Wrappers.lambdaQuery(ClosureTable.class)
                .eq(ClosureTable::getNodeId, id)
                .eq(ClosureTable::getNodeId, pid)
        );
        //这里删除比较特殊，删除可能会影响其他树的结构
        closureTableRefMapper.delete(Wrappers.lambdaQuery(ClosureTableRef.class)
                .eq(ClosureTableRef::getRootId, id)
        );
    }

    @Override
    public List<ClosureTable> queryParents(Long curId) {
        return closureTableMapper.queryParent(curId);
    }

    @Override
    public List<ClosureTable> queryAllParents(Long curId) {
        return closureTableMapper.queryAllParent(curId);
    }
}
