package com.xl.tree.service;

import com.xl.tree.entity.AdjacencyList;

import java.util.Collection;
import java.util.List;

/**
 * @author tanjl11
 * @date 2021/11/22 14:09
 */
public interface TreeNodeService<T> {
    void insertNodes(Collection<AdjacencyList> lists);

    List<T> queryChildren(Long parentId);

    List<T> queryAllChildren(Long parentId);

    void removeNodes(AdjacencyList node);

    List<T> queryParents(Long curId);

    List<T> queryAllParents(Long curId);
}
