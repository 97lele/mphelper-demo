package com.xl.shardingjdbc.demo.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xl.shardingjdbc.demo.entity.OrderInfo;
import com.xl.shardingjdbc.demo.mapper.OrderInfoMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> {

}
