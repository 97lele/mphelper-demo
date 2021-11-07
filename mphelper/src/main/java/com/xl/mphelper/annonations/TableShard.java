package com.xl.mphelper.annonations;

import com.xl.mphelper.shard.ExecBaseMethod;
import com.xl.mphelper.shard.ITableShardDbType;
import com.xl.mphelper.shard.ITableShardStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author tanjl11
 * @date 2021/10/15 16:13
 * 作用于mapper上面
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableShard {
    //是否自动建表
    boolean enableCreateTable() default false;

    //创建表方法
    String createTableMethod() default "";


    //获取表名的策略
    Class<? extends ITableShardStrategy> shardStrategy() default ITableShardStrategy.CommonStrategy.class;

    //是否启用hash策略，-1不启用，其他作为分表的数量
    int hashTableLength() default -1;

    //默认使用的db策略
    Class<? extends ITableShardDbType> dbType() default ITableShardDbType.MysqlShard.class;

    //选取方法的策略，用到分页组件时需额外注意
    Class<? extends ExecBaseMethod> execMethodStrategy() default ExecBaseMethod.class;
}
