
## python3中文问题

### 读取中文path
[参考这里](https://www.cnblogs.com/ifantastic/p/4565822.html)

#### 问题症状
```-bash: warning: setlocale: LC_ALL: cannot change locale (en_US.utf8)```

#### 解决方法
  本地化是指不同地区用户在键盘上输入不同语言的字符集。例如 en_US 表示美国英语字符集，因此只有正确设置了服务器的字符集，服务器才能理解用户的输入字符。setlocale 命令可以帮助用户来设置本地化字符集。

  出现以上问题是因为服务器无法理解 en_US.UTF-8 字符集，因此首先要在服务器上生成 en_US.UTF-8 字符集：

  ```sudo locale-gen en_US.UTF-8```
  然后使用最新生成的字符集更新本地仓库：

  ```sudo dpkg-reconfigure locales```
  最后更新 /etc/default/locale 文件，例如更新前的文件内容为：

  ```LANG=C.UTF-8```
  更新后：

  ```
  LC_ALL=en_US.UTF-8
  LANG=en_US.UTF-8
  ```
  环境变量 LC_ALL 会覆写所有的本地化设置（在某些情况下 $LANGUAGE 不会被覆写），例如 LC_NAME, LC_ADDRESS 等等。设置 LC_ALL=en_US.UTF-8 表示所有的本地化设置都使用 en_US.UTF-8。

### print显示中文
std.out编码格式问题

- error
```
UnicodeEncodeError: 'ascii' codec can't encode characters
```

#### 解决方式
```python
import sys
import io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
```








