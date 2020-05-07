
[TOC]

# mysql

总的存储量没有限制，受限于操作系统对目录数量的支持程度，网上有人说最大容量64TB

mysql性能规范:
单表记录，传闻百度的DBA测试单表到2000万行之后，性能急剧下降，是因为mysql为了提高性能，会将索引装载到内存，InnoDB buffer size 足够的情况下，其能完成全加载进内存，查询不会有问题。但是，当单表数据库到达某个量级的上限时，导致内存无法存储其索引，使得之后的 SQL 查询会产生磁盘 IO，从而导致性能下降；
而阿里巴巴《Java 开发手册》提出单表行数超过 500 万行或者单表容量超过 2GB，就推荐分库分表

## install
### ubuntu下安装
```
apt-get install mysql-server mysql-client
```
执行完之后，mysqld就会启动
#### 检查是否安装成功
```
root@wxtest045:~# netstat -tap | grep mysql
tcp        0      0 *:mysql                 *:*                     LISTEN      2188/mysqld
```
#### 修改配置，可以让远程机器登录
```
$ vim /etc/mysql/mysql.conf.d/mysqld.cnf
...
#
# Instead of skip-networking the default is now to listen only on
# localhost which is more compatible and is not less secure.
bind-address            = 0.0.0.0
...
```
重启mysql服务
```
root@wxtest046:~# vim /etc/mysql/mysql.conf.d/mysqld.cnf
root@wxtest046:~# /etc/init.d/mysql restart
[ ok ] Restarting mysql (via systemctl): mysql.service.
root@wxtest046:~# netstat -nlt | grep 3306
tcp        0      0 0.0.0.0:3306            0.0.0.0:*               LISTEN
root@wxtest046:~#
```

- 查看mysql监听的端口ip
```
root@wxtest045:~# netstat -nlt|grep 3306
tcp        0      0 0.0.0.0:3306            0.0.0.0:*               LISTEN
```

#### 修改mysql编码格式为utf-8
- 修改之前
```
mysql> show variables like '%char%';
+--------------------------+----------------------------+
| Variable_name            | Value                      |
+--------------------------+----------------------------+
| character_set_client     | utf8                       |
| character_set_connection | utf8                       |
| character_set_database   | latin1                     |
| character_set_filesystem | binary                     |
| character_set_results    | utf8                       |
| character_set_server     | latin1                     |
| character_set_system     | utf8                       |
| character_sets_dir       | /usr/share/mysql/charsets/ |
+--------------------------+----------------------------+
8 rows in set (0.00 sec)

mysql>
```
- 修改
vim /etc/mysql/mysql.conf.d/mysqld.cnf
```
...
[mysqld]
character_set_server = utf8
collation-server=utf8_general_ci
...
```
root@wxtest046:~# vim /etc/mysql/mysql.conf.d/mysqld.cnf
root@wxtest046:~#

- 修改完之后，重启服务，登录mysql再次查看
```
root@wxtest046:~# /etc/init.d/mysql restart
[ ok ] Restarting mysql (via systemctl): mysql.service.
mysql> show variables like '%char%';
+--------------------------+----------------------------+
| Variable_name            | Value                      |
+--------------------------+----------------------------+
| character_set_client     | utf8                       |
| character_set_connection | utf8                       |
| character_set_database   | utf8                       |
| character_set_filesystem | binary                     |
| character_set_results    | utf8                       |
| character_set_server     | utf8                       |
| character_set_system     | utf8                       |
| character_sets_dir       | /usr/share/mysql/charsets/ |
+--------------------------+----------------------------+
8 rows in set (0.00 sec)
```


### mysql dameon
[参考这里](https://blog.csdn.net/alexdamiao/article/details/51498684)
有三种方式可以启动mysql守护进程
1、mysqld守护进程启动

一般的，我们通过这种方式手动的调用mysqld，如果不是出去调试的目的，我们一般都不这样做。这种方式如果启动失败的话，错误信息只会从终端输出，而不是记录在错误日志文件中，这样，如果mysql崩溃的话我们也不知道原因，所以这种启动方式一般不用在生产环境中，而一般在调试（debug）系统的时候用到。
启动方法：
```
[root@test libexec]# ./mysqld
```
2、mysqld_safe启动

mysqld_safe是一个启动脚本，该脚本会调用mysqld启动，如果启动出错，会将错误信息记录到错误日志中，mysqld_safe启动mysqld和monitor mysqld两个进程，这样如果出现mysqld进程异常终止的情况，mysqld_safe会重启mysqld进程。
启动方法：
```
[root@test bin]# ./mysqld_safe &
```
符号“&”表示在后台启动

3、mysql.server启动

mysql.server同样是一个启动脚本，调用mysqld_safe脚本。它的执行文件在$MYSQL_BASE/share/mysql/mysql.server 和 support-files/mysql.server。
主要用于系统的启动和关闭配置

启动方法：
```
[root@test ~]# cp mysql-5.5.34/support-files/mysql.server /etc/init.d/mysql
[root@test ~]# chmod u+x /etc/init.d/mysql
[root@test ~]# service mysql start
```

### 修改用户密码
```
$ mysql -uroot -pold-password
mysql> set password for 'root'@'localhost'=password('sd-9898w');
Query OK, 0 rows affected, 1 warning (0.00 sec)

mysql> exit
```
新密码重新登录
$ mysql -uroot -psd-9898w


## example

### 查看数据库大小
```
mysql> use information_shema;
<!-- 以kB方式显示，注意下面的 SUM(DATA_LENGTH)/1024 -->
mysql> SELECT TABLE_SCHEMA, SUM(DATA_LENGTH)/1024 FROM TABLES GROUP BY TABLE_SCHEMA;
+--------------------+-----------------------+
| TABLE_SCHEMA       | SUM(DATA_LENGTH)/1024 |
+--------------------+-----------------------+
| audio_finger       |             5760.0000 |
| VideoStructure     |             8192.0000 |
| XL_VideoDB         |             5728.0000 |
+--------------------+-----------------------+
18 rows in set (3.07 sec)

```
- 单独查询某个库的大小
查询某个数据库的空间大小
`SELECT concat(round(sum(DATA_LENGTH/1024/1024),2),'MB') as data FROM TABLES WHERE table_schema='要查询的数据库名字';`
```
mysql> select concat(round(sum(DATA_LENGTH/1024/1024),2),'MB') as data from tables where table_schema='XL_VideoDB';
+--------+
| data   |
+--------+
| 5.59MB |
+--------+
1 row in set (0.01 sec)
```
- 单独查询某个表的大小
查询某个数据库中某个表的空间大小
`SELECT concat(round(sum(DATA_LENGTH/1024/1024),2),'MB') as data FROM TABLES WHERE table_schema='要查询的数据库名字' and TABLE_NAME='要查询的表名';`
```
mysql> select concat(round(sum(DATA_LENGTH/1024/1024),2),'MB') as data from tables where table_schema='XL_VideoDB' and table_name='query_cache_xl';
+--------+
| data   |
+--------+
| 0.02MB |
+--------+
1 row in set (0.00 sec)
```

### 修改字段属性
对于一些已经创建好的属性，总有需求去修改某个字段的属性，比如varchar(128)修改为varchar(256)

关键字:
**modify** 和 **change**
  - 相同点:
    - 都可以修改字段 属性，数据格式类型，以及约束条件
  - 不同点:
    - change可以修改字段名，modify不能
首先创建一个测试的表
```sql
mysql> create table test_alter(id int unsigned auto_increment key, name varchar(30) not null);
Query OK, 0 rows affected (5.00 sec)

mysql> desc test_alter;
+-------+------------------+------+-----+---------+----------------+
| Field | Type             | Null | Key | Default | Extra          |
+-------+------------------+------+-----+---------+----------------+
| id    | int(10) unsigned | NO   | PRI | NULL    | auto_increment |
| name  | varchar(30)      | NO   |     | NULL    |                |
+-------+------------------+------+-----+---------+----------------+
2 rows in set (0.00 sec)
```

#### 修改字段名称和属性
```sql
mysql> alter table test_alter change name change_name varchar(200) unique not null;
Query OK, 0 rows affected (0.00 sec)
Records: 0  Duplicates: 0  Warnings: 0

mysql> desc test_alter;
+-------------+------------------+------+-----+---------+----------------+
| Field       | Type             | Null | Key | Default | Extra          |
+-------------+------------------+------+-----+---------+----------------+
| id          | int(10) unsigned | NO   | PRI | NULL    | auto_increment |
| change_name | varchar(200)     | NO   | UNI | NULL    |                |
+-------------+------------------+------+-----+---------+----------------+
4 rows in set (0.00 sec)
```
#### 修改字段类型长度
使用 modify 修改 name的类型长度
```sql
mysql> alter table test_alter modify name varchar(128) not null;
Query OK, 0 rows affected (0.01 sec)
Records: 0  Duplicates: 0  Warnings: 0

mysql> desc test_alter;
+-------+------------------+------+-----+---------+----------------+
| Field | Type             | Null | Key | Default | Extra          |
+-------+------------------+------+-----+---------+----------------+
| id    | int(10) unsigned | NO   | PRI | NULL    | auto_increment |
| name  | varchar(128)     | NO   |     | NULL    |                |
+-------+------------------+------+-----+---------+----------------+
2 rows in set (0.00 sec)
```

#### 增加字段
增加两个字段，age，sex
```sql
mysql> alter table test_alter add age int not null, add sex varchar(32) not null;
Query OK, 0 rows affected (0.00 sec)
Records: 0  Duplicates: 0  Warnings: 0

mysql> desc test_alter;
+-------+------------------+------+-----+---------+----------------+
| Field | Type             | Null | Key | Default | Extra          |
+-------+------------------+------+-----+---------+----------------+
| id    | int(10) unsigned | NO   | PRI | NULL    | auto_increment |
| name  | varchar(128)     | NO   |     | NULL    |                |
| age   | int(11)          | NO   |     | NULL    |                |
| sex   | varchar(32)      | NO   |     | NULL    |                |
+-------+------------------+------+-----+---------+----------------+
4 rows in set (0.00 sec)
```


## mysql数据恢复
[参考这里](https://www.cnblogs.com/gomysql/p/3582058.html)




