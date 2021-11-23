package com.xl.tree.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xl.mphelper.annonations.ComposeKey;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tanjl11
 * @date 2021/11/22 13:52
 * 邻接表模型
 * 这里id+pid为唯一主键，实际业务可能会加其他属性作为唯一主键
 */
@Data
@TableName("adjacency_list")
public class AdjacencyList implements TreeNode<AdjacencyList>, Cloneable {
    @ComposeKey
    private Long nodeId;
    @ComposeKey
    private Long pid;
    private String content;
    @TableField(exist = false)
    private boolean isEnd;
    @TableField(exist = false)
    private List<AdjacencyList> subNodes;
    @TableField(exist = false)
    private String fullPath;
    @TableField(exist = false)
    private Integer level;

    @TableField(exist = false)
    private Long leftNum;
    @TableField(exist = false)
    private Long rightNum;

    public static AdjacencyList build(Long id, Long pid, String content) {
        AdjacencyList res = new AdjacencyList();
        res.pid = pid;
        res.nodeId = id;
        res.content = content;
        return res;
    }

    @Override
    public Serializable getTreePid() {
        return pid;
    }

    @Override
    public Serializable getTreeId() {
        return nodeId;
    }

    @Override
    public List<AdjacencyList> getSubNodes() {
        return subNodes;
    }

    @Override
    public boolean isLeaf() {
        return isEnd;
    }

    @Override
    public void setSubNodes(List<AdjacencyList> sons) {
        this.subNodes = sons;
    }

    /**
     * *              0               10
     * *           1    2           1    6
     * *         3  4  5  6       3  4  7 8
     * *        6        7 8     6
     * *      7  8             7   8
     * *
     *
     * @return
     */
    public static List<AdjacencyList> sampleData() {
        AdjacencyList root = AdjacencyList.build(0L, 0L, "value0-0");
        AdjacencyList root2 = AdjacencyList.build(10L, 10L, "value10-10");
        List<AdjacencyList> sum = new ArrayList<>();
        sum.add(root);
        sum.add(root2);
        for (long i = 1; i <= 6; i++) {
            long pid = (i - 1) / 2;
            AdjacencyList build = AdjacencyList.build(i, pid, "value" + i + "-" + pid);
            sum.add(build);
        }
        sum.add(AdjacencyList.build(6L, 3L, "value6-3"));
        sum.add(AdjacencyList.build(7L, 6L, "value7-6"));
        sum.add(AdjacencyList.build(8L, 6L, "value8-6"));
        sum.add(AdjacencyList.build(1L, 10L, "value1-10"));
        sum.add(AdjacencyList.build(6L, 10L, "value6-10"));
        return sum;
    }
}
