package com.xl.mphelper.example.shards;

import com.xl.mphelper.example.entity.Shardable;
import com.xl.mphelper.shard.ITableShardStrategy;

/**
 * @author tanjl11
 * @date 2021/10/27 17:13
 */
public class CommonStrategy implements ITableShardStrategy<Shardable> {
    @Override
    public String routingTable(String tableName, Shardable shardable) {
        return tableName + "_" + shardable.suffix();
    }
}
