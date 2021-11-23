package com.xl.tree.mapper;

import com.xl.mphelper.mapper.CustomMapper;
import com.xl.tree.entity.PathNode;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author tanjl11
 * @date 2021/11/22 14:37
 * 需要查父级 最好建立 path,node,level 索引，如果只建path，node，也可以先查出当前的子节点的level再赋值给对应sql作为过滤
 * 如果只是子级，path即可
 */
public interface PathNodeMapper extends CustomMapper<PathNode> {
    @Select("select p.*\n" +
            "from path_node p\n" +
            "where exists(select 1\n" +
            "             from path_node s\n" +
            "             where s.path like concat(p.path, '%')\n" +
            "               and s.node_id = #{nodeId} and p.level=s.level-1)")
    List<PathNode> queryParent(@Param("nodeId") Long nodeId);


    @Select("select p.*\n" +
            "from path_node p\n" +
            "where exists(select 1\n" +
            "             from path_node s\n" +
            "             where s.path like concat(p.path, '%')\n" +
            "               and s.node_id = #{nodeId} and s.level!=p.level)")
    List<PathNode> queryAllParent(@Param("nodeId") Long nodeId);
}
