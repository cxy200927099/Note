
[TOC]

# android 小知识点

## gradle相关
gradle.properties: 这是android studio创建工程的时候生成的
build.gradle: 这是编译android工程的配置文件

### build.gradle中获取gradle.properties的值

- 用途
可以用在配置工程上，比如某个library库，开发模式的时候需要调试，因此要指定工程属性为`apply plugin: 'com.android.application'`
但是在生产模式这个库需要指定为`apply plugin: 'com.android.library'`,因此可以在`gradle.properties`中定义一个变量，然后在
gradle中获取变量的值来决定当前工程的属性，具体做法
gradle.properties中```isModule=True```
build.gradle中
```
//这里由于gradle.properties中定义的变量是string类型，需要做下转换
if (isModule.toBoolean()){
  apply plugin: 'com.android.library'
}else{
  apply plugin: 'com.android.application'
}
```
androidManifest.xml文件配置化
```gradle
sourceSets {
 main {
 if (isModule.toBoolean()) {
 manifest.srcFile 'src/main/module/AndroidManifest.xml'
 } else {
 manifest.srcFile 'src/main/AndroidManifest.xml'
 }
 }
}
```



## 主流框架
MVVM：
google出的jetpack框架 andoridx.xxxx
google的demo中都用kotlin实现，需要学习kotlin语法
学习注解: 原理
依赖注入: 原理

MVP:


rxjava: 比较了解就好，不推荐深入

## 网络相关
[知识点](http://www.52im.net/thread-1963-1-1.html)



## android-studio增加模板
[参见这里](https://juejin.cn/post/6844903782338265095)

