[TOC]

# android主线程是死循环，为什么不会影响程序运行？
[参考这里](https://www.zhihu.com/question/34652589?spm=a2c4e.10696291.0.0.2d9f19a4zaM5ya)
这里涉及到android消息循环机制，其实ActivityThread.main()中，首先会创建Looper对象和Handler对象用于处理主线程的消息，
然后会创建 ApplicationThread，这个是一个Binder的服务端继承了IApplicationThread.Stub，提供服务给system_server进程中的ApplicationThreadProxy(ATP)调用
之后进入looper.loop()，而loop()中会循环从MsgQueue中取数据，这个操作是阻塞的(这里底层是从pipe中去读，没有消息便会阻塞，让出CPU执行权)；

- 启动流程图


### activity的启动过程
![App运行过程](images/android_activity_startup.jpeg)

### Service的启动过程
![App运行过程](images/android_service_startup.png)

## 主线程死循环会不会一直消耗cpu
不会，主线程中虽然是死循环，但是其实是阻塞去取msgQueue中数据，大多数情况下是休眠状态，不会消耗大量cpu

## 主线程阻塞了，如何响应其他事件
上面说到在一开始，会创建 ApplicationThread，这个是ActivityThread的内部类，其通过handler想主线程发送消息；
而且他实现了Binder的服务端，远程调用的服务端运行的时候，是运行在一个独立的线程中的，所以需要通过handler想主线程发送消息

## handler同步屏障
handler的同步屏障机制，是保证view能够得到及时的绘制从而保证界面不会卡顿，因为view的绘制也是通过handler来进行的，如果
handler中messageQueue的消息太多，view绘制的消息得不到及时处理是不行的，

同步屏障原理是因为在 messageQueue 的next()取消息时，如果遇到message.target为null的消息，会不断的跳过下一个同步消息，直到遇到第一个异步消息，这就是同步屏障，同步屏障的设置通过 messageQueue 的 postSyncBarrier 来设置，view的绘制消息都是异步消息，平常我们设置向handler发送的消息都是 同步消息

# Handler机制
looper
messageQueue


## 考点
- send message delay问题
有一个handler,然后调用
sendMessageDelay(MSG1, 10s);
sendMessageDelay(MSG2, 5s);
紧接着调用sleep(15s);
问题是，过了多久才能收到MSG1和MSG2的消息
经过测试: 答案是15s 后，先收到MSG2，后收到MSG1; 测试代码如下
解析: 因为sendMessage和sendMessageDelay函数都是运行在当前调用的线程中的，当前线程sleep了，那handler也会把sleep的时间计算在message delay的时间中
15s已经>10s>5s 所以时间到期，就直接处理了msg2，和msg1, msg在入队列的时候有顺序的

## 子线程更新UI
通过handler去更新
RunOnUiThread
view.Post(Runnable)
AsyncTask
这些操作UI的底层实现都是通过Handler

### handler更新UI的原理
默认无参构造Handler时，最终会调用 Looper.myLooper()获取looper，而这里面是通过ThreadLocal变量来获取Looper的，
threadlocal修饰的变量是线程独有的，这个looper在主线程中(prepareMainLooper)是已经创建好的,所以在主线程中创建handler的时候，默认是有Looper的，其他线程创建就handler需要自己创建looper

## 子线程一定不能更新UI吗？
不一定，android判断线程是否能更新UI是在 ViewRootImpl中去判断的，而ViewRootImpl的创建是在Activity的OnResume中创建的
这样当在OnCreate中创建子线程去执行UI更新的时候， ViewRootImpl还没有被创建


## IdleHandler
[相关代码分析](https://juejin.cn/post/6844904068129751047)
IdleHandler 是 Handler 提供的一种在消息队列空闲时，执行任务的时机。但它执行的时机依赖消息队列的情况，那么如果 MessageQueue 一直有待执行的消息时，IdleHandler 就一直得不到执行，也就是它的执行时机是不可控的，不适合执行一些对时机要求比较高的任务。
IdleHandler 被定义在 MessageQueue 中，它是一个接口
```java
// MessageQueue.java
public static interface IdleHandler {
  boolean queueIdle();
}
```
返回值为 true 表示是一个持久的 IdleHandler 会重复使用，返回 false 表示是一个一次性的 IdleHandler

- 使用
```java
    // 添加 IdleHandler
    Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
        @Override
        public boolean queueIdle() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("Test","IdleHandler queueIdle");
            return false;
        }
    });
```

- 什么时候出现空闲？
1.MessageQueue 为空，没有消息；
2.MessageQueue 中最近需要处理的消息，是一个延迟消息（when>currentTime），需要滞后执行；

### 相关问题
- Q：IdleHandler 有什么用？
IdleHandler 是 Handler 提供的一种在消息队列空闲时，执行任务的时机；
当 MessageQueue 当前没有立即需要处理的消息时，会执行 IdleHandler；

- Q：MessageQueue 提供了 add/remove IdleHandler 的方法，是否需要成对使用？
不是必须；
IdleHandler.queueIdle() 的返回值，可以移除加入 MessageQueue 的 IdleHandler；

- Q：当 mIdleHanders 一直不为空时，为什么不会进入死循环？
只有在 pendingIdleHandlerCount 为 -1 时，才会尝试执行 mIdleHander；
pendingIdlehanderCount 在 next() 中初始时为 -1，执行一遍后被置为 0，所以不会重复执行；

- Q：是否可以将一些不重要的启动服务，搬移到 IdleHandler 中去处理？
不建议；
IdleHandler 的处理时机不可控，如果 MessageQueue 一直有待处理的消息，那么 IdleHander 的执行时机会很靠后；

- Q：IdleHandler 的 queueIdle() 运行在那个线程？
陷进问题，queueIdle() 运行的线程，只和当前 MessageQueue 的 Looper 所在的线程有关；
子线程一样可以构造 Looper，并添加 IdleHandler；

- Q:主线程的IdleHandler 如果进行耗时操作会怎样？
1.Thread.sleep(n) n > 10 页面会卡死，但不会崩溃，如果页面有动图，则动图变为静态图（视频未尝试，猜测也会处于静止状态） 但此时如果点击页面按钮，则会无响应进入anr，如果不点击，n秒过后恢复正常。
2.网络请求： 不会崩溃，但会报错 IdleHandler threw exception android.os.NetworkOnMainThreadException
3.文件写入本地： 成功。测试所用文件为小文件，大文件猜测也一样，同sleep，中途不点击页面无事，点击anr。

- Q:onCreate中Mainlooper添加idle后，先执行哪个？
先执行oncreate其他代码，然后执行idleHandler


# AIDL VS Binder
## [Binder](https://juejin.cn/post/6844904115777044488)
在 Android 系统的 Binder 是由 Client,Service,ServiceManager,Binder 驱动程序组成的， 其中 Client，service，Service Manager 运行在用户空间，Binder 驱动程序是运行在内核空间 的。而 Binder 就是把这 4 种组件粘合在一块的粘合剂，其中核心的组件就是 Binder 驱动程 序，Service Manager 提供辅助管理的功能，而 Client 和 Service 正是在 Binder 驱动程序和 Service Manager 提供的基础设施上实现 C/S 之间的通信。其中 Binder 驱动程序提供设备文 件/dev/binder 与用户控件进行交互， Client、Service，Service Manager 通过 open 和 ioctl 文件操作相应的方法与 Binder 驱动程序 进行通信。而Client和Service之间的进程间通信是通过Binder驱动程序间接实现的。而Service Manager 是一个守护进程，用来管理 Service，并向 Client 提供查询 Service 接口的能力。

AIDL是为了方便Binder的开发提出的框架
Binder是C/S架构，多个client可以调用同一个server,调用远程服务时是同步的，

### 关于远程调用线程的问题
- server端:
  - service的创建在主线程;
  - service的函数被调用的时候，运行在server进程中的一个新线程
  - server端调用callback回调client端，这个也是同步调用的，server也会阻塞知道client端callback执行完成

- client端:
  - client端调用远程函数时，调用者会阻塞等待远程调用执行完成，
  所以如果在主线程调用远程service的函数需要注意有可能导致ANR
  - client调用的时候有callback的情况
  远端server调用callback回调给client端时，这个callback函数运行在client端调用remote函数的线程中，
  即如果client在主线程调用remote，那么callback就运行在主线程
[相关代码远程调用代码](https://github.com/cxy200927099/Note/tree/master/android/aidl)



## android 签名V1,V2,V3的区别
[详细参考这里](https://blog.csdn.net/freekiteyu/java/article/details/97884257)
v1 方案：基于 JAR 签名。
v2 方案：APK 签名方案 v2，在 Android 7.0 引入。
v3 方案：APK 签名方案 v3，在 Android 9.0 引入。
v1 到 v2 是颠覆性的，为了解决 JAR 签名方案的安全性问题，而到了 v3 方案，其实结构上并没有太大的调整，可以理解为 v2 签名方案的升级版，有一些资料也把它称之为 v2+ 方案。
因为这种签名方案的升级，就是向下兼容的，所以只要使用得当，这个过程对开发者是透明的


## android序列化
### Parcelable和Serializable
#### Serializable
java提供的序列化方式，是一个空接口，对象继承这个接口即可表明当前对象是可序列化的，一般用于持久化到磁盘
序列化的实现是由ObjectOutputStream.writeObject(obj)进行序列化，以及ObjectInputStream.readObject(obj)进行反序列化的
- serialVersionUID
```
private static final long serialVersionUID= 1L;
```
在序列化的时候，最好为当前类指定一个固定的serialVersionUID，如果不指定，系统也可以序列化，但是序列化的时候会默认生成一个 serialVersionUID，并将这个值写入存储介质，当类结构发生变化，比如新增一个变量，那反序列化的时候会由于serialVersionUID不一致导致反序列化失败
#### Parcelable
android提供的对象序列化方式，使用上稍微麻烦些，但是都是基于内存的操作，性能上比Serializable好很多
实现对象序列化，继承Parcelable接口
1）重写writeToParcel 将对象数据序列化成一个Parcel对象(序列化之后成为Parcel对象.以便Parcel容器取出数据)
2）重写describeContents方法,默认值为0
3）Public static final Parcelable.Creator<T>CREATOR (将Parcel容器中的数据转换成对象数据) 同时需要实现两个方法:
　　3.1 CreateFromParcel(从Parcel容器中取出数据并进行转换.)
　　3.2 newArray(int size)返回对象数据的大小

## java方面

## 线程和进程
### 概述
- 进程是具有一定独立功能的程序关于某个数据集合上的一次运行活动,进程是系统进行资源分配和调度的一个独立单位.
- 线程是进程的一个实体,是CPU调度和分派的基本单位,它是比进程更小的能独立运行的基本单位.线程自己基本上不拥有系统资源,只拥有一点在运行中必不可少的资源(如程序计数器,一组寄存器和栈),但是它可与同属一个进程的其他的线程共享进程所拥有的全部资源.
- 一个线程可以创建和撤销另一个线程;同一个进程中的多个线程之间可以并发执行.
- 相对进程而言，线程是一个更加接近于执行体的概念，它可以与同进程中的其他线程共享数据，但拥有自己的栈空间，拥有独立的执行序列。
- 在串行程序基础上引入线程和进程是为了提高程序的并发度，从而提高程序运行效率和响应时间
### 区别
1) 简而言之,一个程序至少有一个进程,一个进程至少有一个线程.
2) 线程的划分尺度小于进程，使得多线程程序的并发性高。
3) 另外，进程在执行过程中拥有独立的内存单元，而多个线程共享内存，从而极大地提高了程序的运行效率。
4) 线程在执行过程中与进程还是有区别的。每个独立的线程有一个程序运行的入口、顺序执行序列和程序的出口。但是线程不能够独立执行，必须依存在应用程序中，由应用程序提供多个线程执行控制。
5) 从逻辑角度来看，多线程的意义在于一个应用程序中，有多个执行部分可以同时执行。但操作系统并没有将多个线程看做多个独立的应用，来实现进程的调度和管理以及资源分配。这就是进程和线程的重要区别


## ThreadLocal
ThreadLocal是android提供的可以为每个线程创建私有变量的类，其原理还因为每个thread都有一个ThreadLocalMap对象来管理，当给threadLocal修饰的对象赋值时，首先是获取当前的线程，然后得到线程私有的threadLocalMap对象，在把threadLocal对象作为key，设置的值value一起存入threadLocalMap

## android异步相关

#### HandlerThread
HandlerThread继承了Thread，然后会创建handler并且创建自己的looper用于处理消息

#### IntentService
继承了Service，一般Service都是运行在主线程，IntentService会创建handlerThread一一处理Intent，具体就是在 OnStartCommand和OnStart 中将 Intent 通过handler发送给handlerThread,处理完成这个intent之后，IntentService会自动 调用 StopSelf来结束当前service

#### AsyncTask
##### 原理
AsyncTask 中有两个线程池（SerialExecutor 和THREAD_POOL_EXECUTOR）和一个 Handler（InternalHandler），其中线程池 SerialExecutor 用于任务的排队，而线程池 THREAD_POOL_EXECUTOR 用于真正地执行任务，InternalHandler 用于将执行环境从线池切换到主线程。
- sHandler 是一个静态的 Handler 对象，为了能够将执行环境切换到主线程，这就要求 sHandler 这个对象必须在主线程创建。由于静态成员会在加载类的时候进行初始化，因此这就变相要求 AsyncTask 的类必须在主线程中加载，否则同一个进程中的 AsyncTask 都将无法正常工作。

## 线程池
ThreadPoolExecutor 主要功能就是复用线程，减少线程的创建和销毁,线程池的工作原理，当一个任务提交到线程池时；
1. 判断核心线程池中线程是否已满，如果没满，则创建一个核心线 程执行任务，否则进入下一步
2. 判断工作队列是否已满，没有满则加入工作队列，否则执行下一步
3. 判断线程数是否达到了最大值，如果没有则创建非核心线程执行任务，否则执行饱和策略，默认抛出异常

### 线程池的参数
- corePoolSize 核心线程数。当线程数小于该值时，线程池会优先创建新线程来执行新任务
- maximumPoolSize 线程池所能维护的最大线程数
- keepAliveTime 空闲线程的存活时间
- workQueue 任务队列，用于缓存未执行的任务
- threadFactory 线程工厂。可通过工厂为新建的线程设置更有意义的名字
- handler 拒绝策略。当线程池和任务队列均处于饱和状态时，使用拒绝策略处理新任务。默认是 AbortPolicy， 即直接抛出异常

### android中常用线程池
1. FixedThreadPool:
  可重用固定线程数的线程池，只有核心线程，没有非核心线程， 核心线程不会被回收，有任务时，有空闲的核心线程就用核心线程执行，没有则 加入队列排队
2. SingleThreadExecutor:
  单线程线程池，只有一个核心线程，没有非核心线程，当 任务到达时，如果没有运行线程，则创建一个线程执行，如果正在运行则加入队列等待，可以保证所有任务在一个线程中按照顺序执行，和 FixedThreadPool 的 区别只有数量
3. CachedThreadPool:
  按需创建的线程池，没有核心线程，非核心线程有 Integer.MAX_VALUE 个，每次提交 任务如果有空闲线程则由空闲线程执行，没有空闲线程则创建新的线程执行，适 用于大量的需要立即处理的并且耗时较短的任务
4. ScheduledThreadPoolExecutor:
  继承自 ThreadPoolExecutor,用于延时执行任务或 定期执行任务，核心线程数固定，线程总数为 Integer.MAX_VALUE

## 并发

### Volatile
解决变量在多线程之间的可见性问题，不能保证原子性，可以限制指令重排；
- 可见性:
强制读写内存，读写都是直接读写内存而不是缓存
- 指令重排:
为了提高执行效率，编译器在编译过程中会对指令进行重排，
而volatile修饰的变量可以限制重排，起到一个屏障的作用，如何限制:
  - 在 volatile 变量的写入指令之前，对其它变量的读写指令不能重排到该指令之后
  - 在 volatile 变量的读取指令之后，对其它变量的读写指令不能重排到该指令之前



### 悲观锁
顾明思议悲观，每次去取数据的时候都认为别人会修改，所以都会上锁，然后别人拿数据的时候就会阻塞直到拿到锁
### 乐观锁
每次去拿数据的时候都认为别人不会修改，所以不会上锁。但是在更新的时候会判断一下，在此期间是否有人去更新这个数据，利用版本号等机制来控制。乐观锁适用于多读的应用类型，这样可以提高吞吐量
### 重入锁
也叫递归锁，指同一线程 在获得外层的锁之后，在锁之内还可以获取该锁，java中synchronized和ReentrantLock都是重入锁
### 读写锁
读写锁包含两个锁，读锁和写锁；读数据的时候，加上读锁，多个线程可以并发读，写数据的时候加上写锁，只允许一个线程在同时写
读-读能共存，读-写不能共存，写-写不能共存
### CAS(compare and swap)无锁机制
包含三个参数，V(要更新的变量)，E(预期值)，N(新值)
仅当V值等于E值时，才会将V的值设为N，
如果V值和E值不同，则说明已经有其他线程做了更新，则当前线程什么都不做。
最后，CAS返回当前V的真实值
- 特点
性能比较好，不会死锁
### 自旋锁
当一个线程在获取锁的时候，如果锁已经被其他线程获取，那该线程将循环等待，不停尝试获取锁，直到成功为止，
自旋锁lock用到了CAS，第一个线程进入的时候能够成功获取锁，不会进入while循环，如果此时A没有释放锁，
另一个线程又来获取锁，不满足cas会进入while循环，然后不断判断是否满足cas，直到线程A释放锁，线程B才能退出循环

### Synchronized vs Lock
#### Synchronized
Synchronized是java的关键字
修饰一个类，其作用的范围是synchronized后面括号括起来的部分，作用的对象是这个类的所有对象。
修饰一个方法，被修饰的方法称为同步方法，其作用的范围是整个方法，作用的对象是调用这个方法的对象；
修改一个静态的方法，其作用的范围是整个静态方法，作用的对象是这个类的所有对象；
修饰一个代码块，被修饰的代码块称为同步语句块，其作用的范围是大括号{}括起来的代码，作用的对象是调用这个代码块的对象；

在定义接口方法时不能使用synchronized关键字。
构造方法不能使用synchronized关键字，但可以使用synchronized代码块来进行同步。
synchronized 关键字不能被继承。如果子类覆盖了父类的 被 synchronized 关键字修饰的方法，那么子类的该方法只要没有 synchronized 关键字，那么就默认没有同步，也就是说，不能继承父类的 synchronized

- 原理:
synchronized经过编译之后，会在同步块的前后分别形成monitorenter和monitorexit这个两个字节码指令。在执行monitorenter指令时，首先要尝试获取对象锁。如果这个对象没被锁定，或者当前线程已经拥有了那个对象锁，把锁的计算器加1，相应的，在执行monitorexit指令时会将锁计算器就减1，当计算器为0时，锁就被释放了。如果获取对象锁失败，那当前线程就要阻塞，直到对象锁被另一个线程释放为止。
主要是为了避免进入内核态线程阻塞

##### [Synchronized的几种锁状态](https://blog.csdn.net/tongdanping/article/details/79647337?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.control&dist_request_id=&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.control)
- 无锁状态

- 偏向锁
大多数时候是不存在锁竞争的，常常是一个线程多次获得同一个锁，因此如果每次都要竞争锁会增大很多没有必要付出的代价，为了降低获取锁的代价，才引入的偏向锁
  当线程1访问代码块并获取锁对象时，会在java对象头和栈帧中记录偏向的锁的threadID，因为偏向锁不会主动释放锁，因此以后线程1再次获取锁的时候，需要比较当前线程的threadID和Java对象头中的threadID是否一致，如果一致（还是线程1获取锁对象），则无需使用CAS来加锁、解锁；如果不一致（其他线程，如线程2要竞争锁对象，而偏向锁不会主动释放因此还是存储的线程1的threadID），那么需要查看Java对象头中记录的线程1是否存活，如果没有存活，那么锁对象被重置为无锁状态，其它线程（线程2）可以竞争将其设置为偏向锁；如果存活，那么立刻查找该线程（线程1）的栈帧信息，如果还是需要继续持有这个锁对象，那么暂停当前线程1，撤销偏向锁，升级为轻量级锁，如果线程1 不再使用该锁对象，那么将锁对象状态设为无锁状态，重新偏向新的线程
- 轻量锁
采用cas来实现，轻量级锁考虑的是竞争锁对象的线程不多，而且线程持有锁的时间也不长的情景。因为阻塞线程需要CPU从用户态转到内核态，代价较大，如果刚刚阻塞不久这个锁就被释放了，那这个代价就有点得不偿失了，因此这个时候就干脆不阻塞这个线程，让它自旋这等待锁释放。
- 重量锁
会使得线程阻塞，CPU要从用户态转到内核态，
自旋的时间太长也不行，因为自旋是要消耗CPU的，因此自旋的次数是有限制的，比如10次或者100次，如果自旋次数到了线程1还没有释放锁，或者线程1还在执行，线程2还在自旋等待，这时又有一个线程3过来竞争这个锁对象，那么这个时候轻量级锁就会膨胀为重量级锁。重量级锁把除了拥有锁的线程都阻塞，防止CPU空转

*一句话就是锁可以升级不可以降级，但是偏向锁状态可以被重置为无锁状态。*

###### 锁粗化
按理来说，同步块的作用范围应该尽可能小，仅在共享数据的实际作用域中才进行同步，这样做的目的是为了使需要同步的操作数量尽可能缩小，缩短阻塞时间，如果存在锁竞争，那么等待锁的线程也能尽快拿到锁。 
但是加锁解锁也需要消耗资源，如果存在一系列的连续加锁解锁操作，可能会导致不必要的性能损耗。 
锁粗化就是将多个连续的加锁、解锁操作连接在一起，扩展成一个范围更大的锁，避免频繁的加锁解锁操作。

###### 锁消除
Java虚拟机在JIT编译时(可以简单理解为当某段代码即将第一次被执行时进行编译，又称即时编译)，通过对运行上下文的扫描，经过逃逸分析，去除不可能存在共享资源竞争的锁，通过这种方式消除没有必要的锁，可以节省毫无意义的请求锁时间


#### Lock,ReentrantLock
- Lock是一个接口，ReentrantLock是实现了这个接口
由于ReentrantLock是java.util.concurrent包下提供的一套互斥锁,其特点如下
- 等待可中断
持有锁的线程长期不释放的时候，正在等待的线程可以选择放弃等待，这相当于Synchronized来说可以避免出现死锁的情况。通过lock.lockInterruptibly()来实现这个机制
- 公平锁
Synchronized是非公平锁，唤醒是随机的，ReentrantLock可以实现公平锁，即先申请的线程优先获取到该锁
- 锁绑定多个对象
一个ReentrantLock对象可以同时绑定对个对象。ReenTrantLock提供了一个Condition（条件）类，用来实现分组唤醒需要唤醒的线程们，而不是像synchronized要么随机唤醒一个线程要么唤醒全部线程

ReentrantLock内部是一种自旋锁，通过循环调用CAS来实现加锁，避免进入内核线程阻塞

#### Atomic
原子操作，java.util.concurrent下的另一个专门为线程安全设计的Java包，包含多个原子操作类
其底层原理也是基于自旋锁+CAS来实现的
CAS相对于其他锁，不会进行内核态操作，有着一些性能的提升。但同时引入自旋，当锁竞争较大的时候，自旋次数会增多。cpu资源会消耗很高。换句话说，CAS+自旋适合使用在低并发有同步数据的应用场景。
- 如何实现线程安全
//使用unsafe的native方法，实现高效的硬件级别CAS



## android类加载器
- PathClassLoader: 主要用于系统和app的类加载器,其中optimizedDirectory为null, 采用默认目录/data/dalvik-cache/
- DexClassLoader: 可以从包含classes.dex的jar或者apk中，加载类的类加载器, 可用于执行动态加载,但必须是app私有可写目录来缓存odex文件. 能够加载系统没有安装的apk或者jar文件， 因此很多插件化方案都是采用DexClassLoader;
- BaseDexClassLoader: 比较基础的类加载器, PathClassLoader 和 DexClassLoader都只是在构造函数上对其简单封装而已.
- BootClassLoader: 作为父类的类构造器。

## 热修复原理
1. 下发补丁（内含修复好的 class）到用户手机，即让 app 从 服务器上下载（网络传输）
2. app 通过**"某种方式"**，使补丁中的 class 被 app 调用（本 地更新）
这里某种方式就涉及到android的类加载机制，android中对class进行了优化，将多个class合并成一个dex文件，android的在加载dex的类加载器中有个DexPathList,在DexPathList.findClass()过程，一个Classloader可以包含多个dex文件，每个dex文件被封装到一个Element对象，这些Element对象排列成有序的数组dexElements。当查找某个类时，会遍历所有的dex文件，如果找到则直接返回，不再继续遍历dexElements。也就是说当两个相同的dex中出现，会优先处理排在前面的dex文件，这便是热修复的核心精髓，将需要修复的类所打包的dex文件插入到dexElements前面


## 导致内存泄漏的原因
根本原因: 长生命周期的对象持有短生命周期的对象，导致短生命周期对象无法及时释放

### cursor游标为关闭
记得及时关闭游标
### 内部类引起的泄漏
比如在Activity中定义的内部类，并启动了一个长时间运行的线程，因为非静态内部类和匿名内部类会持有外部类的引用，这样Activity退出的时候，得不到及时的回收
如Activity中定义了非静态内部类handler，handler的消息队列中还有未处理的消息，或者正在处理消息，activity退出了，也会导致Activity内存泄漏
- 解决
  使用静态内部类+WeakRefernce,即 static修饰内部类，然后内部类构造函数传入acitvity,使用WeakReference来存储Activity
  更新UI的时候，需要对Activity判空处理，有可能Activity已经被回收了
### 静态集合类引起的内存泄漏
### 注册/反注册未成对使用
### Bitmap对象不再使用时，没有调用recycle()释放
### animation对象没有及时调用cancel()取消动画


## android优化
### 布局优化
减少过渡绘制，减少布局嵌套，能用linearLayout和FrameLayout，就不用 RelativeLayout ，RelativeLayout测绘比较耗时
使用include配合merge标签，include可以重用布局，merge减少自己的一层布局
viewStub按需加载，使用到了才去加载布局
复杂的界面使用ConstraintLayout代替RelativeLayout
### 绘制优化
onDraw中不要创建新的局部对象
onDraw中不要做耗时的任务
android屏幕刷新率60HZ，每一帧绘制的时间大约16ms，如果onDraw函数过于繁重，会导致丢帧，卡顿
### 内存优化
避免内存泄漏，内存泄漏最终会触发GC，GC的时候stop-the-world会造成界面卡顿
1. 集合类中对象的释放，比如arrayList，不用的时候及时调用clear(),并将集合对象赋值为null
2. 单例中持有Context(Activity),如果单例中需要用到Context，使用ApplicationContext，因为单例生命周期是和进程一样的
3. 匿名内部类和非静态内部类，这两个类会默认持有外部类的引用，导致外部类(常见的Activity)得不到回收
解决使用静态内部类，及弱引用的方式
4. 资源关闭问题
即使关闭一些对象比如文件流，网络流，广播，一些第三方库的解除注册

使用leakcanary帮助debug时检测内存泄漏

### 启动速度优化
1. 利用提前展示出来的Window，快速展示出来一个界面，给用户快速反馈的体验
2. 在Application中OnCreate中尽量不要做太多初始化工作，可以将一些非必要的组件做成异步的方式加载
3. 数据库及IO操作都移到工作线程，并且设置线程优先级为THREAD_PRIORITY_BACKGROUND，这样工作线程最多能获取到10%的时间片，优先保证主线程执行
4. 冷启动，热启动，温启动
  - 冷启动
  应用程序进程从头开始启动，系统中不存在该进程，这时候一些资源初始化，数据的加载是从头开始的，这时就是上述1，2，3中的优化点
  - 热启动
  启动app时，后台已有app的进程（例：按back键、home键，应用虽然会退出，但是该应用的进程是依然会保留在后台，可进入任务列表查看），所以在已有进程的情况下，这种启动会从已有的进程中来启动应用，这个方式叫热启动，这时候我们可以在onSaveInstanceState的时候保存一些数据，然后Activity再次创建时，从onCreate中bundle获取
  - 温启动
  介于冷启动和热启动之间, 一般来说在以下两种情况下发生
  用户back退出了App, 然后又启动. App进程可能还在运行, 但是activity需要重建。用户退出App后, 系统可能由于内存原因将App杀死, 进程和activity都需要重启, 但是可以在onCreate中将被动杀死锁保存的状态(saved instance state)恢复
### apk大小优化
删除无用的资源，
图片适配以一套为标准比如xxhdp,
png,jpg压缩，
代码压缩，混淆，优化
插件化
###  其他优化
线程优化:使用线程池，优化响应速度，减少频繁创建线程和销毁线程带来的开销，
bitmap的优化，压缩质量，尺寸

### bitmap相关
- 从网络加载一个10M的图片；
首先下载图片，然后解码获取原始图片的大小，可以根据当前显示的view大小设置解码图片的option.insampleSize = 2表示缩小一半，
按需加载图片;对图片进行缓存

- 怎么读取一张100m的图片；
BitmapRegionDecoder只解码图片的部分区域

## 如何保证一个后台服务不被杀死,比较省电的方式是什么
1. onStartCommand方法，返回START_STICKY
进程没kill掉的时候，保证不了
2. 提升service的优先级，在AndroidManifest.xml文件中对于intent-filter可以通过android:priority = “1000”这个属性设置最高优先级，1000是最高值，如果数字越小则优先级越低，同时适用于广播
3. 如果service是进程，提升进程优先级
  1.前台进程( FOREGROUND_APP)
  2.可视进程(VISIBLE_APP )
  3.次要服务进程(SECONDARY_SERVER )
  4.后台进程 (HIDDEN_APP)
  5.内容供应节点(CONTENT_PROVIDER)
  6.空进程(EMPTY_APP)
  将进程设置为前台运行，此时需要配合Notification来使用，但是在onDestroy中记得调用停止前台运行
  内存极度紧张情况下，还是会被kill掉
4. 监听系统广播，拉起服务，比如锁屏
5. 跳转到系统白名单界面让用户自己添加app进入白名单




#### ontouch和onTouchEvent的区别
- ontouch是 view的 OnTouchListener接口的方法
- onTouchEvent是 view的方法
在view的 dispatchTouchEvent函数中，如果view设置了onTouchListener接口，且onTouch返回true表示消费该事件
则 event事件不会分发给 onTouchEvent


binder原理，binder底层原理不是很了解，主要用AIDL比较多

Activity的启动模式
分别介绍一下，
singleTop,singleTask,singleInstance的应用场景，答得不是很好

GC机制

HashMap原理
介绍了下table，链表，tree

Handler机制，主要问handler如何保证消息的顺序执行
通过时间来控制的

用过什么第三方框架，想要问原理
答: 只停留在使用的地方



context的理解


view的事件分发机制
dispatchTouchEvent没答上来

热修复

插件化，没做过

查找链表中的回文字符


















