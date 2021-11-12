package com.xl.cloud.sharding.listen;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.xl.cloud.sharding.utils.YamlUtils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author tanjl11
 * @date 2021/11/12 16:31
 * 还是要解析
 */
@Slf4j
//@Component
public class NacosConfigEventListener implements Listener, InitializingBean {
    @Resource
    private NacosConfigManager configManager;
    @Resource
    private Environment environment;

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void receiveConfigInfo(String s) {
        Map root = YamlUtils.yamlStr2Map(s);
        String fromMap = YamlUtils.getFromMap(root, "123");

        log.info(s);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.configManager.getConfigService().addListener(getDataId(environment), getGroup(environment), this);
    }

    public static String getDataId(Environment environment) {
        StringBuilder dataId = new StringBuilder();
        dataId.append(environment.getProperty("spring.application.name"));
        String profile = environment.getProperty("spring.profiles.active");
        if (!StringUtil.isNullOrEmpty(profile)) {
            dataId.append("-").append(profile);
        }
        String fileExtension = environment.getProperty("spring.cloud.nacos.config.file-extension");
        dataId.append(".").append(fileExtension);
        return dataId.toString();
    }

    public static String getGroup(Environment environment) {
        return environment.getProperty("spring.cloud.nacos.discovery.group");
    }
}
