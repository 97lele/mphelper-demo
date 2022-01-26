package com.xl.tree.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xl.tree.entity.AdjacencyList;
import com.xl.tree.entity.NestedSet;
import com.xl.tree.entity.TreeNode;
import com.xl.tree.mapper.NestedSetMapper;
import com.xl.tree.service.TreeNodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tanjl11
 * @date 2021/11/22 14:55
 */
@Service
public class NestedSetServiceImpl implements TreeNodeService<NestedSet> {
    @Resource
    private NestedSetMapper nestedSetMapper;

    @Override
    public void insertNodes(Collection<AdjacencyList> lists) {
        List<AdjacencyList> roots = new ArrayList<>();
        Iterator<AdjacencyList> iterator = lists.iterator();
        while (iterator.hasNext()) {
            AdjacencyList cur = iterator.next();
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
        List<NestedSet> res = new ArrayList<>();
        for (AdjacencyList root : roots) {
            root.setFullPath(root.getNodeId() + "");
            root.setLevel(0);
            //构建树的同时，把全路径赋值
            TreeNode.build(root, lists, null, (p, s) -> {
                s.setFullPath(p.getFullPath() + "|" + s.getNodeId());
                s.setLevel(p.getLevel() + 1);
                return true;
            }, true);
            AtomicLong i = new AtomicLong(0);
            TreeNode.foreachInDFS(root, e -> {
                e.setLeftNum(i.incrementAndGet());
                return true;
            }, e -> {
                e.setRightNum(i.incrementAndGet());
                res.add(NestedSet.builder()
                        .nodeId(e.getNodeId())
                        .pid(e.getPid())
                        .content(e.getContent())
                        .leftNo(e.getLeftNum())
                        .rightNo(e.getRightNum())
                        .rootId(root.getNodeId())
                        .depth(e.getLevel())
                        .build()
                );
            });
        }
        nestedSetMapper.insertBatch(res);
    }


    @Override
    public List<NestedSet> queryChildren(Long parentId) {
        List<NestedSet> nestedSets = nestedSetMapper.selectList(Wrappers.lambdaQuery(NestedSet.class)
                .eq(NestedSet::getNodeId, parentId)
        );
        List<NestedSet> res = new ArrayList<>();
        for (NestedSet nestedSet : nestedSets) {
            Long leftNo = nestedSet.getLeftNo();
            Long rightNo = nestedSet.getRightNo();
            //子集在父级的left_no和right_no里面
            List<NestedSet> children = nestedSetMapper.selectList(Wrappers.lambdaQuery(NestedSet.class)
                    .eq(NestedSet::getRootId, nestedSet.getRootId())
                    .eq(NestedSet::getDepth, nestedSet.getDepth() + 1)
                    .gt(NestedSet::getLeftNo,leftNo)
                    .lt(NestedSet::getLeftNo,rightNo)
            );
            res.addAll(children);
        }
        return res;
    }

    @Override
    public List<NestedSet> queryAllChildren(Long parentId) {
        List<NestedSet> nestedSets = nestedSetMapper.selectList(Wrappers.lambdaQuery(NestedSet.class)
                .eq(NestedSet::getNodeId, parentId)
        );
        List<NestedSet> res = new ArrayList<>();
        for (NestedSet nestedSet : nestedSets) {
            Long leftNo = nestedSet.getLeftNo();
            Long rightNo = nestedSet.getRightNo();
            //子集在父级的left_no和right_no里面
            List<NestedSet> children = nestedSetMapper.selectList(Wrappers.lambdaQuery(NestedSet.class)
                    .eq(NestedSet::getRootId, nestedSet.getRootId())
                    .gt(NestedSet::getLeftNo,leftNo)
                    .lt(NestedSet::getLeftNo,rightNo)
            );
            res.addAll(children);
        }
        return res;
    }

    @Override
    public void removeNodes(AdjacencyList node) {
        //删除between范围
        Long id = node.getNodeId();
        List<NestedSet> nestedSets = nestedSetMapper.selectList(Wrappers.lambdaQuery(NestedSet.class)
                .eq(NestedSet::getNodeId, id)
        );
        for (NestedSet nestedSet : nestedSets) {
            nestedSetMapper.delete(Wrappers.lambdaQuery(NestedSet.class)
                    .between(NestedSet::getLeftNo, nestedSet.getLeftNo(), nestedSet.getRightNo())
            );
        }
    }

    @Override
    public List<NestedSet> queryParents(Long curId) {
        List<NestedSet> nestedSets = nestedSetMapper.selectList(Wrappers.lambdaQuery(NestedSet.class)
                .eq(NestedSet::getNodeId, curId)
        );
        List<NestedSet> res = new ArrayList<>();
        for (NestedSet nestedSet : nestedSets) {
            Long rootId = nestedSet.getRootId();
            Long leftNo = nestedSet.getLeftNo();
            Long rightNo = nestedSet.getRightNo();
            Integer depth = nestedSet.getDepth();
            List<NestedSet> parents = nestedSetMapper.selectList(Wrappers.lambdaQuery(NestedSet.class)
                    .eq(NestedSet::getRootId, nestedSet.getRootId())
                    .lt(NestedSet::getLeftNo, leftNo)
                    .gt(NestedSet::getRightNo, rightNo)
                    .eq(NestedSet::getRootId, rootId)
                    .eq(NestedSet::getDepth, depth - 1)
            );
            res.addAll(parents);
        }
        return res;
    }

    @Override
    public List<NestedSet> queryAllParents(Long curId) {
        List<NestedSet> nestedSets = nestedSetMapper.selectList(Wrappers.lambdaQuery(NestedSet.class)
                .eq(NestedSet::getNodeId, curId)
        );
        List<NestedSet> res = new ArrayList<>();
        for (NestedSet nestedSet : nestedSets) {
            Long rootId = nestedSet.getRootId();
            Long leftNo = nestedSet.getLeftNo();
            Long rightNo = nestedSet.getRightNo();
            List<NestedSet> parents = nestedSetMapper.selectList(Wrappers.lambdaQuery(NestedSet.class)
                    .eq(NestedSet::getRootId, nestedSet.getRootId())
                    .lt(NestedSet::getLeftNo, leftNo)
                    .gt(NestedSet::getRightNo, rightNo)
                    .eq(NestedSet::getRootId, rootId)
            );
            res.addAll(parents);
        }
        return res;
    }
}
