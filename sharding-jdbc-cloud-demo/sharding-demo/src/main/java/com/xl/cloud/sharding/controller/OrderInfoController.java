package com.xl.cloud.sharding.controller;

import com.xl.cloud.sharding.entity.OrderDetail;
import com.xl.cloud.sharding.entity.OrderInfo;
import com.xl.cloud.sharding.mapper.OrderDetailMapper;
import com.xl.cloud.sharding.mapper.OrderInfoMapper;
import com.xl.cloud.sharding.service.OrderDetailServiceImpl;
import com.xl.cloud.sharding.service.OrderInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tanjl11
 * @date 2021/11/12 11:27
 */
@RestController
public class OrderInfoController {
    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private OrderDetailServiceImpl orderDetailService;

    @GetMapping("/testAdd/{date}")
    public void testAdd(@PathVariable(value = "date") String suffix) {
        List<OrderInfo> collection = OrderInfo.batchRandomData();
        List<OrderDetail> detailRes = new ArrayList<>();
        for (OrderInfo orderInfo : collection) {
            if (!"null".equals(suffix)) {
                String[] split = suffix.split("-");
                int year = Integer.valueOf(split[0]);
                int month = Integer.valueOf(split[1]);
                LocalDateTime now = LocalDateTime.of(year, month, 1, 0, 0);
                orderInfo.setCreateTime(now);
            }
        }
        orderInfoService.saveBatch(collection);
        for (OrderInfo orderInfo : collection) {
            List<OrderDetail> detailList = orderInfo.getDetailList();
            if (!detailList.isEmpty()) {
                detailList.forEach(e ->
                {
                    e.setOrderId(orderInfo.getOrderId());
                    if (!"null".equals(suffix)) {
                        String[] split = suffix.split("-");
                        int year = Integer.valueOf(split[0]);
                        int month = Integer.valueOf(split[1]);
                        LocalDateTime now = LocalDateTime.of(year, month, 1, 0, 0);
                        e.setCreateTime(now);
                    }
                });
                detailRes.addAll(detailList);
            }
        }

        orderDetailService.saveBatch(detailRes);
    }
}
