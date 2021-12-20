package com.xl.tracer.plugin;


import com.xl.tracer.plugin.biz.BizPlugin;
import com.xl.tracer.plugin.redis.RedisPlugin;
import com.xl.tracer.plugin.rpc.SpringMvcPlugin;
import com.xl.tracer.plugin.sql.MybatisPlugin;
import com.xl.tracer.plugin.sql.MysqlPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tanjl11
 * @date 2021/12/14 18:16
 */
public class PluginFactory {
    public static Map<String, IPlugin> pluginMap = new HashMap<>();

    static {
        //链路监控
        BizPlugin bizPlugin = new BizPlugin();
        pluginMap.put(bizPlugin.name(), bizPlugin);
        MysqlPlugin mysqlPlugin = new MysqlPlugin();
        pluginMap.put(mysqlPlugin.name(), mysqlPlugin);
        RedisPlugin redisPlugin = new RedisPlugin();
        pluginMap.put(redisPlugin.name(), redisPlugin);
        MybatisPlugin mybatisPlugin = new MybatisPlugin();
        pluginMap.put(mybatisPlugin.name(), mybatisPlugin);
        SpringMvcPlugin springmvcPlugin = new SpringMvcPlugin();
        pluginMap.put(springmvcPlugin.name(), springmvcPlugin);
    }

    public static IPlugin getByName(String name) {
        return pluginMap.get(name);
    }
}
