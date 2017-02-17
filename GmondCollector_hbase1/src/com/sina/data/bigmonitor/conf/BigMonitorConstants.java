package com.sina.data.bigmonitor.conf;

public interface BigMonitorConstants {
  // Startup options
  static public enum StartupOption {
    FORMAT("-format"), 
    ADDMETRICS("-addmetrics"), 
    GETUSINGNAME("-getUsingName"),
    GETUSINGID("-getUsingID"),
    UPDATEMETRICS("-updatemetrics"),
    REMOVEMETRIC("-removemetrics");

    private String name = null;

    private StartupOption(String arg) {
      this.name = arg;
    }

    public String getName() {
      return name;
    }
  }
  
  static public enum MetricType {
    LONG("long"),
    DOUBLE("double"),
    STRING("string");
    
    private String name = null;

    private MetricType(String arg) {
      this.name = arg;
    }

    public String getName() {
      return name;
    }
  }

  public static String IPRegex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." 
      + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."  
      + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."  
      + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";  
  public static String hbase_namespace="ns_hadoopadmin:";
  public static String CONFFILE = "BigMonitorSetting.xml";
  public static String MetricConfTableKey = "bigmonitor.metricconf.tablename";
  public static String MetricConfDefaultTableName = hbase_namespace+"BigMonitorMetricConfTable";
  public static String ClusterConfTableKey = "bigmonitor.clusterconf.tablename";
  public static String ClusterConfDefaultTableName = hbase_namespace+"BigMonitorClusterConfTable"; //key 集群name   c1:ip(收集数据的时候调用writeHeartbeatsToHBase将ip填入)   
  																									//cm:metricId(在Summarizer的daySummary中更新)
  //monitor-02中如果配置的ip为*  请求此接口获取所有ip "http://%s/get?method=getClusterInfo&clustername=%s&format=json"
  public static String HostConfTableKey = "bigmonitor.hostconf.tablename";
  public static String HostConfDefaultTableName = hbase_namespace+"BigMonitorHostConfTable";
  public static String MetricDataTablePrefixKey = "bigmonitor.metricdata.prefixname";
  public static String MetricDataTableDefaultPrefix = hbase_namespace+"BigMonitorMetricData";
  
  public static String MetricDataAlertTablePrefixKey = "bigmonitor.metricdata.alertprefixname";
  public static String MetricDataAlertTableDefaultPrefix = hbase_namespace+"BigMonitorMetricData";
  
  
  public static String HBaseRegionHeartbeatTableKey="bigmonitor.hbase.region.heartbeat.tablename";
  public static String HBaseRegionHeartbeatTableName=hbase_namespace+"BigMonitorMetricDataRegionHeartbeats";
  
  public static String HBaseRegionABSHeartbeatTableKey="bigmonitor.hbase.region.heartbeatABS.tablename";
  public static String HBaseRegionABSHeartbeatTableName=hbase_namespace+"BigMonitorMetricDataRegionABSHeartbeats";
  
  public static String MetricConfTableCF1 = "c1";
  public static String MetricConfTableCF2 = "c2";
  public static String ClusterConfTableCF1 = "c1";
  public static String ClusterConfTableCF2 = "cm";
  public static String HostConfTableCF1 = ClusterConfTableCF2;
  public static String DataTableCF = "c1";
  public static String DataTableQa = "d";
  
  public static long HTableWriteBufferSize = 50 * 1024;
  
  public static long CollectionInterval = 15; // seconds
  public static long HourTableInterval = 60; // seconds
  public static long DayTableInterval = 20 * 60; // 20 minutes
  public static long WeekTableInterval = 2 * 60 * 60; //2 hours
  public static long MonthTableInterval = 8 * 60 * 60; // 8 hours
  
  public static String GmondAddressKey = "bigmonitor.gmond.address";
  public static String GmondPortKey = "bigmonitor.gmond.port";
  public static int GmondDefaultPort = 8649;
  
  public static String HBaseMasterJmxUrl="bigmonitor.hbase.jmx.url";
  
  public static String HandlerCountKey = "bigmonitor.system.handler.count";
  public static int HandlerDefaultCount = 10;
  public static String MetricConfUpdateKey = "bigmonitor.system.metricconf.update";
  public static int MetricConfDefaultUpdate = 60 * 60 * 1000; // one hour
  
  public static String HTableConcurrencyKey = "bigmonitor.hbase.concurrency";
  public static int HTableConcurrencyDefault = 100;
  
  public static String WebAddressKey = "bigmonitor.web.address";
  public static String WebPortKey = "bigmonitor.web.port";
}
