# go语言中使用Redis
redis client支持市面上大部分语言，go也在其中，但是go的client版本有很多实现
- [Go-Redis](https://github.com/alphazero/Go-Redis) 
  - google开发的client,看最近一次更新的时间是7年前
- [Radix](https://github.com/mediocregopher/radix.v2) 
  - MIT licensed Redis client which supports pipelining, pooling, redis cluster, scripting, pub/sub, scanning, and more，最近更新是在5个月前
- [Redigo](https://github.com/gomodule/redigo) 
  - Redigo is a Go client for the Redis database with support for Print-alike API, Pipelining (including transactions), Pub/Sub, Connection pooling, scripting.社区比较活跃
- [go-redis/redis](https://github.com/go-redis/redis) 
  - Redis client for Golang supporting Redis Sentinel and Redis Cluster out of the box；社区活跃，功能丰富，支持自动连接池(带断路器支持),Pub/Sub, Transactions,Pipeline and TxPipeline,Scripting,Timeouts,Redis Sentinel,Redis Cluster,Cluster of Redis Servers without using cluster mode and Redis Sentinel,Ring,Instrumentation,Cache friendly,Rate limiting,Distributed Locks

这里选择github star比较多的go-redis/redis来作为讨论
go-redis与redigo的性能测试比较

| testCase (benchmark)| requests | ns/op(goredis) |ns/op(redigo) | B/op(goredis) | B/op(redigo) | allocs/op(goredis) | allocs/op(redigo)
|---|---|---|---|---|---|---|---|
|Set10Conns64Bytes-4 |200000 |7621 ns/op |7576 ns/op |210 B/op |208 B/op | 6 allocs/op|7 allocs/op |
|Set100Conns64Bytes-4 |200000 |7554 ns/op |7782 ns/op |210 B/op |208 B/op | 6 allocs/op|7 allocs/op|
|Set10Conns1KB-4 |200000 |7697 ns/op |7958 ns/op |210 B/op |208 B/op |6 allocs/op|7 allocs/op |
|Set100Conns1KB-4 |200000 |7688 ns/op |7725 ns/op |210 B/op |208 B/op | 6 allocs/op|7 allocs/op |
|Set10Conns10KB-4 |200000 |9214 ns/op |18442 ns/op |210 B/op |208 B/op | 6 allocs/op|7 allocs/op|
|Set100Conns10KB-4 |200000 |9181 ns/op |18818 ns/op |210 B/op |208 B/op |6 allocs/op|7 allocs/op |
|Set10Conns1MB-4 |2000 |583242 ns/op | 668829 ns/op |2337 B/op |226 B/op |6 allocs/op|7 allocs/op |
|Set100Conns1MB-4 |2000 |583089 ns/op |679542 ns/op |2338 B/op |226 B/op | 6 allocs/op|7 allocs/op|

## 环境搭建
安装
```go
go get -u github.com/go-redis/redis
```
导入包
```go
import "github.com/go-redis/redis"
```
代码示例：
```go
package main

import (
	"fmt"
	"github.com/go-redis/redis"
)

func main(){
	client := redis.NewClient(&redis.Options{
		Addr: 		"localhost:6379",
		Password: 	"", //no password set
		DB:			0, //use default DB
	})

	pong, err := client.Ping().Result()
	fmt.Println(pong, err)

	//set key value
	err = client.Set("key", "value", 0).Err()
	if err != nil{
		panic(err)
	}
	//get key
	val, err := client.Get("key").Result()
	if err != nil{
		panic(err)
	}
	fmt.Println("get key,",val)

	//get key which is not exist
	val2, err := client.Get("key2").Result()
	if err == redis.Nil {
		fmt.Println("key2 does not exist")
	} else if err != nil {
		panic(err)
	} else {
		fmt.Println("key2", val2)
	}
}

//output
/*

root@wxtest047:/home/cxy/redis/code/demos# ./redis-demo
PONG <nil>
get key, value
key2 does not exist


 */
```











