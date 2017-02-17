package com.sina.data.bigmonitor.web;

import java.util.ArrayList;
import java.util.List;

public class ClusterMetricJson {
  private String clusterName = null;
  private String metricName = null;
  private List<Object[]> data;
  
  public ClusterMetricJson(String n, String metric, List<String> v, List<String> t) {
    clusterName = n;
    metricName = metric;
    this.data = new ArrayList<Object[]>();
    for (int i = 0; i < v.size(); ++i) {
      Object[] e = new Object[2];
      try {
        e[0] = (Long.parseLong(t.get(i)) + 8*3600) * 1000; // convert it to UTC
      } catch (NumberFormatException e1) {
        e[0] = t.get(i);
      }
      try {
        e[1] = Double.parseDouble(v.get(i));
      } catch (NumberFormatException e1) {
        e[1] = v.get(i);
      }
      data.add(e);
    }
  }
}
