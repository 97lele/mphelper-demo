package com.xl.cloud.sharding.utils;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

public final class YamlUtils {
    public static Map yamlStr2Map(String source) {
        Yaml yaml = new Yaml();
        InputStream inputStream = IOUtils.toInputStream(source, StandardCharsets.UTF_8);
        return yaml.loadAs(inputStream, Map.class);
    }

    public static String map2YamlStr(Map map) {
        Yaml yaml = new Yaml();
        ;
        return yaml.dump(map);
    }

    public static <T> void replaceValue(String key, Map root, Class<T> valueClass, Function<T, T> replace) {
        String[] split = key.split("\\.");
        for (int i = 0; i < split.length - 1; i++) {
            Object o = root.get(split[i]);
            if (o instanceof Map) {
                root = (Map) o;
            }
        }
        String resKey = split[split.length - 1];
        T value = (T) root.get(resKey);
        T newVal = replace.apply(value);
        root.put(resKey, newVal);
    }
}
