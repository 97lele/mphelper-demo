package com.xl.mphelper.injector;


import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;

/**
 * @author tanjl11
 * @date 2021/02/04 16:29
 */
public class SQLConditionWrapper {
    public final static String ITEM = "item";

    public static StringBuilder appendNotNull(StringBuilder builder, String property) {
        return appendEnd(appendStart(builder, getCondition(property)));
    }

    public static StringBuilder appendNotEmpty(StringBuilder builder, String property) {
        StringBuilder condition = getCondition(property);
        StringBuilder append = appendStart(builder, condition)
                .append(" ").append(AND).append(" ").append(condition).append(EXCLAMATION_MARK).append(EQUALS).append(" ").append("''");
        return appendEnd(append);
    }

    private static StringBuilder appendEnd(StringBuilder builder) {
        builder.append("\"")
                .append(RIGHT_CHEV);
        return builder;
    }

    private static StringBuilder appendStart(StringBuilder builder, StringBuilder item) {
        builder.append(LEFT_CHEV)
                .append("if test=\"")
                .append(item).append(EXCLAMATION_MARK).append(EQUALS).append(NULL);
        return builder;
    }

    public static StringBuilder getCondition(String property) {
        return new StringBuilder()
                .append(ITEM).append(DOT).append(property);
    }
}
