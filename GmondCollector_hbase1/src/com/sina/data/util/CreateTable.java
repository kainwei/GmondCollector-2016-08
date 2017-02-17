package com.sina.data.util;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.protobuf.generated.SnapshotProtos.SnapshotRegionManifest.StoreFile;
import org.apache.hadoop.hbase.regionserver.BloomType;


/**
 * 集群监控后台程序建表
 * 
 * @author jingyi
 *
 */

public class CreateTable {
	
	static String zk_ip="10.39.3.43";
	
	static String namespace="ns_hadoopadmin:";
	
	static String[] columnFamily = { "c1" };
	
	static String[] columnFamily_c1cm = { "c1","cm" };
	
	static String[] columnFamily_c1c2 = { "c1","c2" };
	
	static String[] columnFamily_cf1 = { "cf1" };
	//对key进行hash，表示00~10开头的数据存入一个region，10~20开头的数据存入一个region...
	//这样将数据量大的数据hash成N份,类似mysql的分表。
	static byte[][] province_ip = { 
			"10.73.0".getBytes(),
			"10.39.0".getBytes(),
			"10.39.1".getBytes(),
			"10.39.2".getBytes(),
			"10.39.3".getBytes(),
			"10.39.4".getBytes(),
			"10.39.5".getBytes(),
			"10.39.6".getBytes(),
			"10.39.7".getBytes(),
			"10.39.8".getBytes()
	};
	
	static byte[][] province0 = { 
			
	};
	
	static byte[][] province_cluster = { 
			"master".getBytes(),
			"hadoop".getBytes(),
			"hbase".getBytes(),
			"yz-gw93-105".getBytes(),
			"kafka".getBytes(),
			"scribe-relay_yf".getBytes()
	};
	
	public static void createMonitorTable() throws IOException{

		
		String table_name=namespace+"BigMonitorClusterConfTable";
		Integer ttl=null;
		createTable(table_name, columnFamily_c1cm,province0,ttl);
		
		table_name=namespace+"BigMonitorMetricConfTable";
		ttl=null;
		createTable(table_name, columnFamily_c1c2,province0,ttl);
		
		//---------
		
		table_name=namespace+"BigMonitorMetricDataClusterHour";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province_cluster,ttl);
		
		table_name=namespace+"BigMonitorMetricDataClusterOrig";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province_cluster,ttl);
		
		
		//---------
		
		table_name=namespace+"BigMonitorMetricDataAlert";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province_ip,ttl);
		
		table_name=namespace+"BigMonitorMetricDataHostHour";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province_ip,ttl);
		
		table_name=namespace+"BigMonitorMetricDataHostNoDisplay";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province_ip,ttl);
		
		table_name=namespace+"BigMonitorMetricDataHostOrig";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province_ip,ttl);
		
		//=============================HBASE=======================
		//=========================================================
		table_name=namespace+"BigMonitorMetricDataRegionHeartbeats";
		ttl=24*3600;
		createTable(table_name, columnFamily_cf1,province0,ttl);
		
		table_name=namespace+"HBaseBigMonitorClusterConfTable";
		ttl=null;
		createTable(table_name, columnFamily_c1cm,province0,ttl);
		
		//--------------------
		
		table_name=namespace+"HBaseBigMonitorMetricDataAlert";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province0,ttl);
		
		table_name=namespace+"HBaseBigMonitorMetricDataClusterOrig";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province0,ttl);
		
		table_name=namespace+"HBaseBigMonitorMetricDataClusterHour";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province0,ttl);
		
		
		//-----------------------
		table_name=namespace+"HBaseBigMonitorMetricDataHostHour";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province0,ttl);
		
		table_name=namespace+"HBaseBigMonitorMetricDataHostNoDisplay";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province0,ttl);
		
		table_name=namespace+"HBaseBigMonitorMetricDataHostOrig";
		ttl=30*24*3600;
		createTable(table_name, columnFamily,province0,ttl);
	
		
	}
	
	public static void main(String args[]) throws IOException {
		CreateTable.zk_ip=args[0];
		CreateTable.createMonitorTable();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void createTable(String table_name,String[] columnFamily,byte[][] province99,Integer ttl) throws IOException {
		Configuration config = HBaseConfiguration.create();

		config.set("hbase.zookeeper.quorum",
				zk_ip+":2181");
		config.set("hbase.zookeeper.property.clientPort", "2181");

		HBaseAdmin admin = new HBaseAdmin(config);

		HTableDescriptor tableDesc = new HTableDescriptor(table_name);
		
		for (String tmp : columnFamily) {
			HColumnDescriptor bid = new HColumnDescriptor(tmp);
			bid.setCompressionType(Compression.Algorithm.LZO);
			bid.setBloomFilterType(BloomType.ROW);
			if(ttl!=null){
				bid.setTimeToLive(ttl);
			}
			tableDesc.addFamily(bid);
		}
		admin.createTable(tableDesc, province99);
		System.out.println("table create susccess:"+table_name);
	}
}
