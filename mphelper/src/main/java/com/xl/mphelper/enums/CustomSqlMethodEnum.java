package com.xl.mphelper.enums;

/**
 * @author tanjl11
 * @date 2021/02/04 17:15
 */
public enum CustomSqlMethodEnum {
    /**
     * 批量插入
     */
    INSERT_BATCH("insertBatch",
            "批量插入",
            "<script>\n"
                    + "INSERT INTO %s %s VALUES \n"
                    + "<foreach collection=\"collection\"  item=\"item\" separator=\",\"> %s\n </foreach>\n"
                    + "</script>"),
    /**
     * 批量更新
     */
    UPDATE_BATCH("updateBatchByIds",
            "批量更新",
            "<script>\n" +
                    "<foreach collection=\"collection\" item=\"item\" separator=\";\"> update %s set %s where %s </foreach>\n"
                    + "</script>"
    ),
    /**
     * 获取单个列
     */
    SELECT_DISTINCT_COLUMN("selectDistinctColumn"
            , "去重列",
            "<script>\n" +
                    "select distinct ${ew.sqlSelect} from %s ${ew.customSqlSegment}</script>"
    ),

    /**
     * 根据联合键查询
     */
    SELECT_BY_COMPOSEKEYS("selectByComposeKeys",
            "联合主键查询",
            "<script>" +
                    " select <choose> <when test=\"ew!=null and ew.sqlSelect != null and ew.sqlSelect != ''\"> ${ew.sqlSelect} </when> <otherwise> * </otherwise> </choose> from %s "
                    + "<where> <foreach collection=\"collection\" item=\"item\" open=\"(\" close=\")\" separator=\"or\"> ( %s ) </foreach> <if test=\"ew!=null and ew.sqlSegment != null and ew.sqlSegment != ''\">\n" +
                    "AND ${ew.sqlSegment}\n" +
                    "</if> </where> </script>"
    ),

    /**
     * 根据联合键查询
     */
    SELECT_IDS_BY_COMPOSEKEYS("selectIdsByComposeKeys",
            "联合主键查询id",
            "<script>" +
                    " select %s from %s "
                    + "<where> <foreach collection=\"collection\" item=\"item\" open=\"(\" close=\")\" separator=\"or\"> ( %s ) </foreach> </where> </script>"
    ),

    /**
     * 根据联合主键删除
     */
    DELETE_BY_COMPOSEKEYS("deleteByComposeKeys",
            "联合主键删除",
            "<script>" +
                    " delete from %s "
                    + "<where> <foreach collection=\"collection\" item=\"item\" open=\"(\" close=\")\" separator=\"or\"> ( %s ) </foreach> </where> </script>"
    ),
    /**
     * 根据联合主键更新
     */
    UPDATE_BY_COMPOSEKEYS("updateByComposeKeys"
            , "联合主键批量修改",
            "<script>\n" +
                    "<foreach collection=\"collection\" item=\"item\" separator=\";\"> update %s set %s where %s </foreach>\n"
                    + "</script>"
    );


    private final String method;
    private final String desc;
    private final String sql;

    CustomSqlMethodEnum(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }

    public String getMethod() {
        return method;
    }

    public String getDesc() {
        return desc;
    }

    public String getSql() {
        return sql;
    }
}
