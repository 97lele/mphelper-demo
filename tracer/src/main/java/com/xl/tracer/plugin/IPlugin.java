package com.xl.tracer.plugin;

/**
 * @author tanjl11
 * @date 2021/12/14 16:21
 * 拦截器接口
 */
public interface IPlugin {
    /**
     * 名称
     * @return
     */
    String name();

    /**
     * 匹配规则
     * @return
     */
    MatchPoint[] points();

    /**
     * 拦截器类
     * @return
     */
    Class adviceClass();
}
