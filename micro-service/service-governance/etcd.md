[toc]

# [etcd](https://etcd.io/)
开源的go编写的分布式系统最关键数据的分布式可靠键值存储数据库,它可以优雅地处理网络分区期间的领导者选举，即使在领导者节点中也可以容忍机器故障
[参考](https://juejin.im/post/5e02fb1f518825123b1aa341#heading-35)

## 特点
- 简单
提供grpc api
- 安全
支持SSL证书验证
- 快速
基准测试写入速度 10,000次/秒
- 可靠
使用raft协议


## etcd vs redis
[参照](https://blog.csdn.net/weixin_41571449/article/details/79429511)
etcd的红火来源于kurbernetes用etcd做服务发现，而redis的兴起则来源于memcache缓存本身的局限性。
etcd是一种分布式存储，更强调的是各个节点之间的通信，同步，确保各个节点上数据和事务的一致性，使得服务发现工作更稳定，本身单节点的写入能力并不强。
redis更像是内存型缓存，虽然也有cluster做主从同步和读写分离，但节点间的一致性主要强调的是数据，并不在乎事务，因此读写能力很强，qps甚至可以达到10万+

两者都是k-v存储，但redis支持更多的存储模式，包括KEY，STRING，HMAP，SET，SORTEDSET等等，因此redis本身就可以完成一些比如排序的简单逻辑。而etcd则支持对key的版本记录和txn操作和client对key的watch，因此适合用做服务发现

日常使用中，etcd主要还是做一些事务管理类的，基础架构服务用的比较多，容器类的服务部署是其主流。而redis广泛地使用在缓存服务器方面，用作mysql的缓存，通常依据请求量，甚至会做成多级缓存，当然部分情况下也用做存储型redis做持续化存储

