package com.xl.shardingjdbc.demo.shard;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Properties;

@Slf4j
public class LocalDateShardAlgorithm implements StandardShardingAlgorithm<LocalDateTime> {

    private String range;

    private Properties props = new Properties();

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<LocalDateTime> shardingValue) {
        log.info("可用表名{}", availableTargetNames);
        return availableTargetNames.stream().findFirst().get();
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<LocalDateTime> shardingValue) {
        return availableTargetNames;
    }

    @Override
    public void init() {
        log.info("初始化策略");
    }

    @Override
    public String getType() {
        return "LOCAL_DATE";
    }

    @Override
    public Properties getProps() {
        return props;
    }

    @Override
    public void setProps(Properties props) {
        log.info("配置文件{}", props);
    }
}
