package com.xl.mphelper.injector.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.xl.mphelper.annonations.ComposeKey;
import com.xl.mphelper.enums.CustomSqlMethodEnum;
import com.xl.mphelper.injector.SQLConditionWrapper;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tanjl11
 * @date 2021/09/23 12:00
 */
public class SelectByComposeKeys extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        CustomSqlMethodEnum sqlMethod = CustomSqlMethodEnum.SELECT_BY_COMPOSEKEYS;
        //select #{ew.sqlSelect} from table_name where  (composekey1=xx and composekey2=xx) or (composekey1=xx1 and composekey2=xx1)
        String sqlTemplate = sqlMethod.getSql();
        String tableName = tableInfo.getTableName();
        List<TableFieldInfo> composeKeys = tableInfo.getFieldList().stream().filter(e -> e.getField().isAnnotationPresent(ComposeKey.class))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(composeKeys)){
            throw new IllegalStateException("not composeKey found in class:"+modelClass.getName());
        }
        StringBuilder builder=new StringBuilder();
        for (int i = 0; i < composeKeys.size(); i++) {
            TableFieldInfo composeKey = composeKeys.get(i);
            if (!composeKey.getColumn().equals(composeKey.getProperty())) {
                builder.append(" AS ").append(composeKey.getProperty());
            }
            builder.append(composeKey.getColumn()).append(EQUALS).append(SqlScriptUtils.safeParam(SQLConditionWrapper.ITEM + DOT + composeKey.getProperty()));
            if(i!=composeKeys.size()-1){
                builder.append(" ").append(AND).append(" ");
            }
        }
        String finalSql = String.format(sqlTemplate, tableName, builder);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, finalSql, modelClass);
        return this.addSelectMappedStatementForTable(mapperClass,sqlMethod.getMethod(),sqlSource,tableInfo);
    }
}
