# scala


## sbt构建scala

### sbt版本依赖问题
- sbt中 %%与 %的区别
|%%|%|
|---|---|
|自动给库包的报名结尾加上Scala的版本号|只用于分割groupId与artifactId|

### sbt插件问题
1. 在使用sbt-protoc插件时，需要在工程目录下添加文件
project/scalapb.sbt,内容如下:
```scala
addSbtPlugin("com.thesamet" % "sbt-protoc"  % "0.99.25")
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.9.0"
```
但是编译的时候，一直提示错误在marven仓库中找不到sbt-protoc,然后去到marven仓库去搜索，确实找不到sbt-protoc
尝试过 其他方案，
比如更改sbt的源，即在`vim ~/.sbt/repositories`,然后添加源，最后还是编译不过，试过阿里云的源，开源中国，jcenter的源都没能解决问题
最后是因为需要添加sbt-plugin-repo的源,如下
```
chenxingyi@cxy-mac-pro  ~  cat ~/.sbt/repositories
[repositories]
local
maven-central
nexus-aliyun:http://maven.aliyun.com/nexus/content/groups/public
sbt-plugins-repo: http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/, [organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]
```

相关代码参考[这里](https://github.com/cxy200927099/scala-grpc)



## sbt构建打包问题
在利用spark开发的时候，提交jar包到spark上，往往用sbt打包出来的包，只包含了自己的代码，所以自然是运行的时候会报找不到xxx.class的问题
此时我们可以通过assembly插件，将需要的依赖打包到一个jar包即可，具体如下
在<工程目录>/project/scalapb.sbt中加入如下代码
```
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")
```
这时候打包的话，一般会报错，依赖冲突，具体情况具体解决，比如我这里的错误
`duplicate META-INF/io.netty.versions.properties`
解决方案:
在工程目录下的build.sbt中添加,排除相关依赖的方法
```scala
assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
```
或者这种写法
```scala
assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
```


## spark上运行jar包错误问题
1. 一直提示找不到guava的某个方法:
原因: spark上提交jar包的时候，spark默认会拷贝其配置下的相关依赖的jar包到集群，比如我们这里的配置,而且上面的guava版本特别旧
spark上查看当前环境的jar包
/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/spark/jars
```shell
(base) [root@tw06a2116 jars]# ls | grep guava
guava-11.0.2.jar
```
而我们scala grpc依赖的guava包版本需要18以上，这里我们下载好guava的高版本如 `guava-20.0.jar` 之后，将其拷贝到
目录`/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/spark/jars/`下之后，再次提交即可









