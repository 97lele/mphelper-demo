package com.xl.mphelper.injector.method;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.xl.mphelper.enums.CustomSqlMethodEnum;
import com.xl.mphelper.injector.SQLConditionWrapper;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;

/**
 * @author tanjl11
 * @date 2021/02/04 17:19
 */
public class InsertBatch extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        KeyGenerator keyGenerator = new NoKeyGenerator();

        CustomSqlMethodEnum sqlMethod = CustomSqlMethodEnum.INSERT_BATCH;

        // ==== 拼接 sql 模板 ==============
        StringBuilder columnScriptBuilder = new StringBuilder(LEFT_BRACKET);
        StringBuilder valuesScriptBuilder = new StringBuilder(LEFT_BRACKET);
        // 主键拼接
        if (StringUtils.isNotBlank(tableInfo.getKeyColumn())) {
            //(x1,x2,x3
            columnScriptBuilder.append(tableInfo.getKeyColumn()).append(COMMA);
            //(item.xx,item.xx
            valuesScriptBuilder.append(SqlScriptUtils.safeParam(SQLConditionWrapper.ITEM + DOT + tableInfo.getKeyProperty())).append(COMMA);
        }
        // 普通字段拼接
        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        for (TableFieldInfo fieldInfo : fieldList) {
            //有插入默认填充器的不参与赋值
            if (!fieldInfo.isWithInsertFill() && fieldInfo.isWithUpdateFill()) {
                continue;
            }
            columnScriptBuilder.append(fieldInfo.getColumn()).append(COMMA);
            valuesScriptBuilder.append(SqlScriptUtils.safeParam(SQLConditionWrapper.getCondition(fieldInfo.getProperty()).toString())).append(COMMA);
        }
        // 替换多余的逗号为括号
        //(x1,x2)
        columnScriptBuilder.setCharAt(columnScriptBuilder.length() - 1, ')');
        //(item.xx,item.xx2)
        valuesScriptBuilder.setCharAt(valuesScriptBuilder.length() - 1, ')');
        // sql 模板占位符替换
        String columnScript = columnScriptBuilder.toString();
        String valuesScript = valuesScriptBuilder.toString();
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), columnScript, valuesScript);


        // === mybatis 主键逻辑处理：主键生成策略，以及主键回填=======
        String keyColumn = null;
        String keyProperty = null;
        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {
            if (tableInfo.getIdType() == IdType.AUTO) {
                /** 自增主键 */
                keyGenerator = new Jdbc3KeyGenerator();
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();
            } else {
                if (null != tableInfo.getKeySequence()) {
                    keyGenerator = TableInfoHelper.genKeyGenerator(sqlMethod.getMethod(), tableInfo, builderAssistant);
                    keyProperty = tableInfo.getKeyProperty();
                    keyColumn = tableInfo.getKeyColumn();
                }
            }
        }
        // 模板写入
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, sqlMethod.getMethod(), sqlSource, keyGenerator, keyProperty, keyColumn);
    }
}
