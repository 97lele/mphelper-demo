package com.xl.tree.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xl.tree.entity.AdjacencyList;
import com.xl.tree.mapper.AdjacencyListMapper;
import com.xl.tree.service.TreeNodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanjl11
 * @date 2021/11/22 14:14
 * 邻接表实现
 */
@Service
public class AdjacencyListServiceImpl implements TreeNodeService<AdjacencyList> {
    @Resource
    private AdjacencyListMapper adjacencyListMapper;

    @Override
    public void insertNodes(Collection<AdjacencyList> lists) {
        //无需处理，直接插入即可
        adjacencyListMapper.insertBatch(lists);
    }

    @Override
    public List<AdjacencyList> queryChildren(Long parentId) {
        //select * from adjacency_list where pid=#{parentId}
        return adjacencyListMapper.selectList(Wrappers.lambdaQuery(AdjacencyList.class)
                .eq(AdjacencyList::getPid, parentId)
        );
    }

    @Override
    public List<AdjacencyList> queryAllChildren(Long parentId) {
        List<AdjacencyList> res = new ArrayList<>();
        List<AdjacencyList> subNodes = queryChildren(parentId);
        while (CollectionUtils.isNotEmpty(subNodes)) {
            res.addAll(subNodes);
            List<Long> parentIds = subNodes.stream().map(AdjacencyList::getNodeId).collect(Collectors.toList());
            subNodes = adjacencyListMapper.selectList(Wrappers.lambdaQuery(AdjacencyList.class)
                    .in(AdjacencyList::getPid, parentIds)
            );
        }
        return res;
    }

    @Override
    public void removeNodes(AdjacencyList node) {
        adjacencyListMapper.delete(Wrappers.lambdaQuery(AdjacencyList.class)
                .eq(AdjacencyList::getPid, node.getPid())
                .eq(AdjacencyList::getNodeId, node.getNodeId())
        );
    }

    @Override
    public List<AdjacencyList> queryParents(Long curId) {
        List<AdjacencyList> res = null;
        List<Long> pids = new ArrayList<>();
        adjacencyListMapper.selectList(Wrappers.lambdaQuery(AdjacencyList.class)
                .eq(AdjacencyList::getNodeId, curId)
        ).forEach(e -> {
            if (!e.getPid().equals(e.getNodeId())) {
                pids.add(e.getPid());
            }
        });
        if (!pids.isEmpty()) {
            res = adjacencyListMapper.selectList(Wrappers.lambdaQuery(AdjacencyList.class)
                    .in(AdjacencyList::getNodeId, pids)
            );
        }
        return res;
    }

    @Override
    public List<AdjacencyList> queryAllParents(Long curId) {
        List<AdjacencyList> res = new ArrayList<>();
        List<AdjacencyList> parents = queryParents(curId);
        if (parents == null) {
            return Collections.EMPTY_LIST;
        }
        Set<String> hasAdd=new HashSet<>();
        res.addAll(parents);
        List<AdjacencyList> points = parents;
        while (CollectionUtils.isNotEmpty(points)) {
            List<AdjacencyList> curLevel = new ArrayList<>();
            for (AdjacencyList point : points) {
                List<AdjacencyList> p = queryParents(point.getNodeId());
                if (p != null) {
                    for (AdjacencyList adjacencyList : p) {
                        if(hasAdd.add(adjacencyList.getUniqueKey())){
                            res.add(adjacencyList);
                            curLevel.add(adjacencyList);
                        }
                    }
                }
            }
            points = curLevel;
        }
        return res;
    }
}
