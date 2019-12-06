# go module笔记
学会研读[官方英文文档](https://blog.golang.org/using-go-modules)，往往官方的英文文档是描写的最直观的
go版本1.11开始引入了go module，所以要想使用，最好升级go的版本到最新
go module用于管理go的依赖package，在go module出现之前，依赖package都需要从$GOPATH/src 目录下去寻找，
这使得项目在结构上管理及其不方便,尤其是想依赖本地的其他package; 

## 使用go module
go module即将packge管理起来，在package的root目录下提供 
**go.mod文件:**
这个go.mod文件定义了 *modul path, 版本号, 此模块依赖的其他模块关系*
go1.11的时候，在`$GOPATH/src`目录下存在moduleA,同时其他目录也包含相同的moduleA，那go默认是从`$GOPATH/src`目录下去加载依赖
go1.13开始，go module变为了默认的加载方式
```
$ cat go.mod
module example.com/hello

go 1.12

require (
    golang.org/x/text v0.3.0 // indirect
    rsc.io/quote v1.5.2
    rsc.io/quote/v3 v3.0.0
    rsc.io/sampler v1.3.1 // indirect
)
$
```
- 版本号意义:
<major>.<minor>.<patch>
MAJOR: 主版本号，一般是做了向下不兼容的改变时才会更改这个版本号
MINOR: 次版本号，一般是做了些向下兼容的api修改，或者增加了新的功能，函数，struct以及type时，修改次版本号
PATCH: 补丁号，比如修复一些bug，或者做了些修改但是不影响公开的接口功能


**go.sum文件:**
这个文件描述了module依赖的特定模块与其对应的 hash映射关系
go命令使用go.sum文件可以确保，在将来的任何时刻下载这些模块时与第一次下载的是同一版本，确保您的项目所依赖的模块不会被意外更改
```
$ cat go.sum
golang.org/x/text v0.0.0-20170915032832-14c0d48ead0c h1:qgOY6WgZO...
golang.org/x/text v0.0.0-20170915032832-14c0d48ead0c/go.mod h1:Nq...
rsc.io/quote v1.5.2 h1:w5fcysjrx7yqtD/aO+QwRjYZOKnaM9Uh2b40tElTs3...
rsc.io/quote v1.5.2/go.mod h1:LzX7hefJvL54yjefDEDHNONDjII0t9xZLPX...
rsc.io/sampler v1.3.0 h1:7uVkIFmeBqHfdjD+gZwtXXI+RODJ2Wc4O7MPEh/Q...
rsc.io/sampler v1.3.0/go.mod h1:T1hPZKmBbMNahiBKFy5HrXp6adAjACjK9...
$
```

在版本管理的时候，比如git为例，要把go.mod和go.sum文件一起提交

### 相关命令
`go mod init`: 创建新的module，初始化go.mod文件
`go build,go test`:以及其他build命令，编译时会去查找相关依赖，并下载到本地缓存，然后将所需要的依赖添加到go.mod文件中
`go list -m all`: 打印当前module所有的依赖
`go get`: 改变当前依赖的版本(或者添加新的依赖)
`go mod tidy`: 删除所有无用的依赖

go module允许项目同时存在多个主要版本

## 发布module让他人可以使用
以git为例

创建并初始化要发布的工程，将相关文件添加到git并提交，如果要发布，需要添加LICENSE 文件
```
$ git init
$ git add LICENSE go.mod go.sum hello.go hello_test.go
$ git commit -m "hello: initial commit"
$
```

### 版本号的管理
发布时注意好版本号的管理，具体参考上文的 **版本号命名意义**
可以指定预发布的版本，通过在版本号后面添加相关的说明 比如 ```v1.0.1-alpha 或者 v2.2.2-beta.2```
如果发布的版本中包含普通的版本，go命令通常会优先加载普通的发布版本，而不是预发布版本，
所以在下载预发布版本的时候，需要显示的指定具体的版本说明比如: ```got get example.com/hello@v1.0.1-alpha```

go.mod文件中的指定的版本号，可能是git中打的某个tag的版本号(比如v1.1.1);
也可能是**伪版本号**(比如 v0.0.0-20191204032832-14c0d48ead0c)，伪版本号是应对某些项目没有添加任何发布版本的tag号码，因此
对于这类依赖项目，不要去指望这些接口的稳定性；

我们在发布的时候，最好应该添加对应的发布tag版本号码，标记这个版本是经过完整测试的，这样也可以给其他使用者了解对应的功能

不要删除repo仓库中已经发布的 tag号码

#### v0版本
这个版本一般是不稳定的初始版本
打tag的步骤
1. 执行`go mod tidy`删除无用的依赖
2. 执行`go test ./...` 确保发布的module work是正常的
3. `git tag -a v1.0.3-fix -m "修复1.0.3已知的两个问题;"`命令打标签
4. `git push origin v1.0.3-fix` 推送到远程仓库
**例子**
```
$ go mod tidy
$ go test ./...
ok      example.com/hello       0.015s
$ git add go.mod go.sum hello.go hello_test.go
$ git commit -m "hello: changes for v0.1.0"
$ git tag v0.1.0
$ git push origin v0.1.0
$
```

#### v1版本
这是第一个稳定的版本
这个版本发布之后，我们对外暴露的相对应的api，后续的更改最好不要改变已发布的api，尽量使用新的api来取代之前老的api

#### v2版本以及后续的版本
随着项目逐渐累积，比如一些新的需求由于以前的设计缺陷无法实现，或者一些api缺陷需要删除，
开发人员需要更改这类api或者拆分之前的package，这种更改需要使用者花费大量精力来迁移到新的api，因此对于此类更改
需要仔细权衡下更改的成本与收益，再决定是否更改




