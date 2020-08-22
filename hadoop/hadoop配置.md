1. **独立模式(standalone|local)**   
nothing!  
本地文件系统。  
不需要启用单独进程。  

2. **pesudo(伪分布模式)**  
等同于完全分布式，只有一个节点。  
ssh配置
[配置文件]  
core-site.xml        //fs.defaultFS=hdfs://localhost/  
hdfs-site.xml        //replication=1  
mapred-site.xml        //  
yarn-site.xml        //  
a)进入${HADOOP_HOME}/etc/hadoop目录  
b)编辑core-site.xml  
    ```
    <?xml version="1.0"?>
    <configuration>
      <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost/</value>
      </property>
    </configuration>
    ```
    c)编辑hdfs-site.xml  
    ```
    <?xml version="1.0"?>
    <configuration>
      <property>
        <name>dfs.replication</name>
        <value>1</value>
      </property>
    </configuration>
    ```
    d)编辑mapred-site.xml  
    注意:cp mapred-site.xml.template mapred-site.xml  
    ```
    <?xml version="1.0"?>
    <configuration>
      <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
      </property>
    </configuration>
    ```
    e)编辑yarn-site.xml  
    ```
    <?xml version="1.0"?>
    <configuration>
      <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>localhost</value>
      </property>
      <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
      </property>
    </configuration>
     ```
    f)配置SSH  
    1)检查是否安装了ssh相关软件包(openssh-server + openssh-clients + openssh)  
    $yum list installed | grep ssh  
    2)检查是否启动了sshd进程  
    $>ps -Af | grep sshd  
    3)在client侧生成公私秘钥对。  
    $>ssh-keygen -t rsa -P '' -f \~/.ssh/id_rsa   
    4)生成~/.ssh文件夹，里面有id_rsa(私钥) + id_rsa.pub(公钥)   
    5)公钥  
    ssh-copy-id -i ~/.ssh/id_rsa.pub localhost

3. **完全分布式**  
在s201主机上生成密钥对  
$>ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa  
将s201的公钥文件id_rsa.pub远程复制到202 ~ 204主机上。  
并放置/home/centos/.ssh/authorized_keys  
    ```
    ~$>scp id_rsa.pub centos@s201:/home/centos/.ssh/authorized_keys~  
    ~$>scp id_rsa.pub centos@s202:/home/centos/.ssh/authorized_keys~  
    ~$>scp id_rsa.pub centos@s203:/home/centos/.ssh/authorized_keys~  
    ~$>scp id_rsa.pub centos@s204:/home/centos/.ssh/authorized_keys~  
    ssh-copy-id -i ~/.ssh/id_rsa.pub admin@Cluster-02  
    ```
    [core-site.xml]  
    ```
    <?xml version="1.0" encoding="UTF-8"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
    <configuration>
      <property>
        <name>fs.defaultFS</name>
        <value>hdfs://s201/</value>
      </property>
    </configuration>
    ```
    [hdfs-site.xml]
    ```
    <?xml version="1.0" encoding="UTF-8"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
    <configuration>
      <property>
        <name>dfs.replication</name>
        <value>3</value>
      </property>
    </configuration>
    ```

    [mapred-site.xml]
    不变

    [yarn-site.xml]
    ```
    <?xml version="1.0"?>
    <configuration>
      <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>s201</value>
      </property>
      <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
      </property>
    </configuration>
    ```

    [slaves]
    s202
    s203
    s204

    [hadoop-env.sh]
    ...
    export JAVA_HOME=/soft/jdk
    ...

    分发配置
    $>cd /soft/hadoop/etc/
    $>scp -r full centos@s202:/soft/hadoop/etc/
    $>scp -r full centos@s203:/soft/hadoop/etc/
    $>scp -r full centos@s204:/soft/hadoop/etc/
