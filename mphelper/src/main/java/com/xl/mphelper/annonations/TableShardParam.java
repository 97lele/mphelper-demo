package com.xl.mphelper.annonations;

import com.xl.mphelper.shard.ITableShardStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author tanjl11
 * @date 2021/10/15 17:56
 * 这个策略比类上的要高
 * 用于方法参数
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableShardParam {
    //获取表名的策略
    Class<? extends ITableShardStrategy> shardStrategy() default ITableShardStrategy.TableShardDefaultStrategy.class;

    int hashTableLength() default -1;

    boolean enableHash() default false;
}
