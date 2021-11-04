package com.xl.shardingjdbc.demo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xl.mphelper.mapper.CustomMapper;
import com.xl.shardingjdbc.demo.entity.OrderDetail;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author lele
 * @since 2021-10-27
 */
public interface OrderDetailMapper extends CustomMapper<OrderDetail> {
    void createTable();
}
