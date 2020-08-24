**Feature**

    Linear and modular scalability.                                        //线性模块化扩展方式。  
    Strictly consistent reads and writes.                        //严格一致性读写  
    Automatic and configurable sharding of tables        //自动可配置表切割  
    Automatic failover support between RegionServers.        //区域服务器之间自动容在  
    Convenient base classes for backing Hadoop MapReduce jobs with Apache HBase tables.          
    Easy to use Java API for client access.                        //java API  
    Block cache and Bloom Filters for real-time queries        //块缓存和布隆过滤器用于实时查询   
    Query predicate push down via server side Filters        //通过服务器端过滤器实现查询预测   
    Thrift gateway and a REST-ful Web service that supports XML, Protobuf, and binary data encoding options    
    Extensible jruby-based (JIRB) shell  
    Support for exporting metrics via the Hadoop metrics subsystem to files or Ganglia; or via JMX       //可视化


1. 配置hbase模式  
    + 本地模式  
      [hbase/conf/hbase-env.sh]
      ```
      EXPORT JAVA_HOME=/soft/jdk
      ```
      [hbase/conf/hbase-site.xml]
      ```
      <property>
        <name>hbase.rootdir</name>
        <value>file:/home/hadoop/HBase/HFiles</value>
      </property>
      ```

    + 伪分布式  
      [hbase/conf/hbase-env.sh]
      ```
      EXPORT JAVA_HOME=/soft/jdk
      ```
      [hbase/conf/hbase-site.xml]
      ```
      <property>
        <name>hbase.cluster.distributed</name>
        <value>true</value>
      </property

      <property>
        <name>hbase.rootdir</name>
        <value>hdfs://localhost:8020/hbase</value>
      </property>
      ```
    + 完全分布式   
      [hbase/conf/hbase-env.sh]
      ```
      export JAVA_HOME=/soft/jdk  
      export HBASE_MANAGES_ZK=false
      ```
      [hbse-site.xml]
      ```
      <!-- 使用完全分布式 -->
      <property>
        <name>hbase.cluster.distributed</name>
        <value>true</value>
      </property>

      <!-- 指定hbase数据在hdfs上的存放路径 -->
      <property>
        <name>hbase.rootdir</name>
        <value>hdfs://s201:8020/hbase</value>
      </property>
      <!-- 配置zk地址 -->
      <property>
        <name>hbase.zookeeper.quorum</name>
        <value>s201:2181,s202:2181,s203:2181</value>
      </property>
      <!-- zk的本地目录 -->
      <property>
        <name>hbase.zookeeper.property.dataDir</name>
        <value>/home/centos/zookeeper</value>
      </property>
      ```
      
2. 配置regionservers  
    [hbase/conf/regionservers]  
    s202  
    s203  
    s204
 
3. 启动hbase集群(s201)  
    $>start-hbase.sh 
 
4. 登录hbase的webui  
    http://s201:16010

5. 启动命令（详细见脚本分析）
    start-hbase.sh
    hbase-daemon.sh start master
    hbase-daemons.sh start regionserver

6. hbase的ha设置
    启动多个master即可。  
    配置 conf/backup-master  
    官方文档：  
    http://hbase.apache.org/book.html#quickstart_pseudo

**hbase基于hdfs**

相同列族的数据存放在一个文件中。  
[表数据的存储目录结构构成]  
hdfs://s201:8020/hbase/data/${名字空间}/${表名}/${区域名称}/${列族名称}/${文件名}  
<br>
[WAL目录结构构成]  
hdfs://s201:8020/hbase/WALs/${区域服务器名称,主机名,端口号,时间戳}/  
