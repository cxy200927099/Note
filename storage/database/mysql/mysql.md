# mysql


## install


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
## example


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
