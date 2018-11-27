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

+ [**Redis**](#10)

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






