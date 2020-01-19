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
```sbt
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







