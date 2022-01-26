package com.xl.tree.builddemo;

import java.io.Serializable;

/**
 * @author tanjl11
 * @date 2022/01/12 10:01
 */
public interface AdjacencyList<F extends AdjacencyList<F>> {
    /**
     * 对应路径表的id,构建时候需要
     * @return
     */
    Long pathDataBaseId();
    /**
     * 设置对应路径表的id,构建时候需要
     * @return
     */
    void setPathDataBaseId(Long id);

    /**
     * 全路径，构建时给邻接表使用
     */
    String fullPath();

    /**
     * 构建时给邻接表使用
     * @param fullPath
     */
    void setFullPath(String fullPath);

    /**
     * 父类编码
     * @return
     */
    Serializable pid();

    /**
     * 自己的物料编码
     * @return
     */
    Serializable nodeId();

    /**
     * 在bom结构的唯一键
     * @return
     */
    default String getUniqueKey() {
        return nodeId() + "_" + pid();
    }
}
