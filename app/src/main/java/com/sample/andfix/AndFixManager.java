package com.sample.andfix;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.alipay.euler.andfix.annotation.MethodReplace;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * 加载dex替换崩溃函数
 */
public class AndFixManager {

    private Context context;
    private File optFile;

    public AndFixManager(Context context) {
        this.context = context;
        AndFix.init(Build.VERSION.SDK_INT);
    }


    /**
     * 修复
     * @param file dex
     * @param classLoader 加载起
     * @param fixClassLists 问题class全类名
     */
    public void fix(File file, final ClassLoader classLoader, List<String> fixClassLists){
        optFile = new File(context.getFilesDir(),file.getAbsolutePath());

        if (optFile.exists()){
            optFile.delete();
        }

        // 加载dex
        try {
            DexFile dexFile = DexFile.loadDex(file.getAbsolutePath(),optFile.getAbsolutePath(),Context.MODE_PRIVATE);

            // 自定义一个classLoader
            ClassLoader loader = new ClassLoader() {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    // 加载实现是通过传入的加载apk的classLoader加载的class
                    Class<?> clazz = classLoader.loadClass(name);

                    // 没有则反射创建
                    if (clazz== null){
                        clazz = Class.forName(name);
                    }
                    return clazz;
                }
            };

            // 获取到需要加载的class之后 加载class 替换崩溃函数
            Enumeration<String> entries = dexFile.entries();

            while (entries.hasMoreElements()) {
                String key = entries.nextElement();
                if (!fixClassLists.contains(key)){
                    continue;
                }

                //加载之后实现修复
                Class realClazz = dexFile.loadClass(key,loader);


                if (realClazz!=null){
                    fixClass(realClazz);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 修复class
     * @param realClazz
     */
    private void fixClass(Class realClazz) {
        // 反射找函数名称
        Method[] methods = realClazz.getDeclaredMethods();

        // 找注解
        for (Method method : methods) {
            MethodReplace methodReplace = method.getAnnotation(MethodReplace.class);
            if (methodReplace!=null){
                String clazzName = methodReplace.clazz();
                String methodName = methodReplace.method();
                Log.i("clazz", "fix class: "+clazzName+"  fix method:" +methodName);

                replaceMethod(clazzName,methodName,method);

            }
        }

    }

    private void replaceMethod(String targetClazzName, String targetMethodName, Method fixMethod) {
        try {

            // 通过名称反射class
            Class targetClazz = Class.forName(targetClazzName);
            if (targetClazz!=null){
                // 找到对应的函数
                Method targetMethod = targetClazz.getDeclaredMethod(targetMethodName,fixMethod.getParameterTypes());

                // ndk替换
                AndFix.fix(targetMethod,fixMethod);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
