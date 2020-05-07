[TOC]

# beego
beego是一个go web开发框架，其基于八大独立的模块构建的，是一个高度解耦的框架。当初设计 beego 的时候就是考虑功能模块化，用户即使不使用 beego 的 HTTP 逻辑，也依旧可以使用这些独立模块，例如：你可以使用 cache 模块来做你的缓存逻辑；使用日志模块来记录你的操作信息；使用 config 模块来解析你各种格式的文件。所以 beego 不仅可以用于 HTTP 类的应用开发，在你的 socket 游戏开发中也是很有用的模块，这也是 beego 为什么受欢迎的一个原因。大家如果玩过乐高的话，应该知道很多高级的东西都是一块一块的积木搭建出来的，而设计 beego 的时候，这些模块就是积木，高级机器人就是 beego

- beego监控
beego 目前做了一个很酷的模块，应用内监控模块，会在 8088 端口做一个内部监听，我们可以通过这个端口查询到 QPS、CPU、内存、GC、goroutine、thread 等统计信息。


## config模块
beego的config文件解析模块，支持的文件格式有ini,json,xml,yaml



## log模块
简单使用方式：
```go
package logs

import (
	"github.com/astaxie/beego/logs"
)

var Log *logs.BeeLogger


func InitLog() {
  Log = logs.NewLogger()
  // 输出文件名和行号
  Log.EnableFuncCallDepth(true)
  // 默认的值，直接调用的层级，如果对log进行了封装，需要调整下这个值
  Log.SetLogFuncCallDepth(2)
  // log输出的地方，cosnole即os.stdout
  Log.SetLogger(logs.AdapterConsole)
  // 设置异步日志出书，并设置缓冲chan的大小
	Log.Async(10000)
}
```
[具体使用方式](https://beego.me/docs/module/logs.md)



## orm


## cache
