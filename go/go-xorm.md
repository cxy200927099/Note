[toc]

# go-xorm
go-xorm是go的一个操作数据库的orm工具库，通过这个工具可以根据数据库的字段自动生成go代码
[官网](https://github.com/go-xorm/cmd)

## install
`go get github.com/go-xorm/cmd/xorm`

- 编译
```
$ cd $GOPATH/src/github.com/go-xorm/cmd/xorm
$ go build
```

### 问题
[invalid pseudo-version](https://blog.cocktail1024.top/archives/130.html)
执行go build 或者 go get的时候出现错误`invalid pseudo-version`
- 例如:
```
$ go build
go: github.com/go-xorm/xorm@v0.0.0-20180925133144-7a9249de3324 requires
	github.com/go-xorm/core@v0.0.0-20180322150003-0177c08cee88: invalid pseudo-version: does not match version-control timestamp (2018-03-22T14:29:44Z)
```

- 原因：
远程包的时间戳跟go mod 记录的时间戳不对应

- 解决方案：
`go mod edit -replace=github.com/go-xorm/core@v0.0.0-20180322150003-0177c08cee88=github.com/go-xorm/cmd/xorm@master`
第一个 @ 后面的 v0.0.0-20180322150003-0177c08cee88 是 旧的版本信息
第二个 @ 后面的master 是 新的 版本信息，可以是 version，也可以是 git 版本


- 再次编译，就没问题了


## 生成代码
```bash
# 进入要生成代码的目录
$ cd ~/work/go/src/fpWriter
# 将 ~/work/go/src/github.com/go-xorm/cmd/xorm/templates 目录拷贝到当前文件夹
$ cp ~/work/go/src/github.com/go-xorm/cmd/xorm/templates ./ -r
# 生成代码将会把远端数据库XL_VideoDB中的所有表生成对应的model代码
$ xorm reverse mysql "test:test@tcp(ip:port)/XL_VideoDB?charset=utf8" templates/goxorm

```
执行完之后，上述会在当前目录创建models文件夹，并在models文件夹下生成每个表对应的model

**注意: 上面 test:test@tcp(ip:port)/XL_VideoDB?charset=utf8 这个需要用""来包含，否则会出现错误`no matches found: test:test@tcp(ip:port)/XL_VideoDB?charset=utf8`**


