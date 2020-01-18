package com.km.data.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field{
    //字段名
    String fieldName();
    //字段解释
    String desc() default "";
    //是否为必填字段
    boolean necessary() default true;
}
