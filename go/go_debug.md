# go程序debug

## dlv
[开源的go代码调试工具](https://github.com/go-delve/delve)

## Linux安装
1. go get获取源码
```
go get -u github.com/go-delve/delve/cmd/dlv
```
2. 编译
进入goroot下 `src/github.com/go-delve/delve/cmd/dlv` 目录,执行`go build`即可生成dlv可执行文件


## debug使用
[详细可以参考这里](http://lday.me/2017/02/27/0005_gdb-vs-dlv/)

```dlv exec ./<your-execute-bin> ```
```
//开始调试
[root@tw06a1573 recovery_fingerprint]# ./dlv exec ./fp_writer_1
Type 'help' for list of commands.
//设置断点
(dlv) b main.main
Breakpoint 1 set at 0x85d36f for main.main() /Users/chenxingyi/work/go/src/wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/main/fp_writer_1.go:5
//设置断点
(dlv) b query_fingerprints.go:281
Breakpoint 2 set at 0x844d50 for wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/fp_algorithms.SelectQueryFeatures() /Users/chenxingyi/work/go/src/wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/fp_algorithms/query_fingerprints.go:281
//显示所有断点
(dlv) bp
Breakpoint unrecovered-panic at 0x42a8c0 for runtime.startpanic() /Users/chenxingyi/work/go/go1.10.8/src/runtime/panic.go:588 (0)
	print runtime.curg._panic.arg
Breakpoint 1 at 0x85d36f for main.main() /Users/chenxingyi/work/go/src/wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/main/fp_writer_1.go:5 (0)
Breakpoint 2 at 0x844d50 for wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/fp_algorithms.SelectQueryFeatures() /Users/chenxingyi/work/go/src/wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/fp_algorithms/query_fingerprints.go:281 (0)
//继续运行
(dlv) c
> main.main() /Users/chenxingyi/work/go/src/wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/main/fp_writer_1.go:5 (hits goroutine(1):1 total:1) (PC: 0x85d36f)
//打印某个变量 startIndex的值，*这里出现某些变量不能答应的现象*
(dlv) p startIndex
Command failed: could not find symbol value for startIndex
//单步运行
(dlv) n
> main.main() /Users/chenxingyi/work/go/src/wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/main/fp_writer_1.go:7 (PC: 0x85d37b)
//打印变量 len(disArray)
dlv) p len(disArray)
Command failed: could not find symbol value for disArray
//打印变量
(dlv) p startIndex
10780
//打印变量
(dlv) p sampleRange
600
//设置断点
(dlv) b query_fingerprints.go:288
Command failed: could not find /Users/chenxingyi/work/go/src/wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/fp_algorithms/query_fingerprints.go:288
(dlv) b query_fingerprints.go:289
Breakpoint 3 set at 0x844c24 for wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/fp_algorithms.SelectQueryFeatures() /Users/chenxingyi/work/go/src/wx-gitlab.xunlei.cn/ai/shortvideo_finger/src/fp_algorithms/query_fingerprints.go:289
(dlv)
```

## 问题
在调试过程中，发现有部分变量无法print其val，有部分源码的某些行无法设置断点
