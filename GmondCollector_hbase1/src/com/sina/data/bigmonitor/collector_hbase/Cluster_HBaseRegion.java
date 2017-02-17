package com.sina.data.bigmonitor.collector_hbase;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;
import com.sina.data.bigmonitor.metric.MetricDescriptor;
import com.sina.data.bigmonitor.metric.MetricValue;

public class Cluster_HBaseRegion {
  public static final Logger LOG = Logger.getLogger(Cluster_HBaseRegion.class.getName());
  private HashMap<String, HBaseRegionVal> hbaseRegionsVal = new HashMap<String, HBaseRegionVal>();
  private HashMap<Long, MetricValue> metricValuesMap = new HashMap<Long, MetricValue>();
  private String clusterName = "";
  private long localTime = 0;
  /** this is the time stamp need to write to HBase, rounded to 15 seconds */
  private long tsToWrite = 0;
  private HTable clusterHeartbeatTable = null;
  private HTable clusterOrigTable = null;
  private HTable clusterHourTable = null;
  private HTable hostOrigTable = null;
  private HTable hostHourTable = null;
  private HTable nodispTable = null;
  private HTable alertTable = null;
  private MetricConf mc = null;

  public Cluster_HBaseRegion(String name, MetricConf mc, Configuration conf)
      throws IOException {
    this.clusterName = name;
    this.mc = mc;
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
              BigMonitorConstants.MetricDataTableDefaultPrefix) + "HostNoDisplay");
    }
    if (this.alertTable == null) {
      this.alertTable = realSetupHTable(
          conf,
          conf.get(BigMonitorConstants.MetricDataAlertTablePrefixKey,
              BigMonitorConstants.MetricDataAlertTableDefaultPrefix) + "Alert");
    }
  }

  public synchronized HBaseRegionVal getHost(String ip) {
    HBaseRegionVal h = hbaseRegionsVal.get(ip);
    if (h == null) {
      h = new HBaseRegionVal(ip, mc);
      hbaseRegionsVal.put(ip, h);
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
    for (Entry<String, HBaseRegionVal> entry : hbaseRegionsVal.entrySet()) {
      p.add(cfBytes, Bytes.toBytes(entry.getKey()),
          Bytes.toBytes(entry.getValue().getLastReport()));
    }
    clusterHeartbeatTable.put(p);
    clusterHeartbeatTable.flushCommits();
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
    clusterOrigTable.flushCommits();
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
    clusterHourTable.flushCommits();
    return true;
  }

  private boolean writeHostMetricsToHBase() throws IOException {
    if (hostOrigTable == null || nodispTable == null || alertTable == null)
      return false;
    for (Entry<String, HBaseRegionVal> e : hbaseRegionsVal.entrySet()) {
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
    for (Entry<String, HBaseRegionVal> e : hbaseRegionsVal.entrySet()) {
      e.getValue().writeHostHourToHBase(hostHourTable);
    }
    return true;
  }

  /**
   * Should be called if we see end tag for cluster. This function calculates
   * cluster summary.
   * 
   * @param needToWrite
   *          True if should write to HBase now.
   * @throws IOException
   */
  synchronized public void thisRoundFinished(boolean needToWrite) throws IOException {
    Set<Long> allMetricIds = new HashSet<Long>();
    for (HBaseRegionVal h : hbaseRegionsVal.values()) {
      allMetricIds.addAll(h.getMetricIDSet());
    }
    
    outer : for (Long id : allMetricIds) {
      // if this metric is not important, don't calculate it cluster summary
      if (!MetricDescriptor.isMetricImportant(id))
        continue;
      Double sum = 0.0;
      for (HBaseRegionVal h : hbaseRegionsVal.values()) {
        MetricValue mv = h.getMetricValue(id);
        if (mv == null || mv.getLatest() == null) {
          continue;
        }
        Object o = mv.getLatest();
        if (o == null)
          continue;
        if (o instanceof Long || o instanceof Double) {
          sum += (Double)o;
        } else {
           // this is not number (should be a string), just use this
          // object as the "summary".
          addClusterMetric(id, o);
          continue outer;
        }
      }
      
      addClusterMetric(id, sum);
    }
    
    if (needToWrite) {
      writeHeartbeatsToHBase();
      writeHostMetricsToHBase();
      writeClusterMetricsToHBase();
    }
  }

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
    for (HBaseRegionVal h : hbaseRegionsVal.values()) {
      sb.append("\t").append(h).append("\n");
    }
    return sb.toString();
  }

}
