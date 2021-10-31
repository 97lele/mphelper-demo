package com.xl.mphelper.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author tanjl11
 * @date 2021/09/23 16:36
 */
public interface ComposeKeyMapper<T> extends CustomMapper<T> {
    /**
     * @param params  查询的主键入参
     * @param wrapper 查询的列会用到 ew.sqlSelect和拼接上查询条件
     * @return
     */
    List<T> selectByComposeKeys(@Param("collection") Collection<T> params, @Param(Constants.WRAPPER) Wrapper<T> wrapper);

    /**
     * 批量更新
     *
     * @param params
     */
    void updateByComposeKeys(@Param("collection") Collection<T> params);

    /**
     * 只查询主键
     *
     * @param params
     * @return
     */
    List<T> selectIdsByComposeKeys(@Param("collection") Collection<T> params);

    /**
     * 联合主键删除
     * @param params
     * @return
     */
    int deleteByComposeKeys(@Param("collection")Collection<T> params);

    /**
     * 下面的方法不支持
     * @param entity
     * @return
     */
    @Override
    default int updateById(T entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int updateBatchByIds(Collection<T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    default T selectById(Serializable id) {
        throw new UnsupportedOperationException();
    }

    @Override
    default List<T> selectBatchIds(Collection<? extends Serializable> idList) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int deleteBatchIds(Collection<? extends Serializable> idList){
        throw new UnsupportedOperationException();
    }

    @Override
    default int deleteById(Serializable id){
        throw new UnsupportedOperationException();
    }
}
