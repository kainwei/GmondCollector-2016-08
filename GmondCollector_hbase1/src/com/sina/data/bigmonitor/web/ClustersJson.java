package com.sina.data.bigmonitor.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;
import com.sina.data.bigmonitor.metric.MetricDescriptor;

public class ClustersJson {
  private List<String> names;
  private transient long lastUpdate = 0;
  private transient Configuration conf = null;
  private transient Map<String, ClusterInfoJson> clusterMap = null;
  
  public ClustersJson() {
    this(new Configuration());
  }
  
  public ClustersJson(Configuration c) {
    names = new ArrayList<String>();
    conf = c;
  }
  
  public ClusterInfoJson getClusterInfoJson(String clusterName) {
    return clusterMap.get(clusterName);
  }
  //获取BigMonitorClusterConfTable表的cm值返回到接口
  public boolean update(HTablePool htp) throws IOException {
    if (System.currentTimeMillis() - lastUpdate < 5 * 60 * 1000) // 5min
      return false;
    List<String> tempNames = new ArrayList<String>();
    Map<String, ClusterInfoJson> tempClusterMap = new HashMap<String, ClusterInfoJson>();
    String tableName = conf.get(BigMonitorConstants.ClusterConfTableKey, 
        BigMonitorConstants.ClusterConfDefaultTableName);
    HTableInterface ht = htp.getTable(tableName);
    Scan sc = new Scan();
    byte[] cf1Bytes = Bytes.toBytes(BigMonitorConstants.ClusterConfTableCF1);
    byte[] cf2Bytes = Bytes.toBytes(BigMonitorConstants.ClusterConfTableCF2);
    MetricConf mc = GetdataServiceServelet.getMetricConf();
    ResultScanner rs = ht.getScanner(sc);
    for (Result r : rs) {
      String row = Bytes.toString(r.getRow());
      tempNames.add(row);
//      tempClusterMap.put(row, new ClusterInfoJson());
      List<String> hosts = new ArrayList<String>();
      NavigableMap<byte[], byte[]>  hostMap = r.getFamilyMap(cf1Bytes);
      for (byte[] e : hostMap.keySet()) {
        hosts.add(Bytes.toString(e));
      }
      List<String> metrics = new ArrayList<String>();
      NavigableMap<byte[], byte[]>  metricMap = r.getFamilyMap(cf2Bytes);
      for (byte[] e : metricMap.keySet()) {
        String metricIDString = Bytes.toString(e);
        Long id = Long.parseLong(metricIDString);
        MetricDescriptor md = mc.getDescriptor(id);
        if (md == null)
          continue;
        String metricName = md.getType() + "#" + md.getName();
        metrics.add(metricName);
      }
      tempClusterMap.put(row, new ClusterInfoJson(row, hosts, metrics));
    }
    synchronized(this) {
      names = tempNames;
      clusterMap = tempClusterMap;
    }
    ht.close();
    lastUpdate = System.currentTimeMillis();
    return false;
  }
}
