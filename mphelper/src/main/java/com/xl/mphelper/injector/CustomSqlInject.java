package com.xl.mphelper.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.xl.mphelper.injector.method.*;
import com.xl.mphelper.mapper.ComposeKeyMapper;
import com.xl.mphelper.mapper.CustomMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.util.List;

/**
 * @author tanjl11
 * @date 2021/02/04 17:17
 * 继承默认的注入器
 */
//默认打开
@ConditionalOnExpression("${mphelper.custom-mapper.enabled:true}")
@Component
public class CustomSqlInject extends DefaultSqlInjector {
    //初始化时候会加载
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        //属于自己定义的mapper才添加方法
        if (((ParameterizedTypeImpl) mapperClass.getGenericInterfaces()[0]).getRawType().equals(CustomMapper.class)) {
            methodList.add(new InsertBatch());
            methodList.add(new UpdateBatchByIds());
            methodList.add(new SelectDistinctColumn());
        }
        if (((ParameterizedTypeImpl) mapperClass.getGenericInterfaces()[0]).getRawType().equals(ComposeKeyMapper.class)) {
            methodList.add(new SelectByComposeKeys());
            methodList.add(new SelectIdsByComposeKeys());
            methodList.add(new UpdateByComposeKeys());
            methodList.add(new DeleteByComposeKeys());
            methodList.add(new InsertBatch());
            methodList.add(new SelectDistinctColumn());
        }
        return methodList;
    }
}
