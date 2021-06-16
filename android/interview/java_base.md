[TOC]

# java base

## java四大引用类型
- 强引用 StrongReference
    平常 new创建的对象关联的引用，系统oom也不会回收这个引用
- 软引用 SoftReference
    只有在内存不足的时候JVM才会回收该对象。因此，这一点可以很好地用来解决OOM的问题，并且这个特性很适合用来实现缓存：比如网页缓存、图片缓存等。
- 弱引用 WeakReference
    内存不足，每次GC都会回收弱引用关联的对象，用于解决内存泄漏，比如
- 虚引用 PhantomReference
    如果一个对象与虚引用关联，则跟没有引用与之关联一样，在任何时候都可能被垃圾回收器回收，虚引用必须和引用队列关联使用
- 引用队列 ReferenceQueue
    使用SoftReference，WeakReference，PhantomReference 的时候，可以关联一个ReferenceQueue。那么当垃圾回收器准备回收一个被引用包装的对象时，该引用会被加入到关联的ReferenceQueue。程序可以通过判断引用队列中是否已经加入引用,来了解被引用的对象是否被GC回收

## equals 与 == 的区别
- 首先的区别是，equals 是方法，而 == 是操作符；
- 对于基本类型的变量来说（如 short、 int、 long、 float、 double），只能使用 == ，因为这些基本类型的变量没有 equals 方法。对于基本类型变量的比较，使用 == 比较， 一般比较的是它们的值
- 对于引用类型，如果需要区分是否 重写了 equals() 函数，
    - 没有重写  此时equals是 object的方法，其内部是 `return (this == obj);` 比较两个引用指向的地址是否相同
    - 重写了  例如 String 类，在判断指向的地址不相同时，判断 值 是否相同
```java
public static class Person {
        String name;

        public Person(String name) {
            this.name = name;
        }

        public Object clone()  {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return new Person(this.name);
            }
        }
    }

    private static void testEquals() {
        Person a = new Person("cxy");
        Person b = new Person("cxy");
        Person c = a;
        Person d = (Person) a.clone();
        System.out.println("a.equals(b):"+a.equals(b)); // false
        System.out.println("a.equals(c):"+a.equals(c)); // true
        System.out.println("b.equals(c):"+b.equals(c)); // false
        System.out.println("a.equals(d):"+a.equals(d)); // false
        System.out.println("b.equals(d):"+b.equals(d)); // false
        System.out.println("c.equals(d):"+c.equals(d)); // false
        System.out.println("a==b:"+(a==b)); // false
        System.out.println("a==c:"+(a==c)); // true
        System.out.println("b==c:"+(b==c)); // false
        System.out.println("a==d:"+(a==d)); // false
        System.out.println("b==d:"+(b==d)); // false
        System.out.println("c==d:"+(c==d)); // false
    }
```


## java常用容器
### ArrayList,LinkedList,Vector
1.ArrayList是实现了基于动态数组的数据结构，LinkedList基于链表的数据结构。
2.对于随机访问get和set，ArrayList优于LinkedList，因为LinkedList要移动指针。
3.对于新增和删除操作add和remove，LinedList比较占优势，因为ArrayList要移动数据
Vector是很老的数据结构，与ArrayList类似，是线程安全的
ArrayList和LinkedList都是线程不安全的

#### ArrayList的几种遍历方式区别
- 根据下标遍历，普通for循环方式
```java
for(int i = 0; i < list.size(); i++){
  list.get(i)
}
```
- for each方式,使用冒号
```
for(String s:list)
```
这种方式底层是通过迭代器来实现的
- 迭代器方式
```java
Iterator it = list.iterator();
while(it.hasNext()){
  it.next()
}
```
耗时上: 普通for循环< 迭代器 < foreach方式

### CopyOnWriteArrayList
由名字可以看出这个类是一个在写的时候做了将原数组复制一份出来，然后修改完成之后，在设置回去；
通过源码分析得知，该类是一个线程安全的类，设计思想采用了读写分离的操作，可以并发读；在写的时候 采用了
Synchonized(lock) 对象锁来保证并发写
```java
    public void add(int index, E element) {
        synchronized (lock) {
            Object[] elements = getArray();
            int len = elements.length;
            if (index > len || index < 0)
                throw new IndexOutOfBoundsException(outOfBounds(index, len));
            Object[] newElements;
            int numMoved = len - index;
            if (numMoved == 0)
                newElements = Arrays.copyOf(elements, len + 1);
            else {
                newElements = new Object[len + 1];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index, newElements, index + 1,
                                 numMoved);
            }
            newElements[index] = element;
            setArray(newElements);
        }
    }
```
CopyOnWriteArrayList使用写时拷贝的策略来保证list的一致性，而获取-拷贝-写入 三步并不是原子性的，所以在修改增删改的过程中都是用了独占锁，并保证了同时只有一个线程才能对list数组进行修改
#### 缺点:
- 内存占用问题，写时拷贝导致了2倍内存
- 数据一致性问题: CopyOnWrite容器只能保证数据的最终一致性，不能保证数据的实时一致性。所以如果你希望写入的的数据，马上能读到，请不要使用CopyOnWrite容器。

### hashmap相关

#### hashmap实现原理
- 底层数据结构
jdk1.7 及以前，HashMap 由数组+链表组成，数组 Entry 是 HashMap 的主体，Entry 是 HashMap 中的一个静态内部类，每一个 Entry 包含一个 key-value 键值对，
链表是为解决哈希冲突而存 在。
从 jdk1.8 起，HashMap 是由数组+链表/红黑树组成，当某个 bucket 位置的链表长度达到阀 值 8 时，这个链表就转变成红黑树，优化了hash冲突过多时，链表太长照成的查找慢的问题
红黑树是近似平衡树
- 线程不安全
存储比较快，能接受null值
- 为什么要使用加载因子，为什么要进行扩容
加载因子是指当 HashMap 中存储的元素/最大空间值的阀值，如果超过这个值，就会进行扩容。默认值是0.75
加载因子是为了让空间得到充分利用，如果加载因子太大，虽对空间利用更充分，但查 找效率会降低;
如果加载因子太小，表中的数据过于稀疏，很多空间还没用就开始扩容，就 会对空间造成浪费。
至于为什么要扩容，如果不扩容，hash冲突就会越来越多，HashMap中entry数组某个index的链表会越来越长，这样查找效率就会大大降低。

- hashmap如何扩容
根据加载因子判断的，默认0.75，当加载因子超过0.75，则会大小扩充为原来2倍

- hashmap中的数组长度一定是2的幂次
目的是为了当hashcode值转化到数组的index时，能尽量的均匀，如何做呢？
index = (n-1)& hashcode
而n=2的幂次的时候，比如16，n-1之后对应的二进制数据0111(最高位为0，其他位都为1)，这样做与运算的时候，能尽量真实的获取hashcode值的低位，使其均匀
如果不是2的幂次，比如15，n-1之后对应二进制数据1110,那么无论什么hashcode值，与上n-1之后最后一位都是0，这样table中最后一位为1的index
0001，0011，0101，1001，1011，0111，1101这几个位置永远都不能存放元素，空间浪费很大，并且增加了hash碰撞的几率，降低查询效率

### hashmap与hashTable，HashSet
1、HashMap是线程不安全的，在多线程环境下会容易产生死循环，但是单线程环境下运行效率高；Hashtable线程安全的，很多方法都有synchronized修饰，但同时因为加锁导致单线程环境下效率较低。
2、HashMap允许有一个key为null，允许多个value为null；而Hashtable不允许key或者value为null。
3 jdk1.8之后，hashmap使用了数组+链表+红黑树数据结构来实现的,而hashTable还是使用数组+链表的形式

hashSet底层实现set接口，仅存储对象，使用对象来计算hashcode值
hashmap实现map接口，存储键值对，使用key来计算hashcode

### [ConcurrentHashmap](https://www.cnblogs.com/hello-shf/p/12183263.html)
JDK8中ConcurrentHashMap的内部结构与hashmap是一样的，采用 Node 数组+链表+红黑树数据结构来实现的;
- put方法:
使用两个懒加载的方式去初始化 table，采用自旋的方式，判断当前数据是否发生hash冲突，没有冲突就采用 cas(保证一定能设置成功) 的方式 来设置对应的数据, 发生冲突了，用 synchronized 只对具体的某一个 桶(链表头或者树节点头) 进行加锁，减少了锁的粒度，从而增加了并发



### linkedHashmap
LinkedHashMap继承于HashMap，只是hashMap是无序的，LinkedHashMap内部用一个双向链表来维护entry，其定义了
```java
// transient表示在序列话的时候，这个成员变量不被序列化
transient LinkedEntry<K, V> header;
// LinkedEntry 继承于HashMapEntry
static class LinkedEntry<K, V> extends HashMapEntry<K, V> {
    LinkedEntry<K, V> nxt;
    LinkedEntry<K, V> prv;

    /** Create the header entry */
    LinkedEntry() {
        super(null, null, 0, null);
        nxt = prv = this;
    }

    /** Create a normal entry */
    LinkedEntry(K key, V value, int hash, HashMapEntry<K, V> next,
                LinkedEntry<K, V> nxt, LinkedEntry<K, V> prv) {
        super(key, value, hash, next);
        this.nxt = nxt;
        this.prv = prv;
    }
}
```
- 重排
linkedHashMap在get和put的时候，如果accessOrder==true，即按访问顺序的方式排序，则需要对双向链表中
的entry进行重排序，具体就是删除entry在链表中的位置，然后重新添加到链表的尾部

#### LruCache实现原理
android的LruCache实现就是基于LinkedHashMap，LruCache是线程安全的，内部用了`synchronize`来修饰get，put等方法
每次调 用 get(), 则将该对象移到链表的尾端。 调用put插入新的对象也是存储在链表尾端，这样当内存缓存达到设定的最大值 时，将链表头部的对象(近期最少用到的)移除。

### SparseArray, ArrayMap
##### SparseArray
内部用两个数组来存储key，value，存储空间比hashMap少,使用基本类型，避免自动装箱
在get和put的时候内部都用了二分查找算法，其存储的元素都是按照从小到大排列好的
- 扩容:
在插入数据的时候进行扩容,大小变为原来的2倍
- 删除
删除的时候只是做了标记，把对应的删除项置为(DELETED= new Object()),这样可以做到删除的时候不对数据进行移动，节约拷贝数据时间，而且在有数据插入到同样位置的时候，可以直接复用，也节省了拷贝数据的时间

- [关于什么时候该用SparseArray替代HashMap](https://greenspector.com/en/android-should-you-use-hashmap-or-sparsearray/)
结论:如果使用HashMap存储的key是Integer或者Long，则建议用SparseArray来代替，网上还有说存储的item小于1000的时候用SparseArray的性能会好于HashMap



### ArrayMap
ArrayMap是一个<key,value>映射的数据结构，它设计上更多的是考虑内存的优化，内部是使用两个数组进行数据存储，一个数组记录key的hash值，另外一个数组记录Value值，它和SparseArray一样，也会对key使用二分法进行从小到大排序，在添加、删除、查找数据的时候都是先使用二分查找法得到相应的index，然后通过index来进行添加、查找、删除等操作，所以，应用场景和SparseArray的一样，如果在数据量比较大的情况下，那么它的性能将退化至少50%
- 如果key的类型已经确定为int类型，那么使用SparseArray，因为它避免了自动装箱的过程，如果key为long类型，它还提供了一个LongSparseArray来确保key为long类型时的使用
- 如果key类型为其它的类型，则使用ArrayMap


## java泛型
泛型有个特点 类型擦除

### 泛型的协变，逆变
- <? extend T>
限定了泛型的上限，即传入的类型是 T或者T的子类
- <? super T>
限定了泛型的下限，即传入的类型是 T或者 T的父类

- 关系
<? extends T> 继承自 T 继承自 <? super T>

- 使用场景
比如系统的源码 ArrayList<E>
```java
// 类定义
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
...

    // 使用了<? extends E> 修饰
    public ArrayList(Collection<? extends E> c) {
        ...
    }


    //
    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

}
```



## java反射机制
[原文](https://zhuanlan.zhihu.com/p/86293659)
反射让开发人员可以通过外部类的全路径名创建对象，并使用这些类，实现一些扩展的功能。
反射让开发人员可以枚举出类的全部成员，包括构造函数、属性、方法。以帮助开发者写出正确的代码。
测试时可以利用反射 API 访问类的私有成员，以保证测试代码覆盖率
### api
#### 获取类 Class
获取 Class 对象
```
// 1.通过字符串获取Class对象，这个字符串必须带上完整路径名
Class studentClass = Class.forName("com.test.reflection.Student");
```
#### 获取构造函数 Constructor
```
// 1.获取所有声明的构造方法
Constructor[] declaredConstructorList = studentClass.getDeclaredConstructors();
for (Constructor declaredConstructor : declaredConstructorList) {
    System.out.println("declared Constructor: " + declaredConstructor);
}
// 2.获取所有公有的构造方法
Constructor[] constructorList = studentClass.getConstructors();
for (Constructor constructor : constructorList) {
    System.out.println("constructor: " + constructor);
}
```
#### 获取普通成员函数 Method
```
// 1.获取所有声明的函数
Method[] declaredMethodList = studentClass.getDeclaredMethods();
for (Method declaredMethod : declaredMethodList) {
    System.out.println("declared Method: " + declaredMethod);
}
// 2.获取所有公有的函数
Method[] methodList = studentClass.getMethods();
for (Method method : methodList) {
    System.out.println("method: " + method);
}
```
#### 获取成员变量 Field
```
// 1.获取所有声明的字段
Field[] declaredFieldList = studentClass.getDeclaredFields();
for (Field declaredField : declaredFieldList) {
    System.out.println("declared Field: " + declaredField);
}
// 2.获取所有公有的字段
Field[] fieldList = studentClass.getFields();
for (Field field : fieldList) {
    System.out.println("field: " + field);
}
```

#### demo
```java
package com.test.reflection;

public class Student {

    private String studentName;
    public int studentAge;

    public Student() {
    }

    private Student(String studentName) {
        this.studentName = studentName;
    }

    public void setStudentAge(int studentAge) {
        this.studentAge = studentAge;
    }

    private String show(String message) {
        System.out.println("show: " + studentName + "," + studentAge + "," + message);
        return "testReturnValue";
    }
}
```
```java
// 1.通过字符串获取Class对象，这个字符串必须带上完整路径名
Class studentClass = Class.forName("com.test.reflection.Student");
// 2.获取声明的构造方法，传入所需参数的类名，如果有多个参数，用','连接即可
Constructor studentConstructor = studentClass.getDeclaredConstructor(String.class);
// 如果是私有的构造方法，需要调用下面这一行代码使其可使用，公有的构造方法则不需要下面这一行代码
studentConstructor.setAccessible(true);
// 使用构造方法的newInstance方法创建对象，传入构造方法所需参数，如果有多个参数，用','连接即可
Object student = studentConstructor.newInstance("NameA");
// 3.获取声明的字段，传入字段名
Field studentAgeField = studentClass.getDeclaredField("studentAge");
// 如果是私有的字段，需要调用下面这一行代码使其可使用，公有的字段则不需要下面这一行代码
// studentAgeField.setAccessible(true);
// 使用字段的set方法设置字段值，传入此对象以及参数值
studentAgeField.set(student,10);
// 4.获取声明的函数，传入所需参数的类名，如果有多个参数，用','连接即可
Method studentShowMethod = studentClass.getDeclaredMethod("show",String.class);
// 如果是私有的函数，需要调用下面这一行代码使其可使用，公有的函数则不需要下面这一行代码
studentShowMethod.setAccessible(true);
// 使用函数的invoke方法调用此函数，传入此对象以及函数所需参数，如果有多个参数，用','连接即可。函数会返回一个Object对象，使用强制类型转换转成实际类型即可
Object result = studentShowMethod.invoke(student,"message");
System.out.println("result: " + result);

```

## 类加载机制
[参考](https://juejin.cn/post/6844903505937858568)
类加载机制首先得从类的生命周期说起
### 类的生命周期
类从被加载到虚拟机内存中开始，到卸载出内存为止，它的整个生命周期包括：加载（Loading）、验证（Verification）、准备（Preparation）、解析（Resolution）、初始化（Initialization）、使用（Using）和卸载（Unloading）7个阶段。其中验证、准备、解析3个部分统称为连接（Linking）
#### 加载
从java字节码的二进制流中加载数据，最终存到方法区中(当然在此之前还需要对加载的数据进行验证)
#### 验证
这一步的工作就是验证加载的数据是否合理
- 格式验证
  符合java虚拟机规范中规定的class文件格式，class文件的完整性;
- 元数据验证
    - 针对数据类型的验证,检查这个类是否有父类（除了Object之外都应有父类）
    - 本类的父类是否继承了不允许被继承的类（被final修饰 如果本类不是抽象类，
    - 是否实现了父类中的全部虚方法或接口
- 字节码验证
  - 保证任意时刻操作数栈的数据类型与指令代码序列都能配合工作。（如：不能出现这样的状况：操作栈中放了一个int类型的数据，使用却按照long或者引用类型加载）
  - 保证跳转指令不会跳转到方法体以外的字节码指令上
  - 类型转换是有效的（如多态）
- 符号引用的验证
    符号引用是一组字符串，用来描述引用的过程, 为后续解析阶段虚拟机可以将符号引用直接转为 直接引用
#### 准备
经过验证阶段，虚拟机从文件，数据类型，方法逻辑，符号引用等各个方面对类进行了验证，已确保代码的正确性。接下来开始为代码的运行做准备，进入准备阶段
  - 对类变量进行初始化(不是我们程序指定的初始化值)，各类型变量的默认初始化,比如 int默认为0， String默认为null
#### 解析
解析阶段是将常量池中符号引用转化成直接引用的过程。主要针对常量池中的类或接口，字段，类方法，接口方法，方法类型，方法句柄，调用限定符
java虚拟机规范中规定了只有执行了以下字节码指令前才会将所用到的符号引用转化为直接引用：
- anewarray 创建一个引用类型的数组
- checkcast 检查对象是否是给定类型
- getfield putfield 从对象获取某一个字段 设置对象的字段
- getstatic putstatic 从类中获取某一静态变量 设置静态变量
- instanceof 确定对象是否是给定类型
- invokedynamic invokeinterface invokestatic invokevirtual 调用动态方法，接口方法，静态方法，虚方法
- invokespecial 调用实例化方法，私有方法，父类中的方法
- ldc idc_w 把常量池中的项压入栈
- multianewarray 创建多为引用类型性数组
- new 实例化对象
#### 初始化
阶段是为类设置类变量的值和一些其他初始化操作的阶段（如执行static{ }静态代码块）。
#### 使用
程序中正常使用类，比如new一个对象

#### 卸载
由java虚拟机去处理

## 双亲委派机制
双亲委派是类加载器加载class时的一种策略，就是判断该类 是否已经加载，如果没有则不是自身去查找而是委托给父加载器进行查找，这样 依次进行递归，直到委托到最顶层的 Bootstrap ClassLoader,如果 Bootstrap ClassLoader 找到了该 Class,就会直接返回，如果没找到，则继续依次向下查找， 如果还没找到则最后交给自身去查找
### 双亲委托模式的好处
1.避免重复加载，如果已经加载过一次 Class，则不需要再次加载，而是直接读取 已经加载的 Class
2.更加安全，确保，java 核心 api 中定义类型不会被随意替换，比如，采用双亲 委托模式可以使得系统在 Java 虚拟机启动时就加载了 String 类，也就无法用自定 义的 String 类来替换系统的 String 类，这样便可以防止核心 API 库被随意篡改




















