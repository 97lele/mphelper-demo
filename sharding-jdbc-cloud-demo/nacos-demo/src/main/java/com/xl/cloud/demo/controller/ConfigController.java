package com.xl.cloud.demo.controller;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/config")
@RefreshScope
@Slf4j
public class ConfigController {
    @Resource
    private NacosConfigManager configManager;
    @Resource
    private StandardEnvironment environment;

    @Value("${useLocalCache:false}")
    private boolean useLocalCache;

    @RequestMapping("/get")
    public boolean get() {
        return useLocalCache;
    }

    @RequestMapping("/testGet")
    public void testGet() throws NacosException {
        ConfigService configService = configManager.getConfigService();
        StringBuilder dataId = new StringBuilder();
        dataId.append(environment.getProperty("spring.application.name"));
        String profile = environment.getProperty("spring.profiles.active");
        if (!StringUtils.isEmpty(profile)) {
            dataId.append("-").append(profile);
        }
        String fileExtension = environment.getProperty("spring.cloud.nacos.config.file-extension");
        dataId.append(".").append(fileExtension);
        String group = environment.getProperty("spring.cloud.nacos.discovery.group");
        String value = dataId.toString();
        String content = configService.getConfig(value, group, 500);
        log.info("文件{},内容{}", dataId, content);
        boolean b = configService.publishConfig(value, group, content);
    }

    @GetMapping(value = "/echo/{string}")
    public String echo(@PathVariable String string) {
        return "Hello Nacos Discovery " + string;
    }
}