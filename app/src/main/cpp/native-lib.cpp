#include <jni.h>
#include <string>
#include "dalvik.h"

#define  LOG_TAG    "AndFix"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_sample_andfix_AndFix_init(JNIEnv *env, jclass type, jint api)
{

    // 打开java dvm虚拟机
    void *handle =  dlopen("libdvm.so",RTLD_NOW);

    if (handle)
    {
        // 通过虚拟机和函数名拿到对应的函数指针
        // 获取dvmDecodeIndirectRef_fnPtr和dvmThreadSelf_fnPtr俩个函数
        // 这两个函数可以通过类对象获取ClassObject结构体
        dvmDecodeIndirectRef_fnPtr = (dvmDecodeIndirectRef_func)(dlsym(handle, api > 10 ? "_Z20dvmDecodeIndirectRefP6ThreadP8_jobject" : "dvmDecodeIndirectRef"));

        dvmThreadSelf_fnPtr = (dvmThreadSelf_func)(dlsym(handle, api > 10 ? "_Z13dvmThreadSelfv" : "dvmThreadSelf"));

        //通过Java层Method对象的getDeclaringClass方法
        //后续会调用该方法获取某个方法所属的类对象
        //因为Java层只传递了Method对象到native层
        jclass clazz = env->FindClass("java/lang/reflect/Method");
        jClassMethod = env->GetMethodID(clazz, "getDeclaringClass", "()Ljava/lang/Class;");
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_sample_andfix_AndFix_fix(JNIEnv *env, jclass type, jobject targetMethod,jobject fixMethod)
{

    // 拿到修复函数的class对象
    jobject clazz = env->CallObjectMethod(fixMethod, jClassMethod);

    // 通过class对象获取class的所有信息的结构体
    ClassObject* clz  = reinterpret_cast<ClassObject *>(dvmDecodeIndirectRef_fnPtr(dvmThreadSelf_fnPtr(), clazz));

    //更改状态为类初始化完成的状态
    clz->status = CLASS_INITIALIZED;

    //通过java层传递的方法对象，在native层获得他们的结构体
    Method* javaFixMethod = (Method*) env->FromReflectedMethod(fixMethod);
    Method* javaTargetMethod = (Method*) env->FromReflectedMethod(targetMethod);

    // 替换参数
    //核心方法如下，就是替换新旧方法结构体中的信息
    javaTargetMethod->accessFlags |= ACC_PUBLIC;
    javaTargetMethod->methodIndex = javaFixMethod->methodIndex;
    javaTargetMethod->jniArgInfo = javaFixMethod->jniArgInfo;
    javaTargetMethod->registersSize = javaFixMethod->registersSize;
    javaTargetMethod->outsSize = javaFixMethod->outsSize;
    javaTargetMethod->insSize = javaFixMethod->insSize;
    javaTargetMethod->prototype = javaFixMethod->prototype;
    javaTargetMethod->insns = javaFixMethod->insns;
    javaTargetMethod->nativeFunc = javaFixMethod->nativeFunc;
}