

## pip安装慢的问题
在使用pip安装的某些包的时候，网络巨慢往往只有几KB/s,那是因为我们访问的是国外的服务器
这里更换成国内的如 阿里云 pip 源即可解决慢的问题, [更多国内pip源](https://yq.aliyun.com/articles/652884)
也可以单独安装某个包的时候，指定pip源，如下
```
pip install scrapy -i https://mirrors.aliyun.com/pypi/simple/
```

## python为指定版本安装库
```
python3.7 -m pip install numpy
```
为指定的python3.7版本安装numpy

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


### python进程池

使用方式，参考如下代码:
```python
import typing
import threading
import os
import psutil
import multiprocessing
import time

def getThreadInfo() -> typing.Tuple[str, str]:
    t = threading.currentThread()
    return t.ident, t.name

def getProcessInfo() -> typing.Tuple[str, str]:
    pid = os.getpid()
    p = psutil.Process(pid)
    return p.pid, p.name()

def doSomeWork(msg: str):
    # print("msg:%s" % msg)
    t_id, t_name = getThreadInfo()
    p_id, p_name = getProcessInfo()
    print("doSomeWork receive %s thread id:%d name:%s process id:%d name:%s" % (msg, t_id, t_name, p_id, p_name))
    time.sleep(1)
    print("doSomeWork %s done thread id:%d name:%s process id:%d name:%s" % (msg, t_id, t_name, p_id, p_name))

if __name__ == '__main__':
    t_id, t_name = getThreadInfo()
    p_id, p_name = getProcessInfo()
    print("main start thread id:%d name:%s process id:%d name:%s" %
                 (t_id, t_name, p_id, p_name))
    global pool
    pool = multiprocessing.Pool(processes=3)
    pool.apply_async(doSomeWork, args=("hello",))
    pool.apply_async(doSomeWork, args=("python",))
    pool.apply_async(doSomeWork, args=("world",))

    pool.close()
    pool.join()
    # time.sleep(4)
    # t1 = threading.Thread(target=doThreadFunc)
    # t1.start()
    # t1.join()
    # time.sleep(2)
    print("main quit thread id:%d name:%s process id:%d name:%s" %
                 (t_id, t_name, p_id, p_name))

```
运行结果:
```
/Users/chenxingyi/opt/miniconda3/envs/python_debug/bin/python3.7 /Users/chenxingyi/work/python/code/python-toolkit/utils/commonUtils.py
main start thread id:4513469888 name:MainThread process id:18348 name:python3.7
doSomeWork receive hello thread id:4513469888 name:MainThread process id:18349 name:python3.7
doSomeWork receive python thread id:4513469888 name:MainThread process id:18350 name:python3.7
doSomeWork receive world thread id:4513469888 name:MainThread process id:18351 name:python3.7
doSomeWork hello done thread id:4513469888 name:MainThread process id:18349 name:python3.7doSomeWork python done thread id:4513469888 name:MainThread process id:18350 name:python3.7
doSomeWork world done thread id:4513469888 name:MainThread process id:18351 name:python3.7

main quit thread id:4513469888 name:MainThread process id:18348 name:python3.7

Process finished with exit code 0
```

#### 进程池的坑
上述代码中，如果传给pool的函数的参数个数不对，那么apply_async 函数执行之后，没有任何反馈，传过去的函数也不会被执行
如修改代码
```python
if __name__ == '__main__':
    t_id, t_name = getThreadInfo()
    p_id, p_name = getProcessInfo()
    print("main start thread id:%d name:%s process id:%d name:%s" %
                 (t_id, t_name, p_id, p_name))
    global pool
    pool = multiprocessing.Pool(processes=3)
    # 这里args参数多传了一个，导致doSomeWork不会被执行，但是没有任何错误提示
    pool.apply_async(doSomeWork, args=("hello", "hello1"))
    pool.apply_async(doSomeWork, args=("python",))
    pool.apply_async(doSomeWork, args=("world",))

    ...
```

执行结果:
```
/Users/chenxingyi/opt/miniconda3/envs/python_debug/bin/python3.7 /Users/chenxingyi/work/python/code/python-toolkit/utils/commonUtils.py
main start thread id:4632835520 name:MainThread process id:18414 name:python3.7
doSomeWork receive python thread id:4632835520 name:MainThread process id:18416 name:python3.7
doSomeWork receive world thread id:4632835520 name:MainThread process id:18417 name:python3.7
doSomeWork python done thread id:4632835520 name:MainThread process id:18416 name:python3.7
doSomeWork world done thread id:4632835520 name:MainThread process id:18417 name:python3.7
main quit thread id:4632835520 name:MainThread process id:18414 name:python3.7

Process finished with exit code 0
```

添加error_callback接收错误:
```python
# 添加callback就可以监控到错误的日志
pool.apply_async(doSomeWork, args=("hello", "hello1"), callback=mycallback, error_callback=myerrorcallback)
```
执行结果:
```
/Users/chenxingyi/opt/miniconda3/envs/python_debug/bin/python3.7 /Users/chenxingyi/work/python/code/python-toolkit/utils/commonUtils.py
main start thread id:4441884096 name:MainThread process id:19246 name:python3.7
doSomeWork receive python thread id:4441884096 name:MainThread process id:19248 name:python3.7
doSomeWork receive world thread id:4441884096 name:MainThread process id:19249 name:python3.7
My errorcallback doSomeWork() takes 1 positional argument but 2 were given
doSomeWork python done thread id:4441884096 name:MainThread process id:19248 name:python3.7
doSomeWork world done thread id:4441884096 name:MainThread process id:19249 name:python3.7
main quit thread id:4441884096 name:MainThread process id:19246 name:python3.7

Process finished with exit code 0
```


## pycharm 激活
1. 下载
jetbrains-agent.jar
将其添加到pycharm的安装路径下，我这里mac下pycharm的安装路径如下
```/Applications/PyCharm with Anaconda plugin .app/Contents/bin```

2. 修改pycharm启动vm选项
打开pycharm，具体 Help->Edit Cuctom VM Options...
在最后一行添加如下代码
```
-javaagent:/Applications/PyCharm with Anaconda plugin .app/Contents/bin/jetbrains-agent.jar
```
3. 修改license server
Help->Register
在licens server一栏填入如下地址
```
http://fls.jetbrains-agent.com
```

