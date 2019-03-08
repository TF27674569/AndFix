package com.sample.andfix;

import java.lang.reflect.Method;

public class AndFix {


    static {
        System.loadLibrary("native-lib");
    }


    /**
     * 根据api 10 来区分版本
     * 调用不同的api函数
     *
     * @param api
     */
    public native static void init(int api);


    /**
     * 修复崩溃的函数 native 函数指针替换
     *
     * @param targetMethod 目标函数（崩溃函数）
     * @param fixMethod 修复好了的函数
     */
    public native static void fix(Method targetMethod,Method fixMethod);



}
