[TOC]

# LruCache

## 介绍
LruCache采用的缓存算法为LRU(Least Recently Used)最近最少使用算法, 算法核心思想是，当缓存达到预设的上限时，会优先淘汰最近最少使用的元素
基本原理就是内部维护一个map对象用于存储缓存的数据，双向链表用于记录数据的访问顺序；当有数据被访问时，将其移动到链表的头部；当插入数据时，若cache的size已满，则将链表尾部数据删除，将数据添加到链表尾部


## go实现
[见 hraban/lrucache](https://github.com/hraban/lrucache)
- feature:

| |`map[string]interface{}`|	`lrucache`|
|--|--|--|
|thread-safe|	no	|yes|
|maximum size	|no	|yes|
|OnMiss handler|	no	|yes|
  - purges least recently used element when full
  - elements can report their own size
  - everything is cacheable (interface{})
  - is a front for your persistent storage (S3, disk, ...) by using OnMiss hooks



## 使用案例
最近看到[美团一片技术文章讨论关于使用lruCache优化api的执行时间](https://tech.meituan.com/2018/12/20/lrucache-practice-dsp.html)，其主要介绍基于lruCache+redis的使用场景，在此基础上做的优化过程
- 思考: 为什么使用redis了还需要使用lrucache？redis不是自带lruCache机制吗？
- 答: 因为redis一般都需要连接到其他redis服务器，中间有个网络通信的过程，lruCache直接缓存于本机，性能上肯定是本机访问较快；另外redis存储的数据类型有限，本机缓存可以直接缓存对象，节省了数据转换的时间

最终方案: lrucache增加时效性清退机制，采用hash将key分散到多个lruCache，对lrucache的存储的对象进行零拷贝改造，即只存储指针，这样可以节省数据从lrucache拷贝给使用者的时间

- lrucache增加时效性清退机制：
  在业务场景中，Redis中的数据有可能做修改。lrucache本身作为数据的使用方，无法感知到数据源的变化。当缓存的命中率较高或者部分数据在较长时间内多次命中，可能出现数据失效的情况。即数据源发生了变化，但lrucache无法及时更新数据。针对这一业务场景，增加了时效清退机制，
- hash lrucache:
  随着业务的迭代，单机QPS持续上升。在更高QPS下，LruCache的查询耗时有了明显的提高，逐渐无法适应低平响的业务场景。在这种情况下，引入了HashLruCache机制以解决这个问题。
  LruCache在高QPS下的耗时增加原因分析：
    线程安全的LruCache中有锁的存在。每次读写操作之前都有加锁操作，完成读写操作之后还有解锁操作。在低QPS下，锁竞争的耗时基本可以忽略；但是在高QPS下，大量的时间消耗在了等待锁的操作上，导致耗时增长。
  HashLruCache适应高QPS场景：
    针对大量的同步等待操作导致耗时增加的情况，解决方案就是尽量减小临界区。引入Hash机制，对全量数据做分片处理，在原有LruCache的基础上形成HashLruCache，以降低查询耗时。

- lrucache零拷贝:
  在使用lrucache存储时，大部分都是存储对象，往往很多lrucache在读取的时候，会做一次拷贝，将拷贝后的数据返回给用户；零拷贝的引入是将指针返回给用户， 这样会有个问题，就是数据在外面被改动之后，会影响缓存中的数据









