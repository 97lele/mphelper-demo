package com.xl.shardingjdbc.demo.shard;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
public class LocalDateShardAlgorithm implements StandardShardingAlgorithm<LocalDateTime> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<LocalDateTime> shardingValue) {
        LocalDateTime value = shardingValue.getValue();
        String s = availableTargetNames.stream()
                .filter(e -> e.contains(value.getYear() + "_" + value.getMonth().getValue()))
                .findFirst().orElse(availableTargetNames.iterator().next());
        log.info("选取的表名{}", s);
        return s;
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
        return "CLASS_BASED";
    }

}
