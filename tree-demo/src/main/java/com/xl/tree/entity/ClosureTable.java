package com.xl.tree.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xl.mphelper.annonations.ComposeKey;
import lombok.*;

/**
 * @author tanjl11
 * @date 2021/11/22 13:55
 * 如果采用builder模式而添加默认空的构造器，在查询行数不够时，会触发下标越界异常
 * {@link org.apache.ibatis.executor.resultset.DefaultResultSetHandler#createUsingConstructor}
 * String columnName = rsw.getColumnNames().get(i);
 * 有默认构造器后，不走构造器赋值方法
 */
@TableName("closure_table")
@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClosureTable {
    @ComposeKey
    private Long nodeId;
    @ComposeKey
    private Long pid;
    private String content;
}
