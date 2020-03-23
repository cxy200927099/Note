
[TOC]


## 查看cpu占用--top



## 查看磁盘占用--top观看%wa

% wa 占比比较高的话，说明磁盘io读写比较频繁


## Linux定时任务----crontab

* 默认crontab是加锁的，不让随便编辑，执行下这个解锁命令
```chattr  -i /var/spool/cron/root```

[参考这里](https://www.cnblogs.com/mingforyou/p/3930636.html)
```
//为当前用户创建crontab
crontab -e   
```
在文件中输入如下，保存退出
```shell
*/2 * * * * /home/root1/checkProxyMemory/hbaseproxy_mem_check.sh > /tmp/ai_hbaseproxy_monitor.log 2>&1
```
每隔2分钟检查一次

### 示例图
![crontab_struct](images/crontab_struct.png)
### 范例一：用dmtsai的身份在每天的12：00发信给自己
`crontab -e`

此时会进入vi的编辑界面让你编辑工作。注意到，每项工作都是一行。
|分 |  时 |  日  | 月 | 周  |《==============命令行=======================》|
|---|---|---|---|---|---|
|  0  |  12 |  * | * |  * |  `mail dmtsai -s "at 12:00" < /home/dmtsai/.bashrc`|

|代表意义	|分钟	|小时|	日期|	月份|	周|	命令|
|---|---|---|---|---|---|---|
|数字范围	|0~59|0~23|	1~31|	1~12|	0~7|shell命令啊|
周的数字为0或7时，都代表“星期天”的意思。另外，还有一些辅助的字符，大概有下面这些：
|特殊字符|代表意义|
|---|---|
|`*(星号)`|代表任何时刻都接受的意思。举例来说，范例一内那个日、月、周都是*，就代表着不论何月、何日的礼拜几的12：00都执行后续命令的意思。|
|,(逗号)|	代表分隔时段的意思。举例来说，如果要执行的工作是3：00与6：00时，就会是：<br>0 3,6 * * * <br>command时间还是有五列，不过第二列是 3,6 ，代表3与6都适用|
|-(减号)|	代表一段时间范围内，举例来说，8点到12点之间的每小时的20分都进行一项工作：<br>20 8-12 * * * <br>command仔细看到第二列变成8-12.代表 8,9,10,11,12 都适用的意思|
|/n(斜线)|	 那个n代表数字，即是每隔n单位间隔的意思，例如每五分钟进行一次，则：<br>`*/5 * * * * command` <br>用*与/5来搭配，也可以写成0-59/5，意思相同|


## rsync 同步大量数据
//本地同步到远程, 需要输入密码: ERGG4Merqk8A
```
//这个命令会将multi_label整个目录下所有文件，都按原来的排布同步到远程目录12306_multi_label/multi_label下
rsync -av  --progress -e ssh multi_label  root1@10.33.23.176:/data2/cxy/12306_multi_label/multi_label
```


## 命令行光标移动
- ctrl + u 删除从开头到光标处的命令文本
- ctrl + k 删除从光标到结尾处的命令文本 
- ctrl + a:光标移动到命令开头
- ctrl + e：光标移动到命令结尾
- alt f:光标向前移动一个单词
- alt b：光标向后移动一个单词
- ctrl w：删除一个词（以空格隔开的字符串）



## 快速找出两个文件的差异，并输出
这里需要说明的是，要找的两个文件 A, B内容格式是一样的，只有不同的如
```
 chenxingyi@bogon  ~  cat a.txt
aaaaaaaa
bbbbbbbb
ccccccccc
 chenxingyi@bogon  ~  cat b.txt
aaaaaaaa
bbbbbbbb
ccccccccc
ddddddddd
eeeeeeeeeee
```

命令: `grep -v -f a.txt b.txt`  //从b.txt中剔除a.txt有的

```
chenxingyi@bogon  ~  grep -v -f a.txt b.txt
ddddddddd
eeeeeeeeeee
```


## 分割字符串
命令: `echo $str | cut -d \= -f 1`
将str字符串内容，以'=' 进行分割，-f 后面是取分割后的第几个内存
例子
```shell
$ str=file=/data2/audiofinger/audio/intune_with_timestamp/00024000/song_222874_64.mp4
$ echo $str
file=/data2/audiofinger/audio/intune_with_timestamp/00024000/song_222874_64.mp4
$ echo $str | cut -d \= -f 1
file
$ echo $str | cut -d \= -f 2
/data2/audiofinger/audio/intune_with_timestamp/00024000/song_222874_64.mp4
```

## 字符串截取

```
root@wxtest045:# str="song_1000107_64.mp4.mp3"; echo ${str%.mp3}
song_1000107_64.mp4
```

## 删除文本中匹配的某一行
命令: `sed -i '/<要匹配的内容>/d' <file path>`
删除file中匹配<要匹配的内容> 的行


## 跨网传同步数据
比如机器A上的 xxx.tar，要传到机器B上 /data1/test/目录 
首先需要知道机器B的外网ip
```
[root@ina005 feature_file_update_20191129]# curl ip.cn -L
{"ip": "13.126.3.86", "country": "印度", "city": "Amazon"}
```
进入机器B目录/data1/test/ 执行:
```nc -l 30976 >xxx.tar```
机器A上执行:
nc 13.126.3.86  30976 <xxx.tar

等待传输完成即可
传输过程中，可以到机器B上查看xxx.tar文件大小是否在增长，一直增长，说明文件正在传输中
传输完成可以用md5sum验证下文件传输的准确性


## 传输目录
B机器(180.97.167.168）
启动监听: nc -l 30976 | tar xfvz -
A机器
进入需要传递文件的目录
tar cfz - * | nc 180.97.167.168 30976

对于我们公司的外网机器，貌似只有30976端口可以传输


## 查看本机外网ip
curl ip.cn -L
查看本机外网ip


## 服务部署常用shell脚本
[服务部署常用shell脚本](https://github.com/cxy200927099/Note/blob/master/linux/shell/%E6%9C%8D%E5%8A%A1%E9%83%A8%E7%BD%B2%E5%B8%B8%E7%94%A8shell%E8%84%9A%E6%9C%AC.md)










