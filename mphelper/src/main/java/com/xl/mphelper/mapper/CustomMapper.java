package com.xl.mphelper.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author tanjl11
 * @date 2021/02/04 17:18
 * 自定义mapper
 */
public interface CustomMapper<T> extends BaseMapper<T> {
    /**
     * 批量插入
     * @param collection 批量插入数据
     * @return ignore
     */
    int insertBatch(@Param("collection") Collection<T> collection);

    /**
     * 批量更新
     * @param collection
     * @return
     */
    int updateBatchByIds(@Param("collection") Collection<T> collection);

    /**
     * 查询单独的列
     * @param wrapper
     * @return
     */
    List<T> selectDistinctColumn(@Param(Constants.WRAPPER) Wrapper wrapper);
}
