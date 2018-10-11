# ElasticSearch知识点及Demo #
---

## es安装 ##
1. 环境：centosOS-minimal、jdk1.8、vsftp、es 5.5.2  
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
3. javaAPI创建索引及操作文档
> 单机状态下，增删改查的简单操作<具体见代码，博客会后续补充>










