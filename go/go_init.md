
# go init
go中init函数是先于main函数执行的，因此可以用来初始化一些配置
- init函数特点
> init函数不能被其他函数调用，执行顺序 init > main
> 无参数，无返回值
> 每个包可有多个init函数
> 包的每个源文件也可以有多个init函数
> 同一个包init执行顺序不确定，因此在编程中不能依赖这个执行顺序
> 不同包的init函数执行顺序与包的导入依赖关系有关

## 同一个文件内
变量初始化->init()->main()


## 不同包
p1.go
```go
package p1

import "fmt"

var VP1 int = 10

func init(){
	fmt.Println("init p1!")
}

```

p2.go
```go
package p2

import (
	"fmt"
	"goDemos/init/p1"
)

var VP2 = 5

func init() {
	a := p1.VP1
	fmt.Println("init p2 a=",a)
}
```

main.go
```go
package main

import (
	"fmt"
	"goDemos/init/p1"
	"goDemos/init/p2"
)

func main() {
	fmt.Println("package p2: ",p2.VP2)
	fmt.Println("package p1: ",p1.VP1)
}

```

- 输出结果
```log
p1 init!
init p2 a= 10
package p2:  5
package p1:  10
```

p2依赖p1,因此在调用的时候，先初始化p1,才初始化p2
p1(init) > p2(init)


- 导入例子
```go
import _ "net/http/pprof"
```
在平常进行性能调优的时候，通常就import一句话，实际上这个操作之后，会调用
pprof.go中的init函数，其实现如下
```go
func init() {
	http.HandleFunc("/debug/pprof/", Index)
	http.HandleFunc("/debug/pprof/cmdline", Cmdline)
	http.HandleFunc("/debug/pprof/profile", Profile)
	http.HandleFunc("/debug/pprof/symbol", Symbol)
	http.HandleFunc("/debug/pprof/trace", Trace)
}
```
注册了http相关接口，后续只需要监听相关端口，就可以实现go的profile了



