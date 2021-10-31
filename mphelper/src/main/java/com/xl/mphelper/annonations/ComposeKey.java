package com.xl.mphelper.annonations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author tanjl11
 * @date 2021/09/23 11:58
 * 只是加个标记,作用于实体上，映射方面还是使用了TableField
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ComposeKey {

}
