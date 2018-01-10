package com.spring4all.spring.boot.starter.hbase.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;


/**
 *
 * @author yankai913@gmail.com
 * @date 2014-4-28
 */
public class SimpleClient {

    static final String rowKey = "row1";
    static HBaseAdmin    hBaseAdmin;
    static Configuration conf;
    static String hbaseHost="iZ2ze1yuiqqq6vpxin7dcmZ";

    static {
        conf = HBaseConfiguration.create();
        conf.setInt("timeout", 120000);
        //conf.set("hbase.master", hbaseHost + ":16010");
        conf.set("hbase.rootdir", "/home/hbase");
        conf.set("hbase.zookeeper.quorum",hbaseHost);
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        try {
            hBaseAdmin = new HBaseAdmin(conf);
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createTable(String tableName, String[] columns) throws Exception {
        //dropTable(tableName);
        HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
        for (String columnName : columns) {
            HColumnDescriptor column = new HColumnDescriptor(columnName);
            hTableDescriptor.addFamily(column);
        }
        hBaseAdmin.createTable(hTableDescriptor);
        System.out.println("create table successed");
    }

    public static void dropTable(String tableName) throws Exception {
        if (hBaseAdmin.tableExists(tableName)) {
            hBaseAdmin.disableTable(tableName);
            hBaseAdmin.deleteTable(tableName);
        }
        System.out.println("drop table successed");
    }

    public static HTable getHTable(String tableName) throws Exception {
        return new HTable(conf, tableName);
    }

    public static void insert(String tableName, Map<String, String> map) throws Exception {
        HTable hTable = getHTable(tableName);
        byte[] row1 = Bytes.toBytes(rowKey);
        Put p1 = new Put(row1);
        for (String columnName : map.keySet()) {
            byte[] value = Bytes.toBytes(map.get(columnName));
            String[] str = columnName.split(":");
            byte[] family = Bytes.toBytes(str[0]);
            byte[] qualifier = null;
            if (str.length > 1) {
                qualifier = Bytes.toBytes(str[1]);
            }
            p1.add(family, qualifier, value);
        }
        hTable.put(p1);
        Get g1 = new Get(row1);
        Result result = hTable.get(g1);
        System.out.println("Get: " + result);
        System.out.println("insert successed");
    }

    public static void delete(String tableName, String rowKey) throws Exception {
        HTable hTable = getHTable(tableName);
        List<Delete> list = new ArrayList<Delete>();
        Delete d1 = new Delete(Bytes.toBytes(rowKey));
        list.add(d1);
        hTable.delete(list);
        Get g1 = new Get(Bytes.toBytes(rowKey));
        Result result = hTable.get(g1);
        System.out.println("Get: " + result);
        System.out.println("delete successed");
    }

    public static void selectOne(String tableName, String rowKey) throws Exception {
        HTable hTable = getHTable(tableName);
        Get g1 = new Get(Bytes.toBytes(rowKey));
        Result result = hTable.get(g1);
        foreach(result);
        System.out.println("selectOne end");
    }

    private static void foreach(Result result) throws Exception {
        for (KeyValue keyValue : result.raw()) {
            StringBuilder sb = new StringBuilder();
            sb.append(Bytes.toString(keyValue.getRow())).append("\t");
            sb.append(Bytes.toString(keyValue.getFamily())).append("\t");
            sb.append(Bytes.toString(keyValue.getQualifier())).append("\t");
            sb.append(keyValue.getTimestamp()).append("\t");
            sb.append(Bytes.toString(keyValue.getValue())).append("\t");
            System.out.println(sb.toString());
        }
    }

    public static void selectAll(String tableName) throws Exception {
        HTable hTable = getHTable(tableName);
        Scan scan = new Scan();
        ResultScanner resultScanner = null;
        try {
            resultScanner = hTable.getScanner(scan);
            for (Result result : resultScanner) {
                foreach(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resultScanner != null) {
                resultScanner.close();
            }
        }
        System.out.println("selectAll end");
    }

    public static void main(String[] args) throws Exception {
        String tableName = "tableTest1";
        String[] columns = new String[] { "column_A", "column_B" };
        createTable(tableName, columns);
        Map<String, String> map = new HashMap<String, String>();
        map.put("column_A", "AAA");
        map.put("column_B:1", "b1");
        map.put("column_B:2", "b2");
        insert(tableName, map);
        selectOne(tableName, rowKey);
        selectAll(tableName);
        delete(tableName, rowKey);
        dropTable(tableName);
    }
}
