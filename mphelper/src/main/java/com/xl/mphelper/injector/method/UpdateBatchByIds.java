package com.xl.mphelper.injector.method;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.xl.mphelper.enums.CustomSqlMethodEnum;
import com.xl.mphelper.injector.SQLConditionWrapper;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;
import java.util.Objects;

/**
 * @author tanjl11
 * @date 2021/02/04 17:22
 */
public class UpdateBatchByIds extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        CustomSqlMethodEnum sqlMethod = CustomSqlMethodEnum.UPDATE_BATCH;
        /**
         * update table set <if test ="item.pro!=null">key1=#{item.pro}</if>, key2=#{item.pro2} where keycolom = %s
         * */
        StringBuilder withChevScript = new StringBuilder();
        StringBuilder lastFiledScriptBuilder=new StringBuilder();
        StringBuilder keyScriptBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(tableInfo.getKeyColumn())) {
            keyScriptBuilder.append(tableInfo.getKeyColumn()).append(EQUALS).append(SqlScriptUtils.safeParam(SQLConditionWrapper.ITEM + DOT + tableInfo.getKeyProperty()));
        }
        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        for (int i = 0; i < fieldList.size(); i++) {
            TableFieldInfo fieldInfo = fieldList.get(i);
            Boolean isLast=(i==(fieldList.size()-1));
            //有插入默认填充器的不参与赋值
            if (fieldInfo.isWithInsertFill() && !fieldInfo.isWithUpdateFill()) {
                continue;
            }
            boolean change = false;
            if (Objects.equals(fieldInfo.getUpdateStrategy(), FieldStrategy.NOT_NULL) && !fieldInfo.isWithUpdateFill()) {
                SQLConditionWrapper.appendNotNull(withChevScript, fieldInfo.getProperty());
                change = true;
            }
            if (Objects.equals(fieldInfo.getUpdateStrategy(), FieldStrategy.NOT_EMPTY) && !fieldInfo.isWithUpdateFill()) {
                SQLConditionWrapper.appendNotEmpty(withChevScript, fieldInfo.getProperty());
                change = true;
            }
            if (change) {
                withChevScript.append(fieldInfo.getColumn()).append(EQUALS).append(SqlScriptUtils.safeParam(SQLConditionWrapper.getCondition(fieldInfo.getProperty()).toString()));
                withChevScript.append(COMMA).append("</if>");
                if(isLast&&lastFiledScriptBuilder.length()==0){
                    //如果没有其他字段替补，弄个占位的，不然会出现语法错误的情况
                    withChevScript.append(tableInfo.getKeyColumn()).append(EQUALS).append(tableInfo.getKeyColumn());
                }
            }else{
                lastFiledScriptBuilder.append(fieldInfo.getColumn()).append(EQUALS).append(SqlScriptUtils.safeParam(SQLConditionWrapper.getCondition(fieldInfo.getProperty()).toString())).append(COMMA);
            }
        }
        //处理多余的逗号
        if(!StringUtils.isBlank(lastFiledScriptBuilder)&&!StringUtils.isBlank(withChevScript)){
            int leftChevIndex= withChevScript.lastIndexOf(LEFT_CHEV);
            if(withChevScript.charAt(leftChevIndex-1)!=','){
                withChevScript.replace(leftChevIndex,leftChevIndex+1,",<");
            }
            if(lastFiledScriptBuilder.lastIndexOf(COMMA)==lastFiledScriptBuilder.length()-1){
                lastFiledScriptBuilder.deleteCharAt(lastFiledScriptBuilder.length()-1);
            }
        }
        withChevScript.append(lastFiledScriptBuilder);
        // sql 模板占位符替换
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), withChevScript, keyScriptBuilder);
        // 模板写入
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addUpdateMappedStatement(mapperClass, modelClass, sqlMethod.getMethod(), sqlSource);
    }
}
