package com.xl.cloud.sharding.job;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.xl.cloud.sharding.listen.NacosConfigEventListener;
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


        ConfigService configService = configManager.getConfigService();
        //获取配置文件内容
        String dataId = NacosConfigEventListener.getDataId(environment);
        String group = NacosConfigEventListener.getGroup(environment);
        String content = configService.getConfig(dataId, group, 500);
        //转换并替换对应的值
        Map map = YamlUtils.yamlStr2Map(content);
        String orderInfo = YamlUtils.replaceValue("spring.shardingsphere.rules.sharding.tables.order_info.actual-data-nodes", map, s -> {
            String appendValue = ",demo.order_info" + suffix;
            if (!s.contains(appendValue)) {
                return s + appendValue;
            }
            return s;
        });
        String orderDetail = YamlUtils.replaceValue("spring.shardingsphere.rules.sharding.tables.order_detail.actual-data-nodes", map, s -> {
            String appendValue = ",demo.order_detail" + suffix;
            if (!s.contains(appendValue)) {
                return s + appendValue;
            }
            return s;
        });
        String s = YamlUtils.map2YamlStr(map);
        log.info("文件{},内容{}", dataId, s);
        //推送最新的配置
        boolean b = configService.publishConfig(dataId, group, s);
        //推送完之后，要反射找到对应的值改一下
        //TagRule
    }

}
