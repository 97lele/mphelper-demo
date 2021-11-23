package com.xl.tree.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * @author tanjl11
 * @date 2021/11/22 13:54
 */
@TableName("path_node")
@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PathNode {
    @TableId
    private Long nodeId;
    private String path;
    private String content;
    private Integer level;
    private Long pid;
}
