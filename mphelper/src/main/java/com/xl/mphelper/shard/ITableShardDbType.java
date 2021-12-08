package com.xl.mphelper.shard;

import com.alibaba.druid.DbType;
import com.mysql.jdbc.JDBC4DatabaseMetaData;
import com.xl.mphelper.dynamic.DynamicDatasource;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author tanjl11
 * @date 2021/10/18 16:57
 * 简单的分库的可以直接用{@link com.xl.mphelper.dynamic.DynamicDataSourceHolder} 自己在业务层处理
 * 在{@link DynamicDatasource#getConnection()}获取链接，如果用注解事务不能保证事务完整
 * 可以在同一个数据源内，调{@link com.xl.mphelper.service.CustomServiceImpl#doInTransaction(Runnable)}来开启一个事务
 */
public interface ITableShardDbType {
    /**
     * 数据库类型
     *
     * @return
     */
    DbType getDbType();

    /**
     * 必须返回单列，值为表名,传入的是待建表的值
     * 如果没有的话，就不会走检查逻辑
     *
     * @param curTableNames
     * @return
     */
    default String getCheckTableSQL(Collection<String> curTableNames, Connection connection) {
        return null;
    }

    ;

    class MysqlShard implements ITableShardDbType {

        private static String DEFAULT_GET_TABLE_SQL = "select TABLE_NAME from information_schema.TABLES where  %s  TABLE_NAME in %s";

        @Override
        public DbType getDbType() {
            return DbType.mysql;
        }

        @Override
        public String getCheckTableSQL(Collection<String> curTableNames, Connection connection) {
            StringBuilder tableParam = new StringBuilder("(");
            Iterator<String> iterator = curTableNames.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                tableParam.append("'").append(next).append("'").append(",");
            }
            int i1 = tableParam.lastIndexOf(",");
            tableParam.replace(i1, tableParam.length(), ")");
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                if (connection instanceof JDBC4DatabaseMetaData) {
                    Field database = ReflectionUtils.findField(JDBC4DatabaseMetaData.class, "database");
                    ReflectionUtils.makeAccessible(database);
                    Object scheme = ReflectionUtils.getField(database, metaData);
                    return String.format(DEFAULT_GET_TABLE_SQL, "TABLE_SCHEMA=" + scheme + " and ", tableParam);
                }
            } catch (SQLException throwables) {
                throw new IllegalStateException(throwables);
            }
            return null;
        }
    }
}
