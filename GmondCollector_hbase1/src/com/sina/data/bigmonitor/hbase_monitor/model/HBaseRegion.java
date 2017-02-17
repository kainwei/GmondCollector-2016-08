package com.sina.data.bigmonitor.hbase_monitor.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.mortbay.log.Log;

import com.sina.data.bigmonitor.collector_hbase.Cluster_HBaseRegion;
import com.sina.data.bigmonitor.collector_hbase.HBaseRegionVal;
import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;
import com.sina.data.bigmonitor.metric.MetricDescriptor;

public class HBaseRegion {
	private MetricConf mc = null;
	private String currentClusterName = null;
	private Cluster_HBaseRegion currCluster = null;
	private HTable hbaseregionHeartbeatTable = null;// key:ip_regionname_timestamp,val: value

	public HBaseRegion(Configuration conf, MetricConf mc,
			String currentClusterName,Cluster_HBaseRegion currCluster) throws IOException {
		this.mc = mc;
		this.currentClusterName = currentClusterName;
		this.currCluster=currCluster;
		setupHBaseConn(conf);
	}

	private HTable realSetupHTable(Configuration conf, String name)
			throws IOException {
		HTable t = new HTable(conf, name);
		t.setAutoFlush(false, true);
		t.setWriteBufferSize(BigMonitorConstants.HTableWriteBufferSize);
		return t;
	}

	private void setupHBaseConn(Configuration conf) throws IOException {
		if (this.hbaseregionHeartbeatTable == null) {
			this.hbaseregionHeartbeatTable = realSetupHTable(conf, conf.get(
					BigMonitorConstants.HBaseRegionHeartbeatTableKey,
					BigMonitorConstants.HBaseRegionHeartbeatTableName));
		}
	}

	
	//put new val
	public int Put2RegionTable(Beans beans) {
		List<Put> list = new ArrayList<Put>();
		int flag=0;
		// roll RegionServer
		for (RegionServerBean rsb : beans.getBeans()[0].getRegionServers()) {
			
			 String ip=rsb.getKey().split(",")[0];
			 int load=rsb.getValue().getLoad();
			//roll regions
			for (RegionBean rb : rsb.getValue().getRegionsLoad()) {
				RegionDetailBean rbdetail = rb.getValue();
				// BigMonitorMetricDataRegionHeartbeats version=2 save 2 data recently
				String regionname = rbdetail.getNameAsString();
				regionname=regionname.replace(",","#");//逗号替换为# 防止后续有根据逗号进行split的操作
				
				String cluster_regionname=currentClusterName + "-"	+ regionname;
				
				
				//roll region metrics
				for (String param : RegionDetailBean.param_arr) {//RegionDetailBean.param_arr中存储的是 json 中的字段名比如：writeCount
				//	System.out.println("hbase.region."+param);
					MetricDescriptor md=mc.getDescriptor("hbase.region."+param);
					if(md==null){
						continue;
					}
					
					long metricId = md.getID();
				//	String type=md.getType();
					String key=cluster_regionname + "_" + metricId;
				//获取metric 信息,  key初始化结束	------------------------------
					
					//get last time data------------------------------
					Get get = new Get(Bytes.toBytes(key));
					get.addColumn(Bytes.toBytes("cf1"),Bytes.toBytes("val"));//  metric data
					//get.setMaxVersions(2);
					Result result=null;
					try {
						result = hbaseregionHeartbeatTable.get(get);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					List<KeyValue> reslist = result.list();
					Double val=new Double("0");
					long timestamp=0;
					boolean isLastRow=false;
					if(reslist!=null){
						for (KeyValue kv : reslist) {
						//	if(type.equals("double")){
								val=new Double(Bytes.toString(kv.getValue()));
								timestamp=kv.getTimestamp()/1000;
								isLastRow=true;
						//	}
			   			}
					}
					//get last time data end -----------------------------
					//
					Put put = new Put(Bytes.toBytes(key));
					put.add(Bytes.toBytes("cf1"), Bytes.toBytes("val"), Bytes
							.toBytes(rbdetail.getVal(param)));
					list.add(put);
					
					long new_timestamp=System.currentTimeMillis()/1000;
					
					long dev_timestamp=new_timestamp-timestamp;
					
					if(dev_timestamp>30&&isLastRow){
				//		Log.info("HBaseRegion_here01:"+new_timestamp+"\t"+timestamp+"\t"+(new_timestamp-timestamp));
						continue;
					}
				//	Log.info("HBaseRegion_here02"+new_timestamp+"\t"+timestamp+"\t"+(new_timestamp-timestamp));
					double abs_val=new Double(rbdetail.getVal(param))-val;
					HBaseRegionVal currHost=currCluster.getHost(cluster_regionname);
					currHost.setLastReport(new_timestamp);
					currHost.addMetricValue(metricId, String.valueOf(abs_val),"");
					flag++;
				}//roll region metrics  end --------
			}//roll regions end ------
		}// roll RegionServer end -------
		//put 
		try {
			int res_size=list.size();
			if(res_size>0){
				hbaseregionHeartbeatTable.put(list);
				return flag;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	synchronized public void thisRoundFinished(boolean needToWrite)
			throws IOException {

	}
}
