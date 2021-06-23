[TOC]

## 理解

unique_ptr也是对auto_ptr的替换。unique_ptr遵循着独占语义。在任何时间点，资源只能唯一地被一个unique_ptr占有。当unique_ptr离开作用域，所包含的资源被释放。如果资源被其它资源重写了，之前拥有的资源将被释放。所以它保证了他所关联的资源总是能被释放。

## 使用

### 创建

1. 创建非数组类型

与shared_ptr一样

```cpp
unique_ptr<int> uptr( new int );
```

2. 创建数组类型



unique_ptr提供了创建数组对象的特殊方法，当指针离开作用域时，调用delete[]代替delete。当创建unique_ptr时，这一组对象被视作模板参数的部分。这样，程序员就不需要再提供一个指定的析构方法，如下：

```cpp
unique_ptr<int[ ]> uptr( new int[5] );
```

当把unique_ptr赋给另外一个对象时，资源的所有权就会被转移。

**记住unique_ptr不提供复制语义（拷贝赋值和拷贝构造都不可以），只支持移动语义(move semantics).**s

在上面的例子里，如果upt3和upt5已经拥有了资源，只有当拥有新资源时，之前的资源才会释放。

```cpp
 unique_ptr<int> ap(new int(88 ));

  unique_ptr<int> one (ap) ; // 会出错

  unique_ptr<int> two = one; //会出错

```



## 接口

unique_ptr提供的接口和传统指针差不多，但是不支持指针运算。

unique_ptr提供一个release()的方法，释放所有权。release和reset的区别在于，release仅仅释放所有权但不释放资源，reset也释放资源。


