package com.xl.mphelper.example.service.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.mphelper.example.entity.OrderDetail;
import com.xl.mphelper.example.entity.OrderInfo;
import com.xl.mphelper.example.mapper.OrderDetailMapper;
import com.xl.mphelper.example.mapper.OrderInfoMapper;
import com.xl.mphelper.example.service.IOrderService;
import com.xl.mphelper.service.CustomServiceImpl;
import com.xl.mphelper.shard.TableShardHolder;
import com.xl.mphelper.util.SnowflakeIds;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

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
    private OrderDetailServiceImpl detailService;

    @Override
    public void testSaveBatchPlus() {
        TableShardHolder.ignore();
        //这个是新增的
        List<OrderInfo> param = new ArrayList<>();
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(1L);
        orderInfo.setTotalAmount(BigDecimal.valueOf(10));
        orderInfo.setCreateTime(LocalDateTime.now());
        orderInfo.setOrderId(SnowflakeIds.generate());
        param.add(orderInfo);
        OrderInfo orderInfo2 = new OrderInfo();
        orderInfo2.setUserId(1L);
        orderInfo2.setTotalAmount(BigDecimal.valueOf(11));
        orderInfo2.setCreateTime(LocalDateTime.now());
        orderInfo2.setOrderId(SnowflakeIds.generate());
        param.add(orderInfo2);
        orderInfoMapper.insertBatch(param);
        List<OrderInfo> infos = orderInfoMapper.selectList(Wrappers.lambdaQuery());
        //修改的
        OrderInfo updateInfo = infos.get(0);
        updateInfo.setTotalAmount(BigDecimal.ONE);
        infos.remove(1);
        //新的
        OrderInfo orderInfo21 = orderInfo2;
        orderInfo21.setTotalAmount(BigDecimal.valueOf(233));
        orderInfo21.setOrderId(null);
        infos.add(orderInfo21);
        LambdaQueryWrapper<OrderInfo> queryWrapper = Wrappers.lambdaQuery(OrderInfo.class)
                .eq(OrderInfo::getUserId, 1);
        saveBatchPlus(infos, queryWrapper);
        TableShardHolder.resetIgnore();
    }

    @Override
    public Page<OrderInfo> queryAll() {
        Page<OrderInfo> page = new Page<>(2,2);
        return orderInfoMapper.selectPage(page, null);
    }

    @Override
    public List<OrderInfo> queryAllList() {
        return orderInfoMapper.selectList(null);
    }

    @Override
    public void testCustomServiceShardCUD() {
        List<OrderInfo> orderInfos = OrderInfo.batchRandomData();
        List<OrderDetail> detailList = new ArrayList<>();
        for (OrderInfo orderInfo : orderInfos) {
            long orderId = SnowflakeIds.generate();
            orderInfo.setOrderId(orderId);
            List<OrderDetail> detailList1 = orderInfo.getDetailList();
            detailList1.forEach(e -> {
                e.setDetailId(SnowflakeIds.generate());
                e.setOrderId(orderId);
            });
            detailList.addAll(detailList1);
        }
        saveBatchShard(orderInfos);
        updateBatchByShard(orderInfos);
        removeByShard(orderInfos);
        detailService.saveBatchShard(detailList);
        detailService.updateBatchByShard(detailList);
        detailService.removeByShard(detailList);
    }

    @Override
    public void saveOrders(Collection<OrderInfo> orders) {
        Map<String, List<OrderInfo>> collect = orders.stream().collect(Collectors.groupingBy(OrderInfo::suffix));
        for (Map.Entry<String, List<OrderInfo>> entry : collect.entrySet()) {
            List<OrderInfo> value = entry.getValue();
            List<OrderDetail> addList = new ArrayList<>(1000);
            for (OrderInfo order : value) {
                long orderId = SnowflakeIds.generate();
                order.setOrderId(orderId);
                List<OrderDetail> detailList = order.getDetailList();
                for (OrderDetail orderDetail : detailList) {
                    orderDetail.setOrderId(orderId);
                    orderDetail.setDetailId(SnowflakeIds.generate());
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
        Page<OrderInfo> res = (Page<OrderInfo>) wrapSupplier(() -> orderInfoMapper.testLeftJoin(page, month), KVBuilder.init(OrderInfo.class, month).put(OrderDetail.class, month)
        );
        return res;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
