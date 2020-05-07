# go介绍

## go base
[go_base](go_base.md)

## go进阶
积累一套自己的代码工具库，包含以下内容

由于go之前依赖比较繁琐，这里使用go官方的go module来管理模块
go module学习之路


1. 文件操作
2. 数据结构
3. 存储
  数据库存储mysql，缓存redis，lru cache
4. 传输
  grpc，http
5. log日志库
6. 监控
4. go调用C++
5. 分布式框架

## go调试
[go_debug](go_debug.md)

## go测试
[go_test](go_test.md)

## gokit介绍及使用
[go kit介绍及使用](gokit/gokit.md)


## go性能调优


## go相关问题

### go get -u报错
```
 chenxingyi@cxy-mac-pro  ~  go get -u github.com/gocolly/colly/...
package github.com/gocolly/colly
	imports golang.org/x/net/html: golang.org/x/net is a custom import path for https://go.googlesource.com/net, but /Users/chenxingyi/work/go/src/golang.org/x/net is checked out from https://github.com/golang/net.git
package github.com/gocolly/colly
	imports golang.org/x/net/html/charset: golang.org/x/net is a custom import path for https://go.googlesource.com/net, but /Users/chenxingyi/work/go/src/golang.org/x/net is checked out from https://github.com/golang/net.git
package github.com/gocolly/colly
	imports golang.org/x/net/context: golang.org/x/net is a custom import path for https://go.googlesource.com/net, but /Users/chenxingyi/work/go/src/golang.org/x/net is checked out from https://github.com/golang/net.git
```
原因: golang.org/x/net下的路径更换了，变为了的下载路径已经变更为https://go.googlesource.com/net
解决: 删除golang.org/x 下的net目录，再次执行相关命令```go get``即可


## 框架

### web开发框架
beego: 快速开发服务的框架，支持很多功能

### 服务熔断
#### Hystrix
Hystrix是Netflix开源的一款容错框架，主要是为了隔离对远程系统，服务和第三方库的访问，在分布式系统中可以实现健康检查，避免级联故障；
Hystrix原来是java写的，[go的实现版本参考这里](https://github.com/afex/hystrix-go)
包含常用的容错方法：线程池隔离、信号量隔离、熔断、降级回退
[详细介绍](https://www.jianshu.com/p/3e11ac385c73)
- 线程池隔离：
对于分布式的微服务来说，一个请求可能后续依赖很多的其他服务的，当某个底层的服务阻塞或者不可用时，会导致整个请求阻塞不可用，资源被占用了;Hystrix提供了线程池隔离的方式
将一些服务封装成command的方式在单独的线程池中独立运行，使得其他服务不会被某个阻塞的服务影响
- 信号量隔离

- 熔断(Circuit Breaker)
对于一些服务，当不可用时需要有熔断，避免级联的故障，Hystrix提供了一整套机制，帮助我们去实现熔断，一些常用参数
	- timeout:
	超时时间
	- MaxConcurrentRequests:
	最大并发请求数量
	- RequestVolumeThreshold:
	请求阈值，熔断器是否打开首先要满足这个条件；这里的设置表示至少有5个请求才进行ErrorPercentThreshold错误百分比计算
	- SleepWindow:
	半开试探休眠时间，默认值5000ms。当熔断器开启一段时间之后比如5000ms，会尝试放过去一部分流量进行试探，确定依赖服务是否恢复
	- ErrorPercentThreshold:
	错误率

- 降级回退
所谓降级，就是指在在Hystrix执行非核心链路功能失败的情况下，我们如何处理，比如我们返回默认值等


### 服务发现
- Registry
go微服务框架go-micro/Registry

- consul
Consul有多个组件，但是整体来看，它是你基础设施中用于发现和配置服务的一个工具它提供如下几个关键功能：
	- 服务发现： Consul的某些客户端可以提供一个服务，例如api或者mysql，其它客户端可以使用Consul去发现这个服务的提供者。使用DNS或者HTTP，应用可以很容易的找到他们所依赖的服务。
	- 健康检查： Consul客户端可以提供一些健康检查，这些健康检查可以关联到一个指定的服务（服务是否返回200 OK），也可以关联到本地节点（内存使用率是否在90%以下）。这些信息可以被一个操作员用来监	控集群的健康状态，被服务发现组件路由时用来远离不健康的主机。
	- 键值存储： 应用可以使用Consul提供的分层键值存储用于一些目的，包括动态配置、特征标记、协作、leader选举等等。通过一个简单的HTTP API可以很容易的使用这个组件。
	- 多数据中心： Consul对多数据中心有非常好的支持，这意味着Consul用户不必担心由于创建更多抽象层而产生的多个区域。
Consul被设计为对DevOps群体和应用开发者友好，他非常适合现代的、可伸缩的基础设施。


