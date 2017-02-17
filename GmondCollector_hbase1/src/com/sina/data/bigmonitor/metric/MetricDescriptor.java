package com.sina.data.bigmonitor.metric;

import java.util.HashSet;

public class MetricDescriptor {
  private String name;
  private String type;
  private long ID;
  private int level;
  private boolean positive;
  private HashSet<String> hosts;
  
  /**
   * 
   * @param id
   * @param l
   * @param inc
   * @param h will copy the content of this set. 
   */
  public MetricDescriptor(String n, String t, long id, int l, 
      boolean inc, HashSet<String> h){
    this.name = n;
    this.type = t;
    this.ID = id;
    this.level = l;
    this.positive = inc;
    hosts = new HashSet<String>(h);
  }
  
  public String getName(){
    return name;
  }
  
  public String getType() {
    return type;
  }
  
  public long getID(){
    return ID;
  }
  
  public int getLevel(){
    return level;
  }
  
  public boolean isSlopePositive(){
    return positive;
  }
  
  public boolean ipInAlert(String ip){
    return hosts.contains(ip);
  }
  
  public static int parseLevel(long id){
    // TODO: this is not fixed yet
    return (int)id/100000;
  }
  
  /**
   * @return True if this metric should be put into the normal table.
   * False if this metric should not be displayed by default.
   */
  public static boolean isMetricImportant(long id){
    return parseLevel(id) <= 5;
  }
  
  /**
   * @return True if this metric may trigger alerts.  
   */
  public static boolean isMetricAlertable(long id){
    return parseLevel(id) <= 5;
  }
  
  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append(name).append("=>(").
    append(ID).append(",").
    append(level).append(",").
    append(positive).append(",").
    append(hosts).append(")");
    return sb.toString();
  }
}
