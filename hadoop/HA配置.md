**HA高可用配置**

&nbsp;&nbsp;&nbsp;&nbsp;high availability,高可用.  
&nbsp;&nbsp;&nbsp;&nbsp;两个名称节点，一个active(激活态)，一个是standby(slave待命),slave节点维护足够多状态以便于容灾。  
&nbsp;&nbsp;&nbsp;&nbsp;和客户端交互的active节点,standby不交互.  
&nbsp;&nbsp;&nbsp;&nbsp;两个节点都和JN守护进程构成组的进行通信。  
&nbsp;&nbsp;&nbsp;&nbsp;数据节点配置两个名称节点，分别报告各自的信息。  
&nbsp;&nbsp;&nbsp;&nbsp;同一时刻只能有一个激活态名称节点。  
&nbsp;&nbsp;&nbsp;&nbsp;脑裂:两个节点都是激活态。  
&nbsp;&nbsp;&nbsp;&nbsp;为防止脑裂，JNs只允许同一时刻只有一个节点向其写数据。容灾发生时，成为active节点的namenode接管  
&nbsp;&nbsp;&nbsp;&nbsp;向jn的写入工作。  

**硬件资源**

&nbsp;&nbsp;&nbsp;&nbsp;名称节点:硬件配置相同。  
&nbsp;&nbsp;&nbsp;&nbsp;N节点:轻量级进程，至少3个节点,允许挂掉的节点数 (n - 1) / 2.  
&nbsp;&nbsp;&nbsp;&nbsp;不需要再运行辅助名称节点。

**配置文件细节**

1. 配置nameservice   
[hdfs-site.xml]

```
<property>
	<name>dfs.nameservices</name>
	<value>mycluster</value>
</property>
```

2. dfs.ha.namenodes.[nameservice ID]  
[hdfs-site.xml]

```
<!-- myucluster下的名称节点两个id -->
<property>
	<name>dfs.ha.namenodes.mycluster</name>
	<value>nn1,nn2</value>
</property>
```

3. dfs.namenode.rpc-address.[nameservice ID].[name node ID]  
[hdfs-site.xml]

```
<property>
	<name>dfs.namenode.rpc-address.mycluster.nn1</name>
	<value>s201:8020</value>
</property>
<property>
	<name>dfs.namenode.rpc-address.mycluster.nn2</name>
	<value>s206:8020</value>
</property>
```

4. dfs.namenode.http-address.[nameservice ID].[name node ID]  
配置webui端口  

```
<property>
	<name>dfs.namenode.http-address.mycluster.nn1</name>
	<value>s201:50070</value>
</property>
<property>
	<name>dfs.namenode.http-address.mycluster.nn2</name>
	<value>s206:50070</value>
</property>
```

5. dfs.namenode.shared.edits.dir  
名称节点共享编辑目录  
[hdfs-site.xml]

```
<property>
	<name>dfs.namenode.shared.edits.dir</name>
	<value>qjournal://s202:8485;s203:8485;s204:8485/mycluster</value>
</property>
```

6. dfs.client.failover.proxy.provider.[nameservice ID]  
java类，client使用它判断哪个节点是激活态。  
[hdfs-site.xml]

```
<property>
	<name>dfs.client.failover.proxy.provider.mycluster</name>
	<value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider
	</value>
</property>
```

7. dfs.ha.fencing.methods  
脚本列表或者java类，在容灾保护激活态的nn.  
[hdfs-site.xml]

```
<property>
	<name>dfs.ha.fencing.methods</name>
	<value>
		sshfence
		shell(/bin/true)
	</value>
</property>
 
<property>
	<name>dfs.ha.fencing.ssh.private-key-files</name>
	<value>/home/centos/.ssh/id_rsa</value>
</property>
```
8. fs.defaultFS  
配置hdfs文件系统名称服务。  
[core-site.xml]

```
<property>
	<name>fs.defaultFS</name>
	<value>hdfs://mycluster</value>
</property>
```
 
9. dfs.journalnode.edits.dir  
配置JN存放edit的本地路径。  
[hdfs-site.xml]

```
<property>
	<name>dfs.journalnode.edits.dir</name>
	<value>/home/centos/hadoop/journal</value>
</property>
```

**部署细节**
1. 在jn节点分别启动jn进程
+ $>hadoop-daemon.sh start journalnode
 
2. 启动jn之后，在两个NN之间进行disk元数据同步
+ a)如果是全新集群，先format文件系统,只需要在一个nn上执行。  
  [s201]  
  $>hadoop namenode -format

+ b)如果将非HA集群转换成HA集群，复制原NN的metadata到另一个nn.
  + 1).步骤一  
    [s201]  
    $>scp -r /home/centos/hadoop/dfs centos@s206:/home/centos/hadoop/  <br>

  + 2).步骤二  
    在新的nn(未格式化的nn)上运行一下命令，实现待命状态引导。
    [s206]
    $>hdfs namenode -bootstrapStandby                //需要s201为启动状态,提示是否格式化,选择N.
 
  + 3).在一个NN上执行以下命令，完成edit日志到jn节点的传输。  
    $>hdfs namenode -initializeSharedEdits  
    #查看s202,s203是否有edit数据.
 
  + 4).启动所有节点.
    [s201]
    $>hadoop-daemon.sh start namenode                //启动名称节点  
    $>hadoop-daemons.sh start datanode                //启动所有数据节点 <br>
    [s206]  
    $>hadoop-daemon.sh start namenode                //启动名称节点

**HA管理**

```
$>hdfs haadmin -transitionToActive nn1                                ··//切成激活态  
$>hdfs haadmin -transitionToStandby nn1                                //切成待命态  
$>hdfs haadmin -transitionToActive --forceactive nn2//强行激活  
$>hdfs haadmin -failover nn1 nn2                                        //模拟容灾演示,从nn1切换到nn2  
```

| s11 | s12 | s13 | s14 | s15 | s16 |
| :-- | :-- | :-- | :-- | :-- | :-- |
| NN<br>ZK<br>ZKFC<br>RM | ZK<br>JN<br>DN<br>NM | ZK<br>JN<br>DN<br>NM | JN<br>DN<br>NM | DN<br>NM | NN<br>ZKFC<br>RM |
