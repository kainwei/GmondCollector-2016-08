package com.sina.data.bigmonitor.web;

import java.util.List;

public class ClusterInfoJson {
  private String name = null;
  private List<String> hosts = null;
  private List<String> metrics = null;
  
  public ClusterInfoJson(String n, List<String> hostList, List<String> metricList){
    name = n;
    hosts = hostList;
    metrics = metricList;
  }
  
  public List<String> getHosts() {
    return hosts;
  }
  
}
