package com.sina.data.bigmonitor.metric;

import java.util.ArrayList;

public class MetricValue {
  private long metricID;
  private ArrayList<Object> value;//长度默认1024
  long lastUpdateTime = 0;//最后一次更新时间
  
  public MetricValue(long id) {
    this(id, 1024);
  }
  
  public MetricValue(long id, int capacity){
    metricID = id;
    value = new ArrayList<Object>(capacity);
  }
  
  public void addValue(Object o){
    value.add(o);
    lastUpdateTime = System.currentTimeMillis();
  }
  
  public String getAverageString() {
    if (value.size() == 0)
      return "0";
    if (value.get(0) instanceof Long) {
      Long sum = 0L;
      for (Object l : value) {
        sum += (Long) l;
      }
      return Double.toString(1.0 * sum / value.size());
    } else if (value.get(0) instanceof Double) {
      Double sum = 0.0;
      for (Object l : value) {
        sum += (Double) l;
      }
      return Double.toString(sum / value.size());
    } else {
      return value.get(0).toString();
    }
  }

  public Object getLatest() {
    if (value.size() == 0)
      return null;
    return value.get(value.size() - 1);
  }
  
  public int getSize(){
    return value.size();
  }
  
  public void clear(){
    value.clear();
  }
  
  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append("ID:").append(metricID).append(" values:").append(value.toString());
    sb.append(" average:").append(getAverageString());
    return sb.toString();
  }
}
