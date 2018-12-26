# java中间件系列 #

---
+ [**ElasticSearch**](#0)
	+ [环境](#1)  
	+ [安装配置es](#2)  
	+ [javaAPI创建索引及操作文档](#3)  
	+ [Head插件安装使用](#4)  
	+ [elasticsearch.yml配置文件](#5)  
	+ [es集群搭建及配置](#6)  
	+ [es查询API](#7)  
	+ [项目中es实战](#8)  


+ [**RabbitMQ**](#9)
	+ [RabbitMQ核心概念](#9.1)
	+ [AMQP协议模型与概念](#9.2)
	+ [RabbitMQ安装](#9.3)
	+ [RabbitMQ 生产者-消费者Demo](#9.4)
	+ [Rabbit Exchange详解](#9.5)
	+ [RabbitMQ高级特性](#9.6)
	+ [RabbitMQ 与 SpringAMQP整合](#9.7)
	+ [RabbitMQ 与 SpringBoot整合](#9.8)


+ [**Redis**](#10)
	+ [Redis五种数据结构API](#10.1)
	+ [Redis其他功能](#10.2)

+ [**ZooKeeper**](#11)
---
<a name="0"></a>
## ElasticSearch ##
<a name="1"></a>
1. 环境：centosOS-minimal、jdk1.8、vsftp、es 5.5.2  
<a name="2"></a>
2. 安装配置es
```
高版本的ES，不能以root用户启动，需要创建一个普通用户来启动
相关命令：
adduser esuser
chown -R esuser /es/elasticsearch-5.5.2/
su esuser
chmod 777 /es/elasticsearch-5.5.2/
sh elasticsearch -d <后台运行>
配置外网设备连接：
vi elasticsearch-5.5.2/config/elasticsearch.yml
配置network的 ip 和 http port
[可能会出现的问题]：
[1] max file descriptors [4096] for elasticsearch process is too low, increase to at least [65536]
解决：切换root用户，修改/etc/security/limits.conf文件，添加：
//* hard  nofile  65536
//* soft nofile   65536
[2] max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
解决：切换root用户，修改 /etc/sysctl.conf 文件，添加：
vm.max_map_count=2621441
刷新文件生效：sysctl -p
```
<a name="3"></a>
3. javaAPI创建索引及操作文档
> 单机状态下，增删改查的简单操作<具体见代码，博客会后续补充>
<a name="4"></a>
4. Head插件安装使用
```
1.head插件安装
1.1 安装node.js
1.2 安装git,克隆head插件 [yum install -y git]
1.3 配置elasticsearch.yml,允许head插件访问 [http.cors.enabled: true http.cors.allow-origin: "*"]
1.4 启动：npm run start
2.head插件添加和删除索引
3.head插件添加、修改、删除文档
3.1 PUT: 创建和修改数据
3.2 POST: 创建和修改数据
3.3 GET: 查询数据
3.4 DELETE: 删除数据
4.head插件打开和关闭文档
5.head插件增加索引映射
```
[安装node.js](http://blog.java1234.com/blog/articles/354.html)

<a name="5"></a>
5. elasticsearch.yml配置文件
```
/**
# Cluster
#
# Use a descriptive name for your cluster:
# 集群名称，默认是elasticsearch
# cluster.name: my-application
#
#Node
#
# Use a descriptive name for the node:
# 节点名称，默认从elasticsearch-2.4.3/lib/elasticsearch-2.4.3.jar!config/names.txt中随机选择一个名称
# node.name: node-1 <集群配置时，每个节点的node.name不能相同>
#
# Add custom attributes to the node:
# node.rack: r1
# Discovery
#
# 当启动新节点时，通过这个ip列表进行节点发现，组建集群
# 默认节点列表：
# 127.0.0.1，表示ipv4的回环地址。
#	[::1]，表示ipv6的回环地址

# 在es1.x中默认使用的是组播(multicast)协议，默认会自动发现同一网段的es节点组建集群，
# 在es2.x中默认使用的是单播(unicast)协议，想要组建集群的话就需要在这指定要发现的节点信息了。
# 注意：如果是发现其他服务器中的es服务，可以不指定端口[默认9300]，如果是发现同一个服务器中的es服务，就需要指定端口了。
# Pass an initial list of hosts to perform discovery when new node is started:
# 
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
# discovery.zen.ping.unicast.hosts: ["host1", "host2"] <集群配置时，配置一个主节点即可>
**/
```
<a name="6"></a>
6. es集群搭建及配置
```
配置集群之前  先把要加群集群的节点的里的data目录下的Node目录 删除，否则集群建立会失败。
集群信息：两台机器IP分别是 192.168.80.100 和 192.168.80.101

100机器：
/**
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
#
node.name: node-1
# ---------------------------------- Network -----------------------------------
#
# Set the bind address to a specific IP (IPv4 or IPv6):
#
network.host: 192.168.80.100
#
# Set a custom port for HTTP:
#
http.port: 9200
# --------------------------------- Discovery ----------------------------------
#
# Pass an initial list of hosts to perform discovery when new node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
discovery.zen.ping.unicast.hosts: ["192.168.80.100"] <主节点>
#
**/
101机器：
/**
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
#
node.name: node-2 <节点名称需要区分>
# ---------------------------------- Network -----------------------------------
#
# Set the bind address to a specific IP (IPv4 or IPv6):
#
network.host: 192.168.80.101
#
# Set a custom port for HTTP:
#
http.port: 9200
# --------------------------------- Discovery ----------------------------------
#
# Pass an initial list of hosts to perform discovery when new node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
discovery.zen.ping.unicast.hosts: ["192.168.80.100"] <主节点>
#
**/
```
<a name="7"></a>
7. es查询API
```
1.查询所有数据
2.分页查询
3.排序
4.数据列过滤
5.简单条件查询
6.查询条件高亮显示
[代码中有详细注释]
```
<a name="8"></a>
7. 项目中es实战
```
1.使用bulkAPI，完成了读取本都文件中的数据，建立对应索引，并批量写入到ES集群中
2.使用bulkAPI，完成了从数据库中读取相应表中数据，建立对应索引，并写入到ES集群中
3.配合过滤条件，完成了集群数据的范围查询，并进行对应处理
[详见代码注释]
```
--------


<a name="9"></a>
## RabbitMQ ##
<a name="9.1"></a>
### RabbitMQ相关概念 ###
1. RabbitMQ：基于**AMQP协议**的**跨平台**、**跨语言**的开源消息中间件
2. AMQP: Advanced Message Queuing Protocol 二进制协议，面向应用层协议的一个开放标准、规范
![AMQP](https://i.imgur.com/8xZPxl5.png)

<a name="9.2"></a>
### AMQP协议模型与概念 ###
``` java
1.Server: (Broker) 接受客户端连接 实现AMQP服务
2.Connection: 应用程序与Broker的网络连接
3.Channel: 网络信道 --> 数据交互的关键
4.Message: [properties:消息属性设置<eg:消息优先级设置> + Body：消息实体]
5.Virtual Host: 虚拟主机 用于逻辑隔离、服务划分
6.Exchange: 交换机，接受消息，根据[路由键] 转发消息到绑定的消息队列
7.Binding： Exchange和Queue之间的虚拟连接
8.RoutingKey: 路由键 用于确定路由特定的信息
9.Queue: 消息队列 保存消息并转发给消费者
```
**MQ架构 --> 解耦特性**
![MQ架构](https://i.imgur.com/GGqefIl.jpg)
**MQ消息流转示意图**
![MQ消息流转](https://i.imgur.com/2IvVg1b.jpg)

<a name="9.3"></a>
### RabbitMQ安装 ###
``` java
下载：
ErLang --> socat秘钥依赖 --> rabbitmq
wget www.rabbitmq.com/releases/erlang/erlang-18.3-1.el7.centos.x86_64.rpm
wget http://repo.iotti.biz/CentOS/7/x86_64/socat-1.7.3.2-5.el7.lux.x86_64.rpm
wget www.rabbitmq.com/releases/rabbitmq-server/v3.6.5/rabbitmq-server-3.6.5-1.noarch.rpm

配置文件：
vim /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.5/ebin/rabbit.app
修改密码：loopback_users 中的 <<"guest">>,只保留guest

服务启动和停止：
启动 rabbitmq-server start &
停止 rabbitmqctl app_stop

管理插件(管控台)：rabbitmq-plugins enable rabbitmq_management
访问地址：http://192.168.80.100:15672/
```

<a name="9.4"></a>
### RabbitMQDemo ###
*代码中有详细注释：Producer.java Consumer.java*


<a name="9.5"></a>
### Rabbit Exchange详解 ###

``` java
1.根据路由键将消息路由到对应的队列中
2.主要属性：
		  AutoDelete: 绑定到该excahnge的Queue删除后，自动删除该队列
		  internal: 是否用于RabbitMQ内部使用
		  Arguments: 设置交换机的扩展参数，eg: x-dead-excahnge DLX死信队列
3.主要类型：
		  DirectExchange: 所有消息路由到routingKey对应的queue中 [exchange和queue的 routingKey 要保持一致]
		  TopicExchange: 所有消息路由到可以和routingKey模糊匹配的queue中
						 其中：*匹配单个，#匹配多个
		  FanoutExchange: 不处理任何路由键，直接将消息路由到与exchange绑定的queue上 [性能最高]	
```
*几种exchange类型示意图*
![DirectExchange](https://i.imgur.com/0If8urg.jpg)
![TopicExchange](https://i.imgur.com/x2xYWFW.jpg)
![FanoutExchange](https://i.imgur.com/bKiU3F1.jpg)


<a name="9.6"></a>
### RabbitMQ高级特性 ###

1.消息的可靠性投递
``` java
生产端可靠性投递要求：
				  1.生产端成功发出消息
				  2.MQ节点成功收到该消息
				  3.生产端成功收到MQ的ACK
				  4.错误消息补偿机制
解决方案：
		1.消息落库，对消息状态打标  [见图片注释]
		2.延迟投递消息，做二次检查，二次ACK  [见图片注释]
```
**消息落库**
![消息落库](https://i.imgur.com/PQecwN3.jpg)
**延迟投递消息**
![延迟投递消息](https://i.imgur.com/aH3ahG9.jpg)

2.幂等性：
``` java
避免消费端重复消费消息
实现：
	1.唯一ID + 指纹码 --> 数据库主键去重
    2.利用Redis原子性实现

```
3.Confirm消息确认机制
``` java
主要步骤：
	   1. channel开启确认模式 channel.confirmSelect()
	   2. channel添加监听 channel.addconfirmListener()

```
**Confirm消息确认机制**
![Confirm消息确认机制](https://i.imgur.com/EqeXsyN.jpg)

4.Return消息机制： 处理路由不可达的消息
``` java
主要设置：
		Mandatory: true时 监听不可达消息 并requeue

```
**Return消息机制**
![Return消息机制](https://i.imgur.com/47swTeq.jpg)

5.自定义消费端监听：
``` java
1. 继承DefaultConsumer
2. 复写handleDelivery()
public class MyConsumer extends DefaultConsumer {

	public MyConsumer(Channel channel) {
		super(channel);
	}
	
	// @param deliveryTag the delivery tag -- enevlope.getdeliveryTag() --> 消息唯一ID标签
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
		System.err.println("-----------consume message----------");
		System.err.println("consumerTag: " + consumerTag);
		System.err.println("envelope: " + envelope);
		System.err.println("properties: " + properties);
		System.err.println("body: " + new String(body));
	}

}

```
6. **消费端限流**

``` java
重要： basicConsume需要取消自动ACK
/**
	 @param prefetchSize maximum amount of content (measured in
     * octets) that the server will deliver, 0 if unlimited
     * @param prefetchCount maximum number of messages that the server
     * will deliver, 0 if unlimited
     * @param global true if the settings should be applied to the
     * entire channel rather than each consumer
*/
 channel.basicQos(int prefetchSize, int prefetchCount, boolean global) --consumer
 channel.basicAck(envelope.getDeliveryTag(), false) --自定义consummer
```
7.消费端ACK与重回队列  
8.消息TTL（Time To Live）  
9.死信队列[Dead-Letter-Exchange] DLX
``` java
当消息在一个队列中变成死信之后，它将被DLX重新publish到另一个exchange中 然后路由到与该exchang绑定的队列中
//见Dlx代码注释
触发死信条件：
		   1. 消息被拒绝[basic.reject() basic.nack()] 且 requeue = false
		   2. 消息TTL过期
		   3. 队列长度被占满

```

<a name="9.7"></a>
### RabbitMQ 与 SpringAMQP整合 ###

``` java
1.SpringAMQP用户管理组件 --RabbitAdmin:底层从Spring容器中 获取 exchange、queue、routingKey，内部调用rabbitTempalte的execute()方法

2.SpringAMQP消息模板组件 -- RabbitTemplate--[适配器模式、]

3.SpringAMQP消息容器 - 适配器 - 转化器  -- SimpleMessageListenerContain--MessageAdapter--'XXX'MessageConvert
注意 三者的嵌套关系

详细说明 见代码-->Spring包

```
<a name="9.8"></a>
### RabbitMQ 与 SpringBoot整合 ###

``` java

1.消息生产端：
			开启消息的确认[需要设置消息的全局唯一id:CorrelationData]和返回模式
			开启消息手动签收模式

2.消息消费端：
			开启消息手动签收模式
			设置监听queue个数
3.[消费端一系列注解使用]：
@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = "queue-1", 
			durable="true"),
			exchange = @Exchange(value = "exchange-1", 
			durable="true", 
			type= "topic", 
			ignoreDeclarationExceptions = "true"),
			key = "springboot.*"
			)
	)  
@Payload  
@Headers  
详细说明 见代码-->SpringBootProvider包、SpringBootConsumer包  
```




----------
<a name="10"></a>
## Redis ##
*使用内存存储的单线程NoSQL*
<a name="10.1"></a>
### Redis五种数据结构 ###
1.String结构
``` java
String的value的大小限制为512M
使用场景：缓存、分布式锁、计数器
API:
get key
set key vale
del key

incr key: key自增1 若key不存在 则自增后get(key)=1
desc key: key自减1 若key不存在 则自增后get(key)=-1
incr key k: key自增k
desc key k

set key value：不管key是否存在 都设置
setnx key value: key不存在是 才设置  --add操作[nx: not exits]
set key value xx: key存在 才设置 --update操作
setex key seconds value: 值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位) --分布式锁

m:批量操作
mget key1 key2...    耗时分析：一次网络时间+m次命令时间<时间短> 节约时间
mset key1 value1 key2 value2...

getset key newValue: get在setnewvalue
append key value: 追加元素
slen key
getrange key start end：获取start到end下标对应的value
settrange key index value：设置指定下标对应的值
```
2.hash结构
``` java
K-V结构：k V[field(不可重复) value] --类似mapMap结构或数据库表结构<k-数据表表名，v-数据表内容>
API：
hget key field: 获取field对应的value
hset key field
hdel key field

hexists key field: 判断key是否有field
hlen key: 获取key对应的field数量

hmget key field1 field2 ...
hmset key field1 value1 field2 value2...

//小心使用遍历redis的命令 --redis是单线程的 可能会造成堵塞
hgetall key: 获取key对应的所有filed-value对
hvals key: 获取key对应的所有field对应的value
hkeys key: 获取说有key对应的field

hsetnx key field value
hincrby key field intCounter

```
3.list结构
``` java
k-v结构：k v[有序可重复队列结构]
index方向：从左向右从0开始， 从右向左-1开始
API:
//增
rpush key value1 vaule2 ...: 列表右边插入
lpush key value1 vaule2 ...: 列表左边插入
linsert key before|after value newValue: 在指定值的前|后插入newValue
//删
lpop key：左侧弹出一个value
rpop key：
lrem key count value: 根据count值 从列表中删除所有与value相等的项
/*
count>0 从左向右删除count个与value相等的项
count<0 从右向左删除Math.abs(count)个与value相等的项
count=0 删除所有的
*/
ltrim key satrt end 
//查
lrange key satrt end
lindex key index:获取列表中index位置的元素
llen key: --redis内部计算 时间复杂度O(1)
//改
lset key index newValue

//阻塞操作
blpop key timeout
brpop key timeout

//tips
lpush + lpop = stack
lpush + rpop = queue
lpush + ltrim = capped collection --大小固定的列表 超过固定大小后 新值会覆盖旧值
lpush + brpop = message queue
```
4.set结构
``` java
k-v结构：k, v[item1,item2...无序 不可重复]
//集合间API:
sadd key element
srem key element
sacrd key: 计算集合的大小‘
sismember key value=xxx: 判断xxx是否在集合中
spop Key: 集合中随机弹出一个元素，会破坏集合结构
srandmember key: 获取集合中随机个数元素，不会破坏集合结构
smembers key:获取集合中所有的元素 --遍历操作需要谨慎使用，推荐使用scan
//集合内API:
sdiff key1 key2: 差集
sinter key1 key2: 交集
sunion key1 key2: 并集

tips:
sadd = Tagging
spop/srandmember = random item
sadd + sinter = social graph
```
5.zset(sorted set)结构
``` java
k-v结构：k, v[score value] --有序不可重复集合
API:
zadd key score element(不可重复)
zrem key element
zscore key element: 返回对应元素的分数
zincrby key increScore element: 对应元素分数 增加或减少Math.abs(socre)
zcard key: 返回element个数
zrange key start end [withscore]: 返回索引范围内的升序元素[是否带上分数显示]
zrangebyscore key minScore maxScore
zcount key minScore maxScore: 返回指定分数范围内的个数
zremrangebyrank key start end: 删除指定排名内的升序元素
zremrangebyscore key minScore maxScore: 

集合间API
zunionstore key1 key2
zinterstore key1 key2
```

<a name="10.2"></a>
### Redis其他功能 ###
1.慢查询
*Redis会把命令执行时间超过 slowlog-log-slower-than 的都记录在 Reids 内部的一个列表（list）中，该列表的长度最大为 slowlog-max-len*
**慢查询生命周期**
![慢查询生命周期](https://i.imgur.com/8R3Wymf.jpg)
``` java
慢查询配置
config set|get slow-max-len :慢查询队列最大长度[队列是固定的，超过长度旧的数据会被弹出]
config set|get slow-log-slower=than ;命令执行时间 超过该时间限制
API:
slowlog get[n]
slowlog len
slowlog reset: 清空慢查询队列
```
2.pipeline:流水线功能
![pipeline](https://i.imgur.com/Zo7OINM.jpg)
``` java

//普通循环 --耗时55s
for(int i=0; i<10000; i++) {
	jdeis.hset("hashkey:"+i, "field:"+i, "value:"+i);
}
//使用pipeline将10000调命令 分100条打包一次 --耗时0.6s
for(int i=0; i<100; i++) {
	Pipeline pipeline = jedis.pipelined();
	for(int j=i*100; j<(i+1)*100; j++) {
		pipeline.hset("hashkey:"+j, "field:"+j, "value:"+j);
	}
	pipeline.synncAndReturnAll();
}

原生M命令[mget\mset]是原子的
pipeline 则会将大批量命令 拆分成小批量命令 非原子的 需要加锁
```
3.发布/订阅模式
``` java
发布/订阅：发布者发布消息到channel上，所欲监听该channel的订阅者 都会接收到该消息
消息队列： 所有对channel监听的订阅者 对channel的消息 会有竞争机制 不会每个订阅者都接收到消息

API:
publish channel message
subscribe [channel]
unsubscribe [channel]
```
4.BitMap：位图 对value中的字节位 进行操作 可用于独立用户的统计
![BitMap](https://i.imgur.com/0GlxYbu.jpg)






