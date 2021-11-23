package com.xl.tree.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xl.tree.entity.AdjacencyList;
import com.xl.tree.entity.PathNode;
import com.xl.tree.entity.TreeNode;
import com.xl.tree.mapper.PathNodeMapper;
import com.xl.tree.service.TreeNodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author tanjl11
 * @date 2021/11/22 14:52
 */
@Service
public class PathNodeServiceImpl implements TreeNodeService<PathNode> {
    @Resource
    private PathNodeMapper pathNodeMapper;

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
        List<PathNode> nodes = new ArrayList<>(lists.size());
        for (AdjacencyList root : roots) {
            root.setFullPath(root.getNodeId() + "");
            root.setLevel(0);
            //构建树的同时，把全路径赋值,并生成节点
            TreeNode.build(root, lists, null, (p, s) -> {
                s.setFullPath(p.getFullPath() + "|" + s.getNodeId());
                s.setLevel(p.getLevel() + 1);
                PathNode value = PathNode.builder()
                        .nodeId(s.getNodeId())
                        .pid(s.getPid())
                        .path(s.getFullPath())
                        .level(s.getLevel())
                        .content(s.getContent())
                        .build();
                nodes.add(value);
            });
        }
        pathNodeMapper.insertBatch(nodes);
    }

    @Override
    public List<PathNode> queryChildren(Long parentId) {
        PathNode parent = pathNodeMapper.selectById(parentId);
        String path = parent.getPath();
        Integer level = parent.getLevel();
        List<PathNode> children = pathNodeMapper.selectList(Wrappers.lambdaQuery(PathNode.class)
                .likeRight(PathNode::getPath, path + "|")
                .eq(PathNode::getLevel, level + 1)
        );
        return children;
    }

    @Override
    public List<PathNode> queryAllChildren(Long parentId) {
        PathNode parent = pathNodeMapper.selectById(parentId);
        String path = parent.getPath();
        return pathNodeMapper.selectList(Wrappers.lambdaQuery(PathNode.class)
                .likeRight(PathNode::getPath, path + "|")
        );
    }

    @Override
    public void removeNodes(AdjacencyList node) {
        Long curId = node.getNodeId();
        PathNode pathNode = pathNodeMapper.selectById(curId);
        String path = pathNode.getPath();
        pathNodeMapper.delete(Wrappers.lambdaQuery(PathNode.class)
                .likeRight(PathNode::getPath, path)
        );
    }

    @Override
    public List<PathNode> queryParents(Long curId) {
        PathNode pathNode = pathNodeMapper.selectById(curId);
        String path = pathNode.getPath();
        Integer level = pathNode.getLevel();
        return pathNodeMapper.selectList(Wrappers.lambdaQuery(PathNode.class)
                .eq(PathNode::getLevel, level)
                .likeLeft(PathNode::getPath, path.substring(0, path.lastIndexOf("|")))
        );
    }

    @Override
    public List<PathNode> queryAllParents(Long curId) {
        PathNode pathNode = pathNodeMapper.selectById(curId);
        String path = pathNode.getPath();
        return pathNodeMapper.selectList(Wrappers.lambdaQuery(PathNode.class)
                .likeLeft(PathNode::getPath, path.substring(0, path.lastIndexOf("|")))
        );
    }
}
