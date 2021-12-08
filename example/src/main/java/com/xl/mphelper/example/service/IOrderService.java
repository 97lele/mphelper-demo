package com.xl.mphelper.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.mphelper.example.entity.OrderInfo;

import java.util.Collection;
import java.util.List;

/**
 * @author tanjl11
 * @date 2021/10/27 16:17
 */
public interface IOrderService {
    void saveOrders(Collection<OrderInfo> orders);

    void testCustomServiceShardCUD();

    List<OrderInfo> queryAll(String month);

    Page<OrderInfo> queryByPage(String month);

    void testSaveBatchPlus();

    Page<OrderInfo> queryAll();

    List<OrderInfo> queryAllList();
}
