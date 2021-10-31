package com.xl.mphelper.example.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.mphelper.annonations.TableShard;
import com.xl.mphelper.annonations.TableShardIgnore;
import com.xl.mphelper.annonations.TableShardParam;
import com.xl.mphelper.example.entity.OrderInfo;
import com.xl.mphelper.example.shards.CommonStrategy;
import com.xl.mphelper.mapper.CustomMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author lele
 * @since 2021-10-27
 */
@TableShard(enableCreateTable = true, createTableMethod = "createTable", shardStrategy = CommonStrategy.class)
public interface OrderInfoMapper extends CustomMapper<OrderInfo> {
    void createTable();

    List<OrderInfo> testLeftJoin2(@TableShardParam String month);


    Page<OrderInfo> testLeftJoin(IPage page, @TableShardParam String month);
}