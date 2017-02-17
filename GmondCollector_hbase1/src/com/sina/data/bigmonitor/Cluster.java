package com.sina.data.bigmonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;
import com.sina.data.bigmonitor.metric.MetricDescriptor;
import com.sina.data.bigmonitor.metric.MetricValue;

public class Cluster {
  public static final Logger LOG = Logger.getLogger(Cluster.class.getName());
  private HashMap<String, Host> hosts = new HashMap<String, Host>();//host ip对应 xml (文件中的 Long.parseLong(attributes.getValue("REPORTED"))
  private HashMap<Long, MetricValue> metricValuesMap = new HashMap<Long, MetricValue>();//汇总所有host的metricID对应的value 放到cluster的metricValuesMap中
  private String clusterName = "";
  private long localTime = 0;
  /** this is the time stamp need to write to HBase, rounded to 15 seconds */
  private long tsToWrite = 0;
  private Table clusterHeartbeatTable = null; 
  private Table clusterOrigTable = null;
  private Table clusterHourTable = null;
  private Table hostOrigTable = null;
  private Table hostHourTable = null;
  private Table nodispTable = null;
  private Table alertTable = null;
  private MetricConf mc = null;

  public Cluster(String name, MetricConf mc, Configuration conf)
      throws IOException {
    this.clusterName = name;
    this.mc = mc;
    setupHBaseConn(conf);
  }

  private Table realSetupHTable(Configuration conf, String name)
      throws IOException {
	  
	  Table table = null;
	 /* Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum","");
		config.set("hbase.zookeeper.property.clientPort", "2181");*/
		Connection connection = null;		
      connection = ConnectionFactory.createConnection(conf);
      table = connection.getTable(TableName.valueOf(name));
	  
	  return table;
    /*HTable t = new HTable(conf, name);
    t.setAutoFlush(false, true);
    t.setWriteBufferSize(BigMonitorConstants.HTableWriteBufferSize);
    return t;*/
  }

  private void setupHBaseConn(Configuration conf) throws IOException {
    if (this.clusterHeartbeatTable == null) {
      this.clusterHeartbeatTable = realSetupHTable(conf, conf.get(
          BigMonitorConstants.ClusterConfTableKey,
          BigMonitorConstants.ClusterConfDefaultTableName));
    }
    if (this.clusterOrigTable == null) {
      this.clusterOrigTable = realSetupHTable(
          conf,
          conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
              BigMonitorConstants.MetricDataTableDefaultPrefix) + "ClusterOrig");
    }
    if (this.clusterHourTable == null) {
      this.clusterHourTable = realSetupHTable(
          conf,
          conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
              BigMonitorConstants.MetricDataTableDefaultPrefix) + "ClusterHour");
    }
    if (this.hostOrigTable == null) {
      this.hostOrigTable = realSetupHTable(
          conf,
          conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
              BigMonitorConstants.MetricDataTableDefaultPrefix) + "HostOrig");
    }
    if (this.hostHourTable == null) {
      this.hostHourTable = realSetupHTable(
          conf,
          conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
              BigMonitorConstants.MetricDataTableDefaultPrefix) + "HostHour");
    }
    if (this.nodispTable == null) {
      this.nodispTable = realSetupHTable(
          conf,
          conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
              BigMonitorConstants.MetricDataTableDefaultPrefix)
              + "HostNoDisplay");
    }
    if (this.alertTable == null) {
      this.alertTable = realSetupHTable(
          conf,
          conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
              BigMonitorConstants.MetricDataTableDefaultPrefix) + "Alert");
    }
  }

  public synchronized Host getHost(String ip) {
    Host h = hosts.get(ip);
    if (h == null) {
      h = new Host(ip, mc);
      hosts.put(ip, h);
    }
    return h;
  }

  public synchronized void setLocalTime(long time) {
    this.localTime = time;
    this.tsToWrite = time / BigMonitorConstants.CollectionInterval
        * BigMonitorConstants.CollectionInterval;
  }

  private boolean writeHeartbeatsToHBase() throws IOException {
    if (clusterHeartbeatTable == null)
      return false;
    Put p = new Put(Bytes.toBytes(clusterName));
    byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.ClusterConfTableCF1);
    for (Entry<String, Host> entry : hosts.entrySet()) {
      p.add(cfBytes, Bytes.toBytes(entry.getKey()),
          Bytes.toBytes(entry.getValue().getLastReport()));
    }
    clusterHeartbeatTable.put(p);//  key:clusterName   family:column格式: "c1:10.39.xx.xx" value:本次获取xml数据的LastReport
   // clusterHeartbeatTable.flushCommits();
    return true;
  }

  private boolean writeClusterMetricsToHBase() throws IOException {
    if (clusterOrigTable == null)
      return false;
    byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.DataTableCF);
    byte[] qBytes = Bytes.toBytes(BigMonitorConstants.DataTableQa);
    for (Entry<Long, MetricValue> e : metricValuesMap.entrySet()) {
      Object value = e.getValue().getLatest();
      if (value == null)
        continue;
      Put p = new Put(Bytes.toBytes(clusterName + "_" + e.getKey() + "_"
          + tsToWrite));
      p.add(cfBytes, qBytes, Bytes.toBytes(value.toString()));
      clusterOrigTable.put(p);
    }
    //clusterOrigTable.flushCommits();
    if (tsToWrite % BigMonitorConstants.HourTableInterval < 
        BigMonitorConstants.CollectionInterval) // now it is time to write the hour table
      return writeClusterHourMetricsToHBase();
    return true;
  }

  private boolean writeClusterHourMetricsToHBase() throws IOException {
    if (clusterHourTable == null)
      return false;
    byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.DataTableCF);
    byte[] qBytes = Bytes.toBytes(BigMonitorConstants.DataTableQa);
    for (Entry<Long, MetricValue> e : metricValuesMap.entrySet()) {
      Put p = new Put(Bytes.toBytes(clusterName + "_" + e.getKey() + "_"
          + tsToWrite));
      p.add(cfBytes, qBytes, Bytes.toBytes(e.getValue().getAverageString()));
      e.getValue().clear();
      clusterHourTable.put(p);
    }
 //   clusterHourTable.flushCommits();
    return true;
  }

  private boolean writeHostMetricsToHBase() throws IOException {
    if (hostOrigTable == null || nodispTable == null || alertTable == null)
      return false;
    for (Entry<String, Host> e : hosts.entrySet()) {
      e.getValue().writeHostOrigToHBase(hostOrigTable, nodispTable, alertTable);
    }
    if (tsToWrite % BigMonitorConstants.HourTableInterval < 
        BigMonitorConstants.CollectionInterval) // now it is time to write the hour table
      return writeHostHourMetricsToHBase();
    return true;
  }

  private boolean writeHostHourMetricsToHBase() throws IOException {
    if (hostHourTable == null)
      return false;
    for (Entry<String, Host> e : hosts.entrySet()) {
      e.getValue().writeHostHourToHBase(hostHourTable);
    }
    return true;
  }

  /**
   * Should be called if we see end tag for cluster. This function calculates
   * cluster summary.
   * 
   * 根据解析完的数据，入到hbase表中
   * 
   * 
   * @param needToWrite
   *          True if should write to HBase now.
   * @throws IOException
   */
  synchronized public void thisRoundFinished(boolean needToWrite) throws IOException {
    Set<Long> allMetricIds = new HashSet<Long>();
    for (Host h : hosts.values()) {//hosts对应GmondXMLParser的currHost
      allMetricIds.addAll(h.getMetricIDSet());//host对应所有 配置表里的 metric id(过滤metric的level)
    }
    
    outer : for (Long id : allMetricIds) {//遍历所有metric配置表里的metric
      // if this metric is not important, don't calculate it cluster summary
      if (!MetricDescriptor.isMetricImportant(id))//过滤metric 的level------
        continue;
      
      //如果是ImportantMetric---------
      Double sum = 0.0;
      for (Host h : hosts.values()) {//hosts对应GmondXMLParser的currHost   key：ip  value:  xml 文件中的 Long.parseLong(attributes.getValue("REPORTED"))
        MetricValue mv = h.getMetricValue(id);//得到这个host 此metric id 的值
        if (mv == null || mv.getLatest() == null) {  //mv 中存储若干此host对应此metric的值,再次取最后一次获得的值
          continue;
        }
        Object o = mv.getLatest();//mv 中存储若干此host对应此metric的值,再次取最后一次获得的值
        if (o == null)
          continue;
        if (o instanceof Long || o instanceof Double) {//如果此metric 是double类型就累加所有host的value
          sum += (Double)o;
        } else {//string类型直接赋值
           // this is not number (should be a string), just use this
          // object as the "summary".
          addClusterMetric(id, o);
          continue outer;
        }
      }
      addClusterMetric(id, sum);//如果是数字类型的metric， 多个host的value值累加
    }
    
    if (needToWrite) {
      writeHeartbeatsToHBase();
      writeHostMetricsToHBase();
      writeClusterMetricsToHBase();
    }
  }
//汇总所有host的metricID对应的value 放到cluster的metricValuesMap中
  private void addClusterMetric(Long metricID, Object value) {
    MetricValue mv = metricValuesMap.get(metricID);
    if (mv == null) {
      if (!MetricDescriptor.isMetricImportant(metricID))
        return;
      mv = new MetricValue(metricID);
      metricValuesMap.put(metricID, mv);
    }
    mv.addValue(value);
  }

  public void close() throws IOException {
    IOException recodedE = null;
    try {
      clusterHeartbeatTable.close();
    } catch (IOException e) {
      recodedE = e;
    }
    try {
      clusterOrigTable.close();
    } catch (IOException e) {
      recodedE = e;
    }
    try {
      clusterHourTable.close();
    } catch (IOException e) {
      recodedE = e;
    }
    try {
      hostOrigTable.close();
    } catch (IOException e) {
      recodedE = e;
    }
    try {
      hostHourTable.close();
    } catch (IOException e) {
      recodedE = e;
    }
    try {
      nodispTable.close();
    } catch (IOException e) {
      recodedE = e;
    }
    try {
      alertTable.close();
    } catch (IOException e) {
      recodedE = e;
    }
    if (recodedE != null)
      throw recodedE;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Cluster:").append(clusterName).append(" lastreport:")
        .append(localTime).append("\n");
    sb.append("Clusterwide metrics: \n");
    for (MetricValue mv : metricValuesMap.values()) {
      sb.append("\t").append(mv).append("\n");
    }
    sb.append("Hosts: \n");
    for (Host h : hosts.values()) {
      sb.append("\t").append(h).append("\n");
    }
    return sb.toString();
  }

}
