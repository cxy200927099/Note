
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

## matplot绘图

### 设置绘图比例大小

```python
import matplotlib.pyplot as plt

plt.rcParams['figure.figsize'] = [15, 5] # for square canvas
plt.rcParams['figure.subplot.left'] = 0
plt.rcParams['figure.subplot.bottom'] = 0
plt.rcParams['figure.subplot.right'] = 1
plt.rcParams['figure.subplot.top'] = 1

ax1 = plt.subplot(n_line, 1, 1)
ax1.plot(pitch_1)
ax1.set_title('estimated pitch [Hz]')
ax2 = plt.subplot(n_line, 1, 2)
ax2.plot(pitch_2)
ax3 = plt.subplot(n_line, 1, 3)
ax3.plot(pitch_3)
plt.show()
```

## python后台运行无日志输出问题
在服务器上运行python处理一些耗时的问题时，我们往往都是后台执行，Linux上有下面几种方式
```
nohup python xxx.py &
nohup python xxx.py 2>&1 &
nohup python xxx.py > xxx.log 2>&1 &
```
但是上面几种方式，无论nohup.out 或xxx.log都没有python日志输出，这是因为python执行有缓存输出

### 解决,python后面加上参数 -u
```
nohup python -u xxx.py > xxx.log 2>&1 &
```



