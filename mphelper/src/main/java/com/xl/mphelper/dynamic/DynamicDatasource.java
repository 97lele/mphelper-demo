package com.xl.mphelper.dynamic;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * @author tanjl11
 * @date 2021/10/27 9:44
 */
@Component
@ConditionalOnExpression("${mphelper.dynamic.enable:false}")
public class DynamicDatasource extends AbstractDataSource {
    @Resource
    private DynamicDatasourceProperties properties;

    @Override
    public Connection getConnection() throws SQLException {
        DruidDataSource dataSource = chooseDataSource();
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        DruidDataSource dataSource = chooseDataSource();
        return dataSource.getConnection(username, password);
    }

    private DruidDataSource chooseDataSource() {
        String key = DynamicDataSourceHolder.getCurrentKey();
        if (key == null) {
            key = properties.defaultSource;
        }
        DruidDataSource dataSource = properties.datasources.get(key);
        if (dataSource == null) {
            throw new IllegalStateException(String.format("获取[%s]数据源失败", key));
        }
        return dataSource;
    }
}
