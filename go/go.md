# go介绍

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

