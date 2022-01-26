package com.xl.tree.builddemo;

import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author tanjl11
 * @date 2022/01/12 11:35
 */
@Data
public class DemoPathNode implements PathNode<DemoPathNode> {
    private Long selfId;
    private String parentMaterialCode;
    private String selfMaterialCode;
    private List<DemoPathNode> subList;
    private boolean leaf;
    private Integer level;
    private String fullPath;
    private Date effectedFrom;
    private Date effectedTo;
    //...还有其他属性
    private String lineNo;

    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder builder = new StringBuilder();
        builder.append("DemoPathNode{")
                .append("code='").append(selfMaterialCode).append("'")
                .append(", fullPath='").append(fullPath)
                .append("'").append(", lineNo='").append(lineNo)
                .append("'");
        if (effectedFrom != null) {
            builder
                    .append(",from='")
                    .append(format.format(effectedFrom)).append("'");
        }
        if (effectedTo != null) {
            builder
                    .append(",to='")
                    .append(format.format(effectedTo)).append("'");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public Long dataBaseId() {
        return selfId;
    }

    @Override
    public Serializable pid() {
        return parentMaterialCode;
    }

    @Override
    public Serializable nodeId() {
        return selfMaterialCode;
    }

    @Override
    public List<DemoPathNode> getSubNodes() {
        return subList;
    }

    @Override
    public boolean isLeaf() {
        return leaf;
    }

    @Override
    public int level() {
        return level;
    }

    @Override
    public String fullPath() {
        return fullPath;
    }

    @Override
    public void setSubNodes(List<DemoPathNode> sons) {
        this.subList = sons;
    }

    @Override
    public Date from() {
        return effectedFrom;
    }

    @Override
    public Date to() {
        return effectedTo;
    }

    /**
     * 路径
     * @return
     */
    @Override
    public String getUniqueKey() {
        return fullPath + lineNo;
    }
}
