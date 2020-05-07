[TOC]

# go base

## map

- map vs sync.map
map: 官方的map类型，不支持并发读写
sync.map: 官方出品支持并发读写的map，在读多写少时性能比较好，写多的时候性能比较差

- [concurrent-map](https://github.com/orcaman/concurrent-map/blob/master/README-zh.md)
concurrent-map提供了一种高性能的解决方案:通过对内部map进行分片，降低锁粒度，从而达到最少的锁等待时间(锁冲突)

- benchtest




