package com.xl.shardingjdbc.demo;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xl.shardingjdbc.demo.entity.OrderDetail;
import com.xl.shardingjdbc.demo.entity.OrderInfo;
import com.xl.shardingjdbc.demo.mapper.OrderDetailMapper;
import com.xl.shardingjdbc.demo.mapper.OrderInfoMapper;
import com.xl.shardingjdbc.demo.service.OrderServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ShardingTestDemo {
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private OrderServiceImpl orderService;

    @Test
    public void test() {
//        orderService.saveBatch(OrderInfo.batchRandomData());
        orderInfoMapper.insertBatch(OrderInfo.batchRandomData());
    }

    @Test
    public void testDatabase() {
        List<OrderInfo> collection = OrderInfo.batchRandomData();
        for (OrderInfo orderInfo : collection) {
            orderInfoMapper.insert(orderInfo);
            List<OrderDetail> detailList = orderInfo.getDetailList();
            if(!detailList.isEmpty()){
                detailList.forEach(e->e.setOrderId(orderInfo.getOrderId()));
                orderDetailMapper.insertBatch(detailList);
            }
        }
    }


}
