package com.sina.data.bigmonitor.web;

import java.util.List;

public class HostInfoJson {
  private String name;
  private List<String> metrics;
  
  public HostInfoJson(String hostname, List<String> metricList) {
    this.name = hostname;
    this.metrics = metricList;
  }
}
