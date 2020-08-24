import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
 
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
public class put_get_other {
    public static Configuration configuration;
 
    static {
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "192.168.10.14");
    }
    @Test
    public void put() throws Exception {
        //创建conf对象
        Configuration conf = HBaseConfiguration.create();
        //通过连接工厂创建连接对象
        Connection conn = ConnectionFactory.createConnection(conf);
        //通过连接查询tableName对象
        TableName tname = TableName.valueOf("ns1:t1");
        //获得table
        Table table = conn.getTable(tname);
 
        //通过bytes工具类创建字节数组(将字符串)
        byte[] rowid = Bytes.toBytes("row3");
 
        //创建put对象
        Put put = new Put(rowid);
 
        byte[] f1 = Bytes.toBytes("f1");
        byte[] id = Bytes.toBytes("id");
        byte[] value = Bytes.toBytes(102);
        put.addColumn(f1, id, value);
 
        //执行插入
        table.put(put);
    }
 
    @Test
    public void bigInsert() throws Exception {
 
        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0000");
        long start = System.currentTimeMillis();
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
//        HTable table = (HTable) conn.getTable(tname);
        BufferedMutator table = conn.getBufferedMutator(TableName.valueOf("ns1:t1"));
        List<Mutation> mutations = new ArrayList<Mutation>();
        for (int i = 1; i < 10000; i++) {
            Put put = new Put(Bytes.toBytes("row" + format.format(i)));
            //关闭写前日志
            //Deprecated
            //put.setWriteToWAL(false);
            //Mutation可以，put父类
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("id"), Bytes.toBytes(i));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("name"), Bytes.toBytes("tom" + i));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("age"), Bytes.toBytes(i % 100));
            mutations.add(put);
        }
        table.mutate(mutations);
        //
        System.out.println(System.currentTimeMillis() - start);
    }
 
    @Test
    public void get() throws Exception {
        //创建conf对象
        Configuration conf = HBaseConfiguration.create();
        //通过连接工厂创建连接对象
        Connection conn = ConnectionFactory.createConnection(conf);
        //通过连接查询tableName对象
        TableName tname = TableName.valueOf("ns1:t1");
        //获得table
        Table table = conn.getTable(tname);
//        HTable table = (HTable) conn.getTable(tname);
        //通过bytes工具类创建字节数组(将字符串)
        byte[] rowid = Bytes.toBytes("row3");
        Get get = new Get(Bytes.toBytes("row3"));
        Result r = table.get(get);
        byte[] idvalue = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("id"));
        System.out.println(Bytes.toInt(idvalue));
    }
 
    @Test
    public void formatNum() {
        DecimalFormat format = new DecimalFormat();
        format.applyPattern("0000000");
        System.out.println(format.format(8));
 
    }
 
    @Test
    public void createNameSpace() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        Admin admin = conn.getAdmin();
        //创建名字空间描述符
        NamespaceDescriptor nsd = NamespaceDescriptor.create("ns2").build();
        admin.createNamespace(nsd);
 
        NamespaceDescriptor[] ns = admin.listNamespaceDescriptors();
        for (NamespaceDescriptor n : ns) {
            System.out.println(n.getName());
        }
    }
 
    @Test
    public void listNameSpaces() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        Admin admin = conn.getAdmin();
 
        NamespaceDescriptor[] ns = admin.listNamespaceDescriptors();
        for (NamespaceDescriptor n : ns) {
            System.out.println(n.getName());
        }
    }
 
    @Test
    public void createTable() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        Admin admin = conn.getAdmin();
        //创建表名对象
        TableName tableName = TableName.valueOf("ns2:t2");
        //创建表描述符对象
        TableDescriptorBuilder  tbl  =TableDescriptorBuilder.newBuilder(tableName);
//        HTableDescriptor tbl = new HTableDescriptor(tableName);
        //创建列族描述符
        ColumnFamilyDescriptorBuilder col =  ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes("user"));
//        HColumnDescriptor col = new HColumnDescriptor("f1");
// 获得列描述起
        ColumnFamilyDescriptor  cfd = col.build();
        tbl.setColumnFamily(cfd);
//获得表描述器
        TableDescriptor td = tbl.build();
        admin.createTable(td);
        System.out.println("over");
    }
 
    @Test
    public void disableTable() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        Admin admin = conn.getAdmin();
        //禁用表 enable(...) disableTable(...)
        admin.deleteTable(TableName.valueOf("ns2:t2"));
    }
 
 
    @Test
    public void deleteData() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
 
        Table table = conn.getTable(tname);
        Delete del = new Delete(Bytes.toBytes("row0001"));
        del.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("id"));
        del.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("name"));
        table.delete(del);
        System.out.println("over");
    }
 
    /**
     * 删除数据
     */
    @Test
    public void scan() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
//        scan.setStartRow(Bytes.toBytes("row5000"));
//        scan.setStopRow(Bytes.toBytes("row8000"));
        scan.withStartRow(Bytes.toBytes("row5000"));
        scan.withStopRow(Bytes.toBytes("row8000"));
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            byte[] name = r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"));
            System.out.println(Bytes.toString(name));
        }
    }
 
    /**
     * 动态遍历
     */
    @Test
    public void scan2() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        Connection conn = ConnectionFactory.createConnection(conf);
        TableName tname = TableName.valueOf("ns1:t1");
        Table table = conn.getTable(tname);
        Scan scan = new Scan();
//        scan.setStartRow(Bytes.toBytes("row5000"));
//        scan.setStopRow(Bytes.toBytes("row8000"));
        scan.withStartRow(Bytes.toBytes("row5000"));
        scan.withStopRow(Bytes.toBytes("row8000"));
        ResultScanner rs = table.getScanner(scan);
        Iterator<Result> it = rs.iterator();
        while (it.hasNext()) {
            Result r = it.next();
            Map<byte[], byte[]> map = r.getFamilyMap(Bytes.toBytes("f1"));
            for (Map.Entry<byte[], byte[]> entrySet : map.entrySet()) {
                String col = Bytes.toString(entrySet.getKey());
                String val;
                if (col.equals("age") || col.equals("id"))
                    val = String.valueOf(Bytes.toInt(entrySet.getValue()));
                else
                    val = Bytes.toString(entrySet.getValue());
                System.out.print(col + ":" + val + ",");
            }
            System.out.println();
        }
    }
}
