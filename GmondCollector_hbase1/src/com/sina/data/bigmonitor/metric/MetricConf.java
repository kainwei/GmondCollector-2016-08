package com.sina.data.bigmonitor.metric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TimerTask;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;

public class MetricConf extends TimerTask {
  public static final Logger LOG = Logger.getLogger(MetricConf.class.getName());
      
  private Configuration conf = null;
  private Table table = null;
  private String tableName;//存储metric配置的hbase表   key:metric_name   value:几个不同的参数，id,host_list等
  private Map<String, MetricDescriptor> name2Descriptor = null;
  private Map<Long, MetricDescriptor> id2Descriptor = null;//metricId对应的metric

  public MetricConf(Configuration conf) throws IOException{
    this.conf  = HBaseConfiguration.create(conf);
    this.tableName = conf.get(BigMonitorConstants.MetricConfTableKey, 
                              BigMonitorConstants.MetricConfDefaultTableName);
    this.name2Descriptor = new HashMap<String, MetricDescriptor>();
    this.id2Descriptor = new HashMap<Long, MetricDescriptor>();
    update();//更新内存中的metric信息
  }
  
  public void update() throws IOException {
	  LOG.info("MetricConf begin update");
    long timeStarted = System.currentTimeMillis();
  		Connection connection = null;		
        connection = ConnectionFactory.createConnection(conf);
 //       System.out.println("conf:"+conf.get("hbase.zookeeper.quorum"));
        table = connection.getTable(TableName.valueOf(tableName));
    
    Map<String, MetricDescriptor> tempN2DMap = new HashMap<String, MetricDescriptor>();
    Map<Long, MetricDescriptor> tempID2DMap = new HashMap<Long, MetricDescriptor>();
    HashSet<String> hosts = new HashSet<String>();
    byte[] CFBytes = Bytes.toBytes(BigMonitorConstants.MetricConfTableCF1);//扫描metric配置全表
    ResultScanner rs = table.getScanner(CFBytes);
    for (Result r : rs){
      NavigableMap<byte[], byte[]> m = r.getFamilyMap(CFBytes);
      String metricName = Bytes.toString(r.getRow());
      long ID=0;
      try {
        ID = Long.parseLong(Bytes.toString(m.get(Bytes.toBytes("id"))));
      } catch (NumberFormatException e) {
        LOG.error(metricName + " has id that is not a number", e);
       // return;
        continue;
      }
      int level = MetricDescriptor.parseLevel(ID);
//      String slopeString = Bytes.toString(m.get(Bytes.toBytes("slope")));
//      boolean positive = slopeString.equals("positive") ? true : false;
      boolean positive = false;
//      String hostString = Bytes.toString(m.get(Bytes.toBytes("host_list")));
      String type = Bytes.toString(m.get(Bytes.toBytes("type")));
      hosts.clear();
/*      for (String ip : hostString.split(",")){
        hosts.add(ip);
      }
      */
      MetricDescriptor md = new MetricDescriptor(metricName, type, ID, level, positive, hosts);
      tempN2DMap.put(metricName, md);
      tempID2DMap.put(ID, md);
    }
    synchronized(this){
      this.name2Descriptor = tempN2DMap;
      this.id2Descriptor = tempID2DMap;
    }
    LOG.info("MetricConf is updated, using time "
        + (System.currentTimeMillis() - timeStarted) + " ms "+"name2Descriptor.size():"+name2Descriptor.size());
  }
  
  public synchronized MetricDescriptor getDescriptor(String metricName){
    return name2Descriptor.get(metricName);
  }
  
  public synchronized MetricDescriptor getDescriptor(Long id){
    return id2Descriptor.get(id);
  }
  
  public synchronized ArrayList<Long> getImportantIDs() {//得到开头Id为1~5的metricId
    ArrayList<Long> result = new ArrayList<Long>();
    for (Long id : id2Descriptor.keySet()) {
      if (MetricDescriptor.isMetricImportant(id))
        result.add(id);
    }
      
    return result;
  }
  
  public void close() throws IOException{
    if (table != null)
      table.close();
  }
  
  public String toString(){
    StringBuilder sb = new StringBuilder();
    for (Entry<String, MetricDescriptor> entry : name2Descriptor.entrySet()){
      sb.append(entry.getValue()).append("\n");
    }
    return sb.toString();
  }

  @Override
  public void run() {
    try {
      update();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        table.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
