package com.sina.data.bigmonitor.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClusterMetricRankingJson {
  private String clusterName = null;
  private String metricName = null;
  private List<String> hosts = null;
  private transient List<HostMetricEntry> buffer = null;
  
  public ClusterMetricRankingJson(String cName, String mName) {
    clusterName = cName;
    metricName = mName;
    hosts = new ArrayList<String>();
    buffer = new ArrayList<HostMetricEntry>();
  }
  
  class HostMetricEntry implements Comparable {
    String hostname;
    Double value;
    
    public HostMetricEntry(String h, Double v) {
      this.hostname = h;
      this.value = v;
    }
    
    @Override
    public int compareTo(Object o) {
      if (!(o instanceof HostMetricEntry))
        return 0;
      HostMetricEntry e = (HostMetricEntry) o;
      if (this.value < e.value)
        return -1;
      else if (this.value > e.value)
        return 1;
      else
        return 0;
    }
  }
  
  public void add(String host, Double value) {
    buffer.add(new HostMetricEntry(host, value));
  }
  
  public void finishedAdding() {
    Collections.sort(buffer);
    for (HostMetricEntry e : buffer) {
      hosts.add(e.hostname);
    }
  }
  public static void main(String[] args)
  {
   // ClusterMetricRankingJson test=new ClusterMetricRankingJson("unspecified","mem_total");


  }
}
