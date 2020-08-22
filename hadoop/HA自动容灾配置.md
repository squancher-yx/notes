# HDFS HA
**在HA的基础上**

a.配置hdfs-site.xml，启用自动容灾.  
[hdfs-site.xml]
```
<property>
  <name>dfs.ha.automatic-failover.enabled</name>
  <value>true</value>
</property>
```
b.配置core-site.xml，指定zk的连接地址.  
[core-site.xml]
```
<property>
  <name>ha.zookeeper.quorum</name>
  <value>s201:2181,s202:2181,s203:2181</value>
</property>
```

在ZooKeeper中初始化所需的状态  
```
[hdfs]$ $HADOOP_HOME/bin/hdfs zkfc -formatZK  
```
由于已在配置中启用了自动故障转移，因此start-dfs.sh脚本现在将在任何运行NameNode的计算机上自动启动ZKFC守护程序。ZKFC启动时，它们将自动选择一个NameNode激活。  
如果在群集上手动管理服务，则需要在运行NameNode的每台计算机上手动启动zkfc守护程序。您可以通过运行以下命令启动守护程序：  
```
[hdfs]$ $HADOOP_HOME/bin/hdfs --daemon start zkfc  
```
官方网址  
https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HDFSHighAvailabilityWithQJM.html


# RM HA

1. 配置yarn-site.xml
```
<property>
	<name>yarn.resourcemanager.ha.enabled</name>
	<value>true</value>
</property>
<property>
	<name>yarn.resourcemanager.cluster-id</name>
	<value>cluster1</value>
</property>
<property>
	<name>yarn.resourcemanager.ha.rm-ids</name>
	<value>rm1,rm2</value>
</property>
<property>
	<name>yarn.resourcemanager.hostname.rm1</name>
	<value>s201</value>
</property>
<property>
	<name>yarn.resourcemanager.hostname.rm2</name>
	<value>s206</value>
</property>
<property>
	<name>yarn.resourcemanager.webapp.address.rm1</name>
	<value>s201:8088</value>
</property>
<property>
	<name>yarn.resourcemanager.webapp.address.rm2</name>
	<value>s206:8088</value>
</property>
<property>
	<name>yarn.resourcemanager.zk-address</name>
	<value>s201:2181,s202:2181,s203:2181</value>
</property>
```

2. 使用管理命令  
//查看状态  
```
$>yarn rmadmin -getServiceState rm1  
```
//切换状态到standby  
```
$>yarn rmadmin -transitionToStandby rm1  
 ```
3. 启动yarn集群  
$>start-yarn.sh  
 
4. **hadoop没有启动两个resourcemanager,需要手动启动另外一个**  
```
$>yarn-daemon.sh start resourcemanager  
 ```
