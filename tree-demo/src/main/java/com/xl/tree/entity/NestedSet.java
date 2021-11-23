package com.xl.tree.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xl.mphelper.annonations.ComposeKey;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

/**
 * @author tanjl11
 * @date 2021/11/22 14:03
 * 前序遍历得到的编号赋值给子节点
 * right_no+1=left_no为末尾节点
 */
@Getter
@TableName("nested_set")
@Builder
@ToString
public class NestedSet {
    private Long nodeId;
    private Long pid;
    private String content;
    @ComposeKey
    private Long leftNo;
    @ComposeKey
    private Long rightNo;
    @ComposeKey
    private Long rootId;
    private Integer depth;
}
