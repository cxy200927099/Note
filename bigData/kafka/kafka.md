# kafka介绍
Kafka是一种高吞吐量的分布式发布订阅消息系统，用于构建实时流数据管道的应用程序，可以水平扩展，容错性好，速度快的特点

## 核心概念
### Broker
  Kafka集群包含一个或多个服务器，这种服务器被称为broker
### Topic
  每条发布到Kafka集群的消息都有一个类别，这个类别被称为Topic。（物理上不同Topic的消息分开存储，逻辑上一个Topic的消息虽然保存于一个或多个broker上但用户只需指定消息的Topic即可生产或消费数据而不必关心数据存于何处）
  一个topic可以被多个消费者 订阅
### Partition
  Partition是物理上的概念，每个Topic包含一个或多个Partition.
### Producer
  负责发布消息到Kafka broker
### Consumer
  消息消费者，向Kafka broker读取消息的客户端。
### Consumer Group
  每个Consumer属于一个特定的Consumer Group（可为每个Consumer指定group name，若不指定group name则属于默认的group）
### Record
  每一条记录由 一个Key, 一个value和 一个timestamp组成

## 核心API
### The Producer API: 
  allows an application to publish a stream of records to one or more Kafka topics.
### The Consumer API: 
  allows an application to subscribe to one or more topics and process the stream of records produced to them.
### The Streams API: 
  allows an application to act as a stream processor, consuming an input stream from one or more topics and producing an output stream to one or more output topics, effectively transforming the input streams to output streams.
### The Connector API: 
  allows building and running reusable producers or consumers that connect Kafka topics to existing applications or data systems. For example, a connector to a relational database might capture every change to a table.





