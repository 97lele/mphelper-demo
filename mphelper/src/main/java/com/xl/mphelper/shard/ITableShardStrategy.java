package com.xl.mphelper.shard;

import com.alibaba.druid.support.json.JSONUtils;
import com.xl.mphelper.annonations.TableShardParam;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
/**
 * @author tanjl11
 * @date 2021/10/15 16:18
 */
@FunctionalInterface
public interface ITableShardStrategy<T> {
    /**
     * 通过实体获取表名，可以用 {@link TableShardParam}指定某个参数，并复写对应的策略
     * 如果是可迭代的对象，会取列表的第一个参数作为对象，所以再进入sql前要进行分组
     * 也可以使用 {@link TableShardHolder} 进行名称替换
     * 优先级：TableShardHolder>TableShardParam>参数第一个
     *
     * @param tableName
     * @param entity
     * @return
     */
    String routingTable(String tableName, T entity);

    class TableShardDefaultStrategy implements ITableShardStrategy {
        @Override
        public String routingTable(String tableName, Object entity) {
            return tableName + "_" + entity.toString();
        }
    }

    class CommonStrategy implements ITableShardStrategy<Shardable> {

        @Override
        public String routingTable(String tableName, Shardable shardable) {
            return tableName + "_" + shardable.suffix();
        }
    }

    class HashStrategy implements ITableShardStrategy {
        @Override
        public String routingTable(String tableName, Object entity) {
            Integer length = TableShardHolder.hashTableLength();
            if (length == null||length==0) {
                throw new IllegalStateException("illegal hash length in TableShardHolder");
            }
            String hashKey=null;
            if (entity instanceof String) {
                hashKey= (String) entity;
            }
            if(entity instanceof Shardable){
                hashKey=((Shardable)entity).suffix();
            }
            if(entity instanceof Number){
                hashKey=entity.toString();
            }
            if(hashKey==null&&entity!=null){
                hashKey= JSONUtils.toJSONString(entity);
            }
            if(hashKey==null){
                throw new IllegalStateException("can not generate hashKey in current param:"+entity);
            }
            String value = DigestUtils.md5DigestAsHex(hashKey.getBytes(StandardCharsets.UTF_8));
            value=value.substring(value.length()-4);
            long hashMod = Long.parseLong(value, 16);
            return tableName+"_"+hashMod % length;
        }
    }
}
