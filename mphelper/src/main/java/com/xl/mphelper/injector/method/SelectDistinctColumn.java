package com.xl.mphelper.injector.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.xl.mphelper.enums.CustomSqlMethodEnum;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author tanjl11
 * @date 2021/10/11 14:20
 */
public class SelectDistinctColumn extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        CustomSqlMethodEnum sqlMethod = CustomSqlMethodEnum.SELECT_DISTINCT_COLUMN;
        String sqlTemplate = sqlMethod.getSql();
        String tableName = tableInfo.getTableName();
        String finalSql = String.format(sqlTemplate, tableName);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, finalSql, modelClass);
        return this.addSelectMappedStatementForTable(mapperClass,sqlMethod.getMethod(),sqlSource,tableInfo);
    }
}
