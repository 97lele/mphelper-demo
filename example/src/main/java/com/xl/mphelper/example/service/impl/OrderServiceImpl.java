package com.xl.mphelper.example.service.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.mphelper.example.entity.OrderDetail;
import com.xl.mphelper.example.entity.OrderInfo;
import com.xl.mphelper.example.mapper.OrderDetailMapper;
import com.xl.mphelper.example.mapper.OrderInfoMapper;
import com.xl.mphelper.example.service.IOrderService;
import com.xl.mphelper.example.utils.SnowFlowIds;
import com.xl.mphelper.service.CustomServiceImpl;
import com.xl.mphelper.shard.TableShardHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanjl11
 * @date 2021/10/27 17:07
 */
@Service
public class OrderServiceImpl extends CustomServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderService {
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private OrderServiceImpl orderService;

    @Override
    public void testSaveBatch() {
        TableShardHolder.ignore();
        //这个是新增的
        List<OrderInfo> param = new ArrayList<>();
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(1L);
        orderInfo.setTotalAmount(BigDecimal.valueOf(10));
        orderInfo.setCreateTime(LocalDateTime.now());
        orderInfo.setOrderId(SnowFlowIds.generate());
        param.add(orderInfo);
        OrderInfo orderInfo2 = new OrderInfo();
        orderInfo2.setUserId(1L);
        orderInfo2.setTotalAmount(BigDecimal.valueOf(11));
        orderInfo2.setCreateTime(LocalDateTime.now());
        orderInfo2.setOrderId(SnowFlowIds.generate());
        param.add(orderInfo2);
        orderInfoMapper.insertBatch(param);
        List<OrderInfo> infos = orderInfoMapper.selectList(Wrappers.lambdaQuery());
        //修改的
        infos.get(0).setTotalAmount(BigDecimal.ONE);
        infos.remove(1);
        //新的
        OrderInfo orderInfo21 = orderInfo2;
        orderInfo21.setTotalAmount(BigDecimal.valueOf(233));
        orderInfo21.setOrderId(null);
        infos.add(orderInfo21);
        LambdaQueryWrapper<OrderInfo> queryWrapper = Wrappers.lambdaQuery(OrderInfo.class)
                .eq(OrderInfo::getUserId, 1);
        saveBatchPlus(infos, queryWrapper, e -> e.setOrderId(SnowFlowIds.generate()));
        TableShardHolder.resetIgnore();
    }


    @Override
    public void saveOrders(Collection<OrderInfo> orders) {
        for (OrderInfo order : orders) {

        }
        Map<String, List<OrderInfo>> collect = orders.stream().collect(Collectors.groupingBy(OrderInfo::suffix));
        for (Map.Entry<String, List<OrderInfo>> entry : collect.entrySet()) {
            List<OrderInfo> value = entry.getValue();
            List<OrderDetail> addList = new ArrayList<>(1000);
            for (OrderInfo order : value) {
                long orderId = SnowFlowIds.generate();
                order.setOrderId(orderId);
                List<OrderDetail> detailList = order.getDetailList();
                for (OrderDetail orderDetail : detailList) {
                    orderDetail.setOrderId(orderId);
                    orderDetail.setDetailId(SnowFlowIds.generate());
                    addList.add(orderDetail);
                }
            }
            if (!addList.isEmpty()) {
                orderInfoMapper.insertBatch(value);
                orderDetailMapper.insertBatch(addList);
            }
        }
    }

    @Override
    public List<OrderInfo> queryAll(String month) {
        return orderInfoMapper.testLeftJoin2(month);
    }

    @Override
    public Page<OrderInfo> queryByPage(String month) {
        Page<OrderInfo> page = new Page<>();
        TableShardHolder.putVal(OrderInfo.class, month);
        TableShardHolder.putVal(OrderDetail.class, month);
        Page<OrderInfo> res = orderInfoMapper.testLeftJoin(page, month
        );
        TableShardHolder.clear();
        return res;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}