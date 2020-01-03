# go程序测试
作为一名合格的开发，在写完程序之后，首先是要对自己的程序进行测试，go框架中已经提供了此功能；
主要是单元测试(unit test), 基准测试(benchmark test)和样本测试(example test)

## unit test
单元测试一般用作具体函数实现的功能测试
**go test指令**
```
go test 默认执行当前目录下以xxx_test.go的测试文件。
go test -v 可以看到详细的输出信息。
go test -v xxx_test.go 指定测试单个文件，但是该文件中如果调用了其它文件中的模块会报错。
```


**注意**：在执行```go test```的时候，后面如果跟的是 *包名* 那么程序将会运行此包所有的 xxxx_test.go 测试程序，如果是要单独测试具体的某个文件 AAA_test.go
则其命令为```go test AAA_test.go [源码文件AAA.go已经AAA.go中依赖的此包中其他所有xxx.go源码文件]```;否则会报*undefined: xxxx*



## benchMark test
基准测试一般用作测试某个函数的性能测试



## 样本测试

