package com.xl.cloud.sharding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xl.cloud.sharding.entity.OrderInfo;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    void createTable(String suffix);
}
