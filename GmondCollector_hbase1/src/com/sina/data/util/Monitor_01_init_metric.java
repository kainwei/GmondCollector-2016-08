package com.sina.data.util;



import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * 
 * 导入监控metric配置
 * 
 * @author jingyi
 *
 */
//put到hbase
public class Monitor_01_init_metric extends Configured implements Tool{

	public int run(String args[]) throws IOException {
		String table_name="ns_hadoopadmin:BigMonitorMetricConfTable";
		String zk_ip=args[0];

		
		Configuration config = HBaseConfiguration.create();
  		config.set("hbase.zookeeper.quorum",zk_ip);
  		config.set("hbase.zookeeper.property.clientPort", "2181");
  		setConf(HBaseConfiguration.create(config));
  		Connection connection = null;
  	    Table table = null;
  	    TableName TABLE_NAME=TableName.valueOf(table_name);
        connection = ConnectionFactory.createConnection(getConf());
        table = connection.getTable(TABLE_NAME);
		
		String file_path="doc/metric_new.txt";
		String line="";
		File file = new File(file_path);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int i=0;
		while((line = reader.readLine()) != null){
			String arr[]=line.split("\t",-1);
			
			String metricName=arr[0];
			String metricColumn=arr[1];
			String metricVal=arr[2];
			String family=arr[3];
			
			if(metricColumn.equals("host_list")){
				metricVal="";
			}
			System.out.println(metricName+"\t"+metricColumn+"\t"+metricVal);
			  Put p = new Put(Bytes.toBytes(metricName));
	          p.add(Bytes.toBytes(family), Bytes.toBytes(metricColumn), Bytes.toBytes(metricVal));
			 // table.
	          table.put(p);			
		}
		
		return i;
     	
	}
	
	public static void main(String arg[]) throws Exception{
		
		if(arg.length<2){
			System.out.println("input :  table_name  zookeeper_ip");
		}
		
	    int ret = ToolRunner.run(new Monitor_01_init_metric(), arg);
	    System.exit(ret);
		
	}
}
