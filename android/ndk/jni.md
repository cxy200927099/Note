# JNI


## java 和c++相互调用
java端的类


### java 调用c++



### c++调用java方法
[参考](https://www.jianshu.com/p/78ea7c096f73)
通过jni的env指针来操作，具体有点类似于java的反射
1. 获取jclass
2.

### c++子线程调用java方法
[参考这里](https://www.jianshu.com/p/af20937a3bff)
jni中线程和JNIEnv是一一对应的，所以要想在子线程中调用java方法，需要获取当前线程的JNIEnv；
具体可以通过JavaVM 指针对象来获取，这个JavaVM指针可以通过在 JNI_Onload 回调中获取，这个函数在
java通过 System.loadLibrary("xxxx") 的时候，系统就会回调 jni中的 JNI_Onload 函数，并将 JavaVM 指针作为参数；

```cpp
//定义一个全局 java vm 实例
JavaVM *jvm;
//在加载动态库时回去调用 JNI_Onload 方法，在这里可以得到 JavaVM 实例对象
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    jvm = vm;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_6;
}


JNIEnv *env;
//1.根据 AttachCurrentThread 获取到当前线程的 JNIEnv 实例
vm->AttachCurrentThread(&env, 0);

//2.调用 java 函数
//call method

//3.解除挂载当前线程
vm->DetachCurrentThread();
```


### c++返回对象给java调用
主要原理就是返回一个c++的指针，在java层用 long 修饰的变量保存

```cpp
static inline jfieldID getHandleField(JNIEnv *env, jobject obj)
{
    jclass c = env->GetObjectClass(obj);
    // J is the type signature for long:
    // 获取java中定义的 long nativeHandle;
    return env->GetFieldID(c, "nativeHandle", "J");
}

template <typename T>
T *getHandle(JNIEnv *env, jobject obj)
{
    jlong handle = env->GetLongField(obj, getHandleField(env, obj));
    return reinterpret_cast<T *>(handle);
}

template <typename T>
void setHandle(JNIEnv *env, jobject obj, T *t)
{
    // 将传入的c++指针强转为 jlong
    jlong handle = reinterpret_cast<jlong>(t);
    // 直接值给java对象中的
    env->SetLongField(obj, getHandleField(env, obj), handle);
}

```











