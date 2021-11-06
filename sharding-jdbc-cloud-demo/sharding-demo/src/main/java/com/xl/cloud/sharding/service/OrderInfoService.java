package com.xl.cloud.sharding.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xl.cloud.sharding.entity.OrderInfo;
import com.xl.cloud.sharding.mapper.OrderInfoMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoService extends ServiceImpl<OrderInfoMapper, OrderInfo> {
}
