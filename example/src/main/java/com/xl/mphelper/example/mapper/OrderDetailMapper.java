package com.xl.mphelper.example.mapper;

import com.xl.mphelper.annonations.TableShard;
import com.xl.mphelper.annonations.TableShardIgnore;
import com.xl.mphelper.example.entity.OrderDetail;
import com.xl.mphelper.example.shards.CommonStrategy;
import com.xl.mphelper.mapper.CustomMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author lele
 * @since 2021-10-27
 */
@TableShard(enableCreateTable = true, createTableMethod = "createTable", shardStrategy = CommonStrategy.class)
public interface OrderDetailMapper extends CustomMapper<OrderDetail> {
    void createTable();
}
