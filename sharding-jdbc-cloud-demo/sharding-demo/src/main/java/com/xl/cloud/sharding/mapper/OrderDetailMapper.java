package com.xl.cloud.sharding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xl.cloud.sharding.entity.OrderDetail;

public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    void createTable(String suffix);
}
