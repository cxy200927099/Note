# python dependency
python本地包的依赖确实有点烦人,案例如下
工程目录树
```
.
├── README.md
├── dependency
│   ├── __init__.py
│   ├── dependency_a.py
│   ├── dependency_b.py
│   └── readme.md
├── test_dependency.py
└── utils
    ├── __init__.py
    ├── commonUtils.py
    └── logUtil.py
```
## 同package下的不同文件引入
如上面工程目录树所示，对于同一个package下的不同文件引入, dependency_b需要引入dependency_a依赖，如下所示
dependency_a.py
```python
def dependency_a():
    print("package dependency A was called")

if __name__ == '__main__':
    dependency_a()
```

dependency_b.py
```python
from dependency_a import *

def dependency_b():
    print("package dependency b was called")
    dependency_a()

if __name__ == '__main__':
    dependency_b()
```
此时在，dependency这个package下执行`python dependency_b`时程序能正常执行，结果如下
```
$ cd dependency
$ python dependency_b.py 
package dependency b was called
package dependency A was called
```

但是在dependency package之外调用 dependency_b 时，程序出现 `ModuleNotFoundError: No module named 'dependency_a'`的错误
test_dependency.py代码如下
```python
from dependency.dependency_b import *

if __name__ == '__main__':
    dependency_b()
```
错误结果:
```
$ python test_dependency.py 
Traceback (most recent call last):
  File "test_dependency.py", line 8, in <module>
    from dependency.dependency_b import *
  File "/Users/chenxingyi/work/python/code/python-toolkit/dependency/dependency_b.py", line 7, in <module>
    from dependency_a import *
ModuleNotFoundError: No module named 'dependency_a'
```

#### 解决方法
对于此类引用，应该以整个工程root目录为主package,修改dependency_b.py 中引入dependency_a的代码,使用全路径的方式导入，具体修改如下
dependency_b.py
```python
# from dependency_a import *
from dependency.dependency_a import *

def dependency_b():
    print("package dependency b was called")
    dependency_a()

if __name__ == '__main__':
    dependency_b()
```
正确执行结果:
```
$ python test_dependency.py 
package dependency b was called
package dependency A was called
```

*注意: 此时如果，再次进入dependency目录下,然后执行`python dependency_b.py`,则会报`ModuleNotFoundError: No module named 'dependency'`的错误 *

所以这就是python比较坑爹的地方


## 不同package下的不同文件引入
与上面同一个文件夹使用一样，当需要引入其他包下的python文件时，最好使用全路径引入;
比如这里 dependency_b中引入了util下的log_utils.py，引入代码为`from utils.logUtil import *`
```python
# from dependency_a import *
# from dependency.dependency_a import *
from utils.logUtil import *

def dependency_b():
    print("init logging")
    init_log()
    logging.info("init done!")
    print("package dependency b was called")
    # dependency_a()

if __name__ == '__main__':
    dependency_b()
```
执行的时候，需要在工程root最外层去调用dependency_b
<root目录>/test_dependency.py
```python
from dependency.dependency_b import *

if __name__ == '__main__':
    dependency_b()
```
执行结果:
```
$ python test_dependency.py
init logging
2020-01-20 16:08:45,936 INFO logUtil.py:18:log init!
2020-01-20 16:08:45,936 INFO dependency_b.py:14:init done!
package dependency b was called
```

## 总结
python依赖，依赖文件最好使用全路径的方式依赖
在写测试代码时，调用python的代码最好放在最外层的目录，如上面所示的test_dependency.py一样

