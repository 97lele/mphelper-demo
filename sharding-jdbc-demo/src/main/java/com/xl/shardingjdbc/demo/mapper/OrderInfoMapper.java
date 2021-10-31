package com.xl.shardingjdbc.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xl.shardingjdbc.demo.entity.OrderInfo;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author lele
 * @since 2021-10-27
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    void createTable();

}
