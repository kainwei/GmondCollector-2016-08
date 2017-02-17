package com.sina.data.bigmonitor.hbase_monitor.Parser;

import com.google.gson.Gson;
import com.sina.data.bigmonitor.collector_hbase.Cluster_HBaseRegion;
import com.sina.data.bigmonitor.hbase_monitor.model.Beans;
import com.sina.data.bigmonitor.hbase_monitor.model.HBaseRegion;
import com.sina.data.bigmonitor.metric.MetricConf;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.log4j.Logger;

import java.io.IOException;

public class HBaseJsonParser {
	/**
	 * 通过hbase http接口获取hbase region最新的metric信息
	 * 
	 */
	
	public static final Logger LOG = Logger.getLogger(HBaseJsonParser.class
			.getName());
	private static Configuration conf = null;
	private MetricConf mc = null;
	private Cluster_HBaseRegion currCluster = null;
	private String currentClusterName = null;
	private String hbase_master_jmx_url="";

	public HBaseJsonParser(Configuration c,MetricConf mc,String hbase_master_jmx_url) {
		this.conf = c;
		this.mc = mc;
		this.hbase_master_jmx_url=hbase_master_jmx_url;
		LOG.info(this.conf.get("hbase.zookeeper.quorum"));
	}
	
	
	synchronized public void Parser(){
		try {
			//conf.get("hbase-cluster_name")
			currentClusterName=this.conf.get("bigmonitor.metricconf.hbase.currentClusterName");
			currCluster=new Cluster_HBaseRegion(currentClusterName, mc, conf);
			currCluster.setLocalTime(System.currentTimeMillis()/1000);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//get json data
		 HttpClient httpClient = new HttpClient();
         String responseBodyAsStream = "";
         GetMethod getMethod = null;
         try{
         getMethod = new GetMethod(hbase_master_jmx_url+"/jmx?qry=hadoop:service=Master,name=Master");
         getMethod.setRequestHeader("Connection", "close");
         httpClient.executeMethod(getMethod);
         responseBodyAsStream = getMethod.getResponseBodyAsString();
         }catch(Exception e){
        	 e.printStackTrace();
         }
         //parser json data
         Gson gson=new Gson();  
         Beans beans=gson.fromJson(responseBodyAsStream, Beans.class);
      //   System.out.println(beans.getBeans()[0].getRegionServers()[0].getValue().getRegionsLoad()[0].getValue().getNameAsString());
         
     //    LOG.info("responseBodyAsStream:"+responseBodyAsStream);
         
         int res_size=Put2RegionTable(beans);//往heartbeat表中存入新数据
         
         
         if(res_size>0){
        	 try {
     			currCluster.thisRoundFinished(true);
     		} catch (IOException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     			LOG.error("Error happens", e);
     		}
         }
         
        
	}
	
	public static void main(String arg[]) throws IOException{
	//	System.out.println("bbbb8");
		Configuration config = HBaseConfiguration.create();
		//config.set("hbase.zookeeper.quorum","10.39.1.57,10.39.1.59,10.39.1.60,10.39.1.62,10.39.1.63");
		config.set("hbase.zookeeper.quorum","10.39.6.85,10.39.6.86,10.39.6.87");
		config.set("hbase.zookeeper.property.clientPort", "2181");
		config.set("bigmonitor.metricconf.tablename","BigMonitorMetricConfTable");
		config.set("bigmonitor.metricdata.prefixname","HBaseBigMonitorMetricData");
		config.set("bigmonitor.hbase.jmx.url","http://mis20254.hadoop.data.sina.com.cn:60010");
		config.set("bigmonitor.clusterconf.tablename","HBaseBigMonitorClusterConfTable");
		config.set("bigmonitor.metricconf.hbase.currentClusterName","HBase70");
		config.set("bigmonitor.metricdata.alertprefixname","BigMonitorMetricData");
		MetricConf mc = new MetricConf(config);
		
		
		System.out.println(mc.getDescriptor("hbase.region.readRequestsCount").getID());
		String hbase_master_jmx_url=config.get("bigmonitor.hbase.jmx.url");
		
		
		HBaseJsonParser hbaseJsonParser = new HBaseJsonParser(config,  mc,hbase_master_jmx_url);
		hbaseJsonParser.Parser();
		
		
	}
	
	public int Put2RegionTable(Beans beans){
		
		
		HBaseRegion hbregion=null;
		try {
			
			hbregion = new HBaseRegion(conf,mc,currentClusterName,currCluster);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hbregion.Put2RegionTable(beans);
	}
	
}
