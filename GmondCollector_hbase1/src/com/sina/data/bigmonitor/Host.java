package com.sina.data.bigmonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.conf.BigMonitorConstants.MetricType;
import com.sina.data.bigmonitor.metric.MetricConf;
import com.sina.data.bigmonitor.metric.MetricDescriptor;
import com.sina.data.bigmonitor.metric.MetricValue;

public class Host {
  private String ip;
  private long lastReport;//  每次读数据时=  xml 文件中的 Long.parseLong(attributes.getValue("REPORTED"))
  private long tsToWrite;
  private HashMap<Long, MetricValue> metricValuesMap = new HashMap<Long, MetricValue>();
  private MetricConf mc = null;
  
  public Host(String address, MetricConf mc){
    this.ip = address;
    this.mc = mc;
  }
  public String getIP(){
    return ip;
  }
  
  public long getLastReport(){
    return lastReport;
  }
  
  public synchronized void setLastReport(long ts){
    this.lastReport = ts;
    this.tsToWrite = ts / BigMonitorConstants.CollectionInterval * 
                          BigMonitorConstants.CollectionInterval;
  }
  
  public synchronized void addMetricValue(Long metricID, String val, String metricType){
    MetricValue mv = metricValuesMap.get(metricID);
    if (mv == null){
      mv = new MetricValue(metricID);
      metricValuesMap.put(metricID, mv);
    }
    Object o;
    if (metricType.equals(MetricType.STRING.getName())){
      o = val;
    } else {
      o = Double.parseDouble(val);
    }
    mv.addValue(o);
  }
  
  public MetricValue getMetricValue(Long metricID){
    return metricValuesMap.get(metricID);
  }
  
  public Set<Long> getMetricIDSet(){
    return metricValuesMap.keySet();
  }
  
  public boolean writeHostOrigToHBase(Table normalTable, 
                                      Table nodispTable, 
                                      Table alertTable) throws IOException {
    if (normalTable == null || nodispTable == null || alertTable == null)
      return false;
    byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.DataTableCF);
    byte[] qBytes = Bytes.toBytes(BigMonitorConstants.DataTableQa);
    for (Entry<Long, MetricValue> e : metricValuesMap.entrySet()) {
      Long id = e.getKey();
      if (mc.getDescriptor(id) == null) {
        // this metric id is deleted from hbase, we delete the local buffer
        metricValuesMap.remove(id);
        continue;
      }
      Object value = e.getValue().getLatest();
      if (value == null)
        continue;
      Put p = new Put(Bytes.toBytes(ip + "_" + id + "_" + tsToWrite));
      p.add(cfBytes, qBytes,
          Bytes.toBytes(value.toString()));
      if (MetricDescriptor.isMetricImportant(id)) {
        normalTable.put(p);
      } else {
        nodispTable.put(p);
      }
      if (MetricDescriptor.isMetricAlertable(id)){//&& mc.getDescriptor(id).ipInAlert(ip)
          
        p = new Put(Bytes.toBytes(tsToWrite + "_" + id + "_" + ip));
        p.add(cfBytes, qBytes,
            Bytes.toBytes(value.toString()));
        alertTable.put(p);
      }
    }
  //  normalTable.flushCommits();
 //   nodispTable.flushCommits();
 //   alertTable.flushCommits();
    return true;
  }
  
  public boolean writeHostHourToHBase(Table ht) throws IOException{
    if (ht == null)
      return false;
    long hourTS = tsToWrite / BigMonitorConstants.HourTableInterval 
        * BigMonitorConstants.HourTableInterval;
    byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.DataTableCF);
    byte[] qBytes = Bytes.toBytes(BigMonitorConstants.DataTableQa);
    for (Entry<Long, MetricValue> e : metricValuesMap.entrySet()) {
      Put p = new Put(Bytes.toBytes(ip + "_" + e.getKey() + "_"
          + hourTS));
      p.add(cfBytes, qBytes, Bytes.toBytes(e.getValue().getAverageString()));
      e.getValue().clear();
      ht.put(p);
    }
  //  ht.flushCommits();
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ip:").append(ip).append(" lastreport:").append(lastReport).append("\n");
    for (MetricValue mv : metricValuesMap.values()){
      sb.append("\t").append(mv).append("\n");
    }
    return sb.toString();
  }

}
