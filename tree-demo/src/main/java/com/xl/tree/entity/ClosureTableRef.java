package com.xl.tree.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * @author tanjl11
 * @date 2021/11/22 13:57
 */
@TableName("closure_table_ref")
@Builder
@Getter
@ToString
public class ClosureTableRef {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long rootId;
    private Long nodeId;
    private Long pid;
    private Boolean isLeaf;
    private Integer depth;

}
