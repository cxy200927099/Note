# redis-python
这里主要介绍如何使用python连接redis,需要借助[redis-py](https://github.com/andymccurdy/redis-py)这个lib


## 单机模式直连


## 哨兵模式
假设服务器部署哨兵模式，三台机器，三个哨兵，采用主从模式，8主8从
具体哨兵ip，port和 master_name如下:
```python
# the sentinel ip port list
const.SENTINELS_IP_PORT = [('xx.xx.xx.xx', 1000),
                           ('xx.xx.xx.xx', 1000),
                           ('xx.xx.xx.xx', 1000)]
# the sentinel master name list
const.MASTER_NAMES = ['novel_recommend_01',
                      'novel_recommend_02',
                      'novel_recommend_03',
                      'novel_recommend_04',
                      'novel_recommend_05',
                      'novel_recommend_06',
                      'novel_recommend_07',
                      'novel_recommend_08']

```
redis-py是直接支持哨兵模式的，具体如下
```python
from redis.sentinel import Sentinel
# 初始化哨兵对象，里面具体是根据一批哨兵地址，一个个尝试去连接，如果所有连接都失败，则最终抛出异常
self.sentinel = Sentinel(const.SENTINELS_IP_PORT, socket_timeout=0.1)
# 根据哨兵获取master redis client对象
self.masters_dict = {}
  for _master_name in const.MASTER_NAMES:
    # 根据master的名称获取master client对象，此时返回的redis clien内部已经使用redis pool连接池
    # 其实就是使用了redis命令 SENTINEL get-master-addr-by-name master-name
    _master = self.sentinel.master_for(_master_name, socket_timeout=0.1,
                                      password='你的密码')
    self.masters_dict[_master_name] = _master
```

- 哨兵模式完整代码
```python
#!/usr/bin/python3
# -*- coding: utf-8 -*-
# @Time    : 2020/1/17 10:42 上午
# @Author  : Chenxingyi
# @FileName: redis_util.py

from redis.sentinel import Sentinel
from redis import Redis
import typing


class _const:
    class ConstError(TypeError):
        pass

    class ConstCaseError(ConstError):
        pass

    def __setattr__(self, name, value):
        if name in self.__dict__:
            raise self.ConstError("can't change const %s" % name)
        if not name.isupper():
            raise self.ConstCaseError(
                'const name "%s" is not all uppercase' % name)
        self.__dict__[name] = value


const = _const()
# the sentinel ip port list
const.SENTINELS_IP_PORT = [('xx.xx.xx.xx', 1000),
                           ('xx.xx.xx.xx', 1000),
                           ('xx.xx.xx.xx', 1000)]
# the sentinel master name list
const.MASTER_NAMES = ['novel_recommend_01',
                      'novel_recommend_02',
                      'novel_recommend_03',
                      'novel_recommend_04',
                      'novel_recommend_05',
                      'novel_recommend_06',
                      'novel_recommend_07',
                      'novel_recommend_08']


class RedisUtil(object):
    def __init__(self):
        self.sentinel = Sentinel(const.SENTINELS_IP_PORT, socket_timeout=0.1)
        self.masters_dict = {}
        for _master_name in const.MASTER_NAMES:
            _master = self.sentinel.master_for(_master_name, socket_timeout=0.1,
                                               password='redis密码')
            self.masters_dict[_master_name] = _master

    def get_all_master(self) -> typing.Dict[str, Redis]:
        '''
        get all master redis client object
        :return: a dict contains all master redis client object
        the dict key is the @link{const.MASTER_NAMES}
        '''
        return self.masters_dict

    def set(self, _master_name: str, _key, value,
            ex=None, px=None, nx=False, xx=False):
        return self.masters_dict[_master_name]\
            .set(_key, value, ex=ex, px=px, nx=nx, xx=xx)

    def get(self, _master_name: str, _key) -> typing.Any:
        return self.masters_dict[_master_name].get(_key)



def test_redis():
    print(const.SENTINELS_IP_PORT)
    # this will threw exception(can't change const SENTINELS_IP_PORT)
    # const.SENTINELS_IP_PORT = [('cxy', 2000)]
    # print(const.SENTINELS_IP_PORT)
    for _ip_port in const.SENTINELS_IP_PORT:
        print(_ip_port)
    redis_util = RedisUtil()
    masters_dict = redis_util.get_all_master()
    print("======== test set")
    for (_k, _v) in masters_dict.items():
        masters_dict[_k].set(_k, _k)
        print("set k:%s val:%s to %s" % (_k, _k, _k))

    print("======== test get")
    for (_k, _v) in masters_dict.items():
        _val = masters_dict[_k].get(_k)
        print("get k:%s val:%s to %s" % (_k, _val, _k))

    print("======== test delete")
    for (_k, _v) in masters_dict.items():
        _val = masters_dict[_k].delete(_k)
        print("delete k:%s from %s, ret=%s" % (_k, _k, _val))
        _val = masters_dict[_k].get(_k)
        print("get k:%s val:%s to %s" % (_k, _val, _k))

if __name__ == '__main__':
    test_redis()

```





