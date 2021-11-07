package com.xl.mphelper.shard;


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
