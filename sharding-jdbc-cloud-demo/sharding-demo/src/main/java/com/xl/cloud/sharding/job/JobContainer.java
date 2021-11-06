package com.xl.cloud.sharding.job;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.xl.cloud.sharding.mapper.OrderDetailMapper;
import com.xl.cloud.sharding.mapper.OrderInfoMapper;
import com.xl.cloud.sharding.utils.YamlUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JobContainer {
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private NacosConfigManager configManager;
    @Resource
    private StandardEnvironment environment;


    @XxlJob(value = "createOrderTable")
    public void createOrderTable() throws Exception {
        //首先创建一个月后的
        Date now = new Date();
        Calendar instance = Calendar.getInstance();
        instance.setTime(now);
        int curMonth = instance.get(Calendar.MONTH);
        instance.set(Calendar.MONTH, curMonth + 1);
        int year = instance.get(Calendar.YEAR);
        int month = curMonth + 2;
        String suffix = "_" + year + "_" + month;
        orderDetailMapper.createTable(suffix);
        orderInfoMapper.createTable(suffix);
        //创建完毕后，更改配置
        StringBuilder dataId = new StringBuilder();
        dataId.append(environment.getProperty("spring.application.name"));
        String profile = environment.getProperty("spring.profiles.active");
        String group = environment.getProperty("spring.cloud.nacos.discovery.group");
        if (!StringUtil.isNullOrEmpty(profile)) {
            dataId.append("-").append(profile);
        }
        String fileExtension = environment.getProperty("spring.cloud.nacos.config.file-extension");
        dataId.append(".").append(fileExtension);
        String value = dataId.toString();
        ConfigService configService = configManager.getConfigService();
        String content = configService.getConfig(value, group, 500);
        Map map = YamlUtils.yamlStr2Map(content);
        YamlUtils.replaceValue("spring.shardingsphere.rules.sharding.tables.order_info.actual-data-nodes", map, String.class, s -> {
            String appendValue = ",demo.order_info" + suffix;
            if (!s.contains(appendValue)) {
                return s + appendValue;
            }
            return s;
        });
        String s = YamlUtils.map2YamlStr(map);
        log.info("文件{},内容{}", dataId, s);
        boolean b = configService.publishConfig(value, group, s);
    }

}
