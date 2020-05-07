[TOC]

# beego
beego是一个go web开发框架，其基于八大独立的模块构建的，是一个高度解耦的框架。当初设计 beego 的时候就是考虑功能模块化，用户即使不使用 beego 的 HTTP 逻辑，也依旧可以使用这些独立模块，例如：你可以使用 cache 模块来做你的缓存逻辑；使用日志模块来记录你的操作信息；使用 config 模块来解析你各种格式的文件。所以 beego 不仅可以用于 HTTP 类的应用开发，在你的 socket 游戏开发中也是很有用的模块，这也是 beego 为什么受欢迎的一个原因。大家如果玩过乐高的话，应该知道很多高级的东西都是一块一块的积木搭建出来的，而设计 beego 的时候，这些模块就是积木，高级机器人就是 beego

- beego监控
beego 目前做了一个很酷的模块，应用内监控模块，会在 8088 端口做一个内部监听，我们可以通过这个端口查询到 QPS、CPU、内存、GC、goroutine、thread 等统计信息。

## bee工具
bee工具可以帮助用户创建基于beego框架的项目结构

### bee new(创建web应用)
bee new <服务名称>
执行成功之后，会在 $GOPATH 目录下创建好项目，如下所示
```
~/work/go/code/fpWriter$ bee new webDemo
______
| ___ \
| |_/ /  ___   ___
| ___ \ / _ \ / _ \
| |_/ /|  __/|  __/
\____/  \___| \___| v1.9.1
2020/05/07 16:02:32 WARN     ▶ 0001 You current workdir is not inside $GOPATH/src.
2020/05/07 16:02:32 INFO     ▶ 0002 Creating application...
        create   /Users/chenxingyi/work/go/src/webDemo/
        create   /Users/chenxingyi/work/go/src/webDemo/conf/
        create   /Users/chenxingyi/work/go/src/webDemo/controllers/
        create   /Users/chenxingyi/work/go/src/webDemo/models/
        create   /Users/chenxingyi/work/go/src/webDemo/routers/
        create   /Users/chenxingyi/work/go/src/webDemo/tests/
        create   /Users/chenxingyi/work/go/src/webDemo/static/
        create   /Users/chenxingyi/work/go/src/webDemo/static/js/
        create   /Users/chenxingyi/work/go/src/webDemo/static/css/
        create   /Users/chenxingyi/work/go/src/webDemo/static/img/
        create   /Users/chenxingyi/work/go/src/webDemo/views/
        create   /Users/chenxingyi/work/go/src/webDemo/conf/app.conf
        create   /Users/chenxingyi/work/go/src/webDemo/controllers/default.go
        create   /Users/chenxingyi/work/go/src/webDemo/views/index.tpl
        create   /Users/chenxingyi/work/go/src/webDemo/routers/router.go
        create   /Users/chenxingyi/work/go/src/webDemo/tests/default_test.go
        create   /Users/chenxingyi/work/go/src/webDemo/main.go
2020/05/07 16:02:32 SUCCESS  ▶ 0003 New application successfully created!
```

### bee api(创建api服务)
bee api <服务名称>
执行成功之后，会在 $GOPATH 目录下创建好项目，如下所示
```
~/work/go/code/fpWriter$ bee api fpWriter
______
| ___ \
| |_/ /  ___   ___
| ___ \ / _ \ / _ \
| |_/ /|  __/|  __/
\____/  \___| \___| v1.9.1
2020/05/07 15:59:58 WARN     ▶ 0001 You current workdir is not inside $GOPATH/src.
2020/05/07 15:59:58 INFO     ▶ 0002 Creating API...
        create   /Users/chenxingyi/work/go/src/fpWriter
        create   /Users/chenxingyi/work/go/src/fpWriter/conf
        create   /Users/chenxingyi/work/go/src/fpWriter/controllers
        create   /Users/chenxingyi/work/go/src/fpWriter/tests
        create   /Users/chenxingyi/work/go/src/fpWriter/conf/app.conf
        create   /Users/chenxingyi/work/go/src/fpWriter/models
        create   /Users/chenxingyi/work/go/src/fpWriter/routers/
        create   /Users/chenxingyi/work/go/src/fpWriter/controllers/object.go
        create   /Users/chenxingyi/work/go/src/fpWriter/controllers/user.go
        create   /Users/chenxingyi/work/go/src/fpWriter/tests/default_test.go
        create   /Users/chenxingyi/work/go/src/fpWriter/routers/router.go
        create   /Users/chenxingyi/work/go/src/fpWriter/models/object.go
        create   /Users/chenxingyi/work/go/src/fpWriter/models/user.go
        create   /Users/chenxingyi/work/go/src/fpWriter/main.go
2020/05/07 15:59:58 SUCCESS  ▶ 0003 New API successfully created!
```


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

### beego连接mysql说明
- url
[username[:password]@][protocol[(address)]]/dbname[?param1=value1&...&paramN=valueN]

- beego代码
orm.RegisterDataBase("cdndb", "mysql", "cdn_support_r:XXXXXX@tcp(10.10.10.10:3860)/testdb?charset=utf8")


## cache
