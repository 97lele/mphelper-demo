package com.xl.mphelper.dynamic;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author tanjl11
 * @date 2021/10/27 10:50
 */

@ConfigurationProperties(prefix = "mphelper.dynamic")
@Configuration
public class DynamicDatasourceProperties {
    protected Map<String, DruidDataSource> datasources;
    protected String defaultSource;

    public DruidDataSource getDefaultDatasource() {
        if (datasources != null) {
            return datasources.get(defaultSource);
        }
        return null;
    }

    public void setDefaultSource(String defaultSource) {
        this.defaultSource = defaultSource;
    }

    public void setDatasources(Map<String, DruidDataSource> datasources) {
        this.datasources = datasources;
    }
}
