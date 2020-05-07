[TOC]

# prometheus
prometheus是一个开源的分布式日志统计及监控报警系统，所有的数据都以时间序列存储，每一个时间序列是唯一标识的(由metric名字和称为标签的可选键值对组成)
- 例子
一个记录所有http请求数量的，label为method="POST"和handler="/messages"的时间序列如下
api_http_requests_total{method="POST", handler="/messages"}


## 功能
- 多维 数据模型（时序由 metric 名字和 k/v 的 labels 构成）。
- 灵活的查询语句（PromQL）。
- 无依赖存储，支持 local 和 remote 不同模型。
- 采用 http 协议，使用 pull 模式，拉取数据，简单易懂。
- 监控目标，可以采用服务发现或静态配置的方式。
- 支持多种统计数据模型，图形化友好

## 核心组件
- Prometheus Server， 主要用于抓取数据和存储时序数据，另外还提供查询和 Alert Rule 配置管理。
- client libraries，用于对接 Prometheus Server, 可以查询和上报数据。
- push gateway ，用于批量，短期的监控数据的汇总节点，主要用于业务数据汇报等。
- 各种汇报数据的 exporters ，例如汇报机器数据的 node_exporter, 汇报 MongoDB 信息的 MongoDB exporter 等等。
- 用于告警通知管理的 alertmanager

## metric types
- Counter
表示收集的数据某一阶段是单调递增的，比如可以表示一段时间的所有请求数，成功请求数，失败请求数

- Gauge
表示收集的数据趋势可能增加，可能减少；常用于比如温度的变化，当前内存使用率，并发请求数

- Histogram
主要用于表示一段时间范围内对数据进行采样（通常是请求持续时间或响应大小），并能够对其指定区间以及总数进行统计，通常它采集的数据展示为直方图

- Summary

[Histogram vs Summary](https://blog.csdn.net/wtan825/article/details/94616813)

