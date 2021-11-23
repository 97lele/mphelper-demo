package com.xl.tree.mapper;

import com.xl.mphelper.mapper.ComposeKeyMapper;
import com.xl.mphelper.mapper.CustomMapper;
import com.xl.tree.entity.ClosureTable;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author tanjl11
 * @date 2021/11/22 14:36
 */
public interface ClosureTableMapper extends ComposeKeyMapper<ClosureTable> {
    /**
     * @param curId
     * @return
     */
    @Select("select * from closure_table where node_id in (select node_id from closure_table_ref where root_id =#{curId} and depth=1 ) ")
    List<ClosureTable> queryChildren(Long curId);

    @Select("select * from closure_table where node_id in (select root_id from closure_table_ref where node_id =#{curId} and depth=1) ")
    List<ClosureTable> queryParent(Long curId);

    @Select("select * from closure_table where node_id in (select root_id from closure_table_ref where node_id =#{curId}) ")
    List<ClosureTable> queryAllParent(Long curId);
}
