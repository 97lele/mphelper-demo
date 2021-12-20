package com.xl.tracer;

import com.xl.tracer.plugin.IPlugin;
import com.xl.tracer.plugin.MatchPoint;
import com.xl.tracer.plugin.PluginFactory;
import com.xl.tracer.trace.TracingContext;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author tanjl11
 * @date 2021/12/14 18:15
 * esHost:xx
 * recordRate:100
 * plugins:xx,xx,xx
 * <p>
 * bizPath:com.xl.mphelper.example;esHost:127.0.0.1;plugins:mysql,mybatis,redis;recordRate:100
 */
public class Entrance {
    public static String ES_HOST = "127.0.0.1";
    public static int RECORD_RATE = 100;
    public static String BIZ_PATH = "com.xl.mphelper.expamle";

    public static void premain(String agentArgs, Instrumentation inst) {
        String[] allArgs = agentArgs.split(";");
        Set<String> pluginNames = new HashSet();
        pluginNames.add("biz");
        List<IPlugin> pluginGroup = new ArrayList<>();
        pluginGroup.add(PluginFactory.getByName("biz"));
        for (String arg : allArgs) {
            String[] split = arg.split(":");
            String key = split[0];
            String[] param = split[1].split(",");
            if ("esHost".equals(key)) {
                ES_HOST = param[0];
            }
            if ("bizPath".equals(key)) {
                BIZ_PATH = param[0];
            }
            if ("recordRate".equals(key)) {
                RECORD_RATE = Integer.parseInt(param[0]);
            }
            if ("plugins".equals(key)) {
                for (String pluginName : param) {
                    if (!pluginNames.contains(pluginName)) {
                        IPlugin plugin = PluginFactory.getByName(pluginName);
                        pluginNames.add(pluginName);
                        if (plugin != null) {
                            TracingContext.logger.debug("load:" + pluginName);
                            pluginGroup.add(plugin);
                        }
                    }

                }
            }
        }
        if (pluginGroup.isEmpty()) {
            return;
        }
        pluginNames = null;
        AgentBuilder agentBuilder = new AgentBuilder.Default();
        for (IPlugin plugin : pluginGroup) {
            for (MatchPoint point : plugin.points()) {
                AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
                    builder = builder.visit(Advice.to(plugin.adviceClass()).on(point.buildMethodsMatcher()));
                    return builder;
                };
                agentBuilder = agentBuilder.type(point.buildTypesMatcher()).transform(transformer).asTerminalTransformation();
            }
        }
        //监听
        AgentBuilder.Listener listener = new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
                TracingContext.logger.trace("enhance:" + typeDescription);
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {

            }

            @Override
            public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {

            }

            @Override
            public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

            }

        };
        agentBuilder
                .with(listener)
                .installOn(inst);


    }
}
