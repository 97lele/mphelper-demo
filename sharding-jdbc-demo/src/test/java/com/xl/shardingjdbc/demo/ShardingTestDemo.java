package com.xl.shardingjdbc.demo;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xl.shardingjdbc.demo.mapper.OrderInfoMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.sql.Wrapper;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ShardingTestDemo {

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Test
    public void test() {
        orderInfoMapper.selectList(Wrappers.lambdaQuery());
    }


}
