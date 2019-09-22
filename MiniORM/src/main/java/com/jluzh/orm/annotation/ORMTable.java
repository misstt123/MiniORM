package com.jluzh.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: 该注解原来作用在表上
 * @Author lyh-god
 **/
@Retention(RetentionPolicy.RUNTIME)//设置注解保留策略
@Target(ElementType.TYPE)//设置注解作用到类上
public @interface ORMTable {
    public String name() default "";
}
