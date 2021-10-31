package com.xl.mphelper.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.mphelper.example.entity.OrderInfo;
import com.xl.mphelper.example.service.IOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author tanjl11
 * @date 2021/10/27 17:39
 */
@RestController
public class OrderController {
    @Resource
    private IOrderService orderService;

    @GetMapping("/testAdd")
    public void testAdd() {
        orderService.saveOrders(OrderInfo.batchRandomData());
    }

    @PostMapping("/query")
    public List<OrderInfo> query(@RequestParam("month") String month) {
        List<OrderInfo> infos = orderService.queryAll(month);
        return infos;
    }

    @PostMapping("/queryByPage")
    public Page<OrderInfo> queryByPage(@RequestParam("month") String month) {
        return orderService.queryByPage(month);
    }

    @PostMapping("/testBatch")
    public void testBatch() {
        orderService.testSaveBatch();
    }

}

