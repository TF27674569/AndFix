package com.alipay.euler.andfix.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * apatch生成工具用的阿里的这里的包和注解要与阿里一致
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodReplace {
    String clazz();
    String method();
}
