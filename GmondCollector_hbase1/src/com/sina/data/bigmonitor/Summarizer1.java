package com.sina.data.bigmonitor;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;
import com.sina.data.bigmonitor.metric.MetricValue;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class Summarizer1 extends TimerTask {
  public static synchronized HConnection getHConnection(Configuration conf){
    HConnection conn = null;
    if(conn == null){
      try {
        conn = HConnectionManager.createConnection(conf);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return conn;
  }
  class MyHTableFactory extends HTableFactory {
    @Override
    public HTableInterface createHTableInterface(Configuration config,
        byte[] tableName) {
      try {
        HTable t = new HTable(config, tableName);
        t.setAutoFlush(false, true);
        t.setWriteBufferSize(BigMonitorConstants.HTableWriteBufferSize);
        return t;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  static {
    Configuration.addDefaultResource(BigMonitorConstants.CONFFILE);
    PropertyConfigurator.configure("conf/log4j.properties");
  }

  public static final Logger LOG = Logger.getLogger(Summarizer1.class.getName());
  private Configuration conf = null;
  private String clusterTableName = null;
  private String hostTableName = null;
  private String prefix = null;
  private int htpConcurrency = 0;
  //private HTablePool htp = null;
  private HConnection htp = null;
  private ExecutorService executor = null; 
  private LinkedBlockingQueue<Integer> triger = new LinkedBlockingQueue<Integer>();
  private ArrayList<HTableInterface> clusterConfTables = null;
  private ArrayList<HTableInterface> hostConfTables = null;
  private ArrayList<HTableInterface> HostHourTables = null;
  private ArrayList<HTableInterface> HostDayTables = null;
  private ArrayList<HTableInterface> HostWeekTables = null;
  private ArrayList<HTableInterface> HostMonthTables = null;
  private ArrayList<HTableInterface> ClusterHourTables = null;
  private ArrayList<HTableInterface> ClusterDayTables = null;
  private ArrayList<HTableInterface> ClusterWeekTables = null;
  private ArrayList<HTableInterface> ClusterMonthTables = null;
  
  private MetricConf mc = null;
  
  public Summarizer1(Configuration c) throws IOException {
    this.conf = c;
    htpConcurrency = c.getInt(BigMonitorConstants.HTableConcurrencyKey, 
        BigMonitorConstants.HTableConcurrencyDefault);
    //htp = new HTablePool(c, htpConcurrency, new MyHTableFactory());
    htp = getHConnection(c);
    executor = Executors.newFixedThreadPool(htpConcurrency);
    init();
  }
  
  private ArrayList<HTableInterface> getTables(String name) throws IOException {
    ArrayList<HTableInterface> result = new ArrayList<HTableInterface>();
    for (int i = 0; i < htpConcurrency; ++i) {
      HTable tempTable = new HTable(conf, name);
      tempTable.setAutoFlush(false, true);
      tempTable.setWriteBufferSize(BigMonitorConstants.HTableWriteBufferSize);
      result.add(tempTable);
    }
    return result;
  }

  private void init() throws IOException {
    mc = new MetricConf(conf);
    clusterTableName = conf.get(BigMonitorConstants.ClusterConfTableKey,
        BigMonitorConstants.ClusterConfDefaultTableName);
    hostTableName = conf.get(BigMonitorConstants.HostConfTableKey, 
        BigMonitorConstants.HostConfDefaultTableName);
    prefix = conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
        BigMonitorConstants.MetricDataTableDefaultPrefix);
    
    clusterConfTables = getTables(clusterTableName);
    hostConfTables = getTables(hostTableName);
    HostHourTables = getTables(prefix + "HostHour");
    HostDayTables = getTables(prefix + "HostDay");
    HostWeekTables = getTables(prefix + "HostWeek");
    HostMonthTables = getTables(prefix + "HostMonth");
    ClusterHourTables = getTables(prefix + "ClusterHour");
    ClusterDayTables = getTables(prefix + "ClusterDay");
    ClusterWeekTables = getTables(prefix + "ClusterWeek");
    ClusterMonthTables = getTables(prefix + "ClusterMonth");
    
    for (int i = 0; i < htpConcurrency; ++i)
      triger.add(i);
  }

  /**
   * Retrieve cluster information from HBase, return only alive nodes that sent 
   * heart beats after the specified time stamp aliveFrom.
   * @param aliveFrom 
   * @return cluster_name => host_set
   * @throws IOException
   */
  private HashMap<String, HashSet<String>> getCluster(long aliveFrom)
      throws IOException {
    HashMap<String, HashSet<String>> result = new HashMap<String, HashSet<String>>();
    Scan scan = new Scan();
    ResultScanner rs = null;
    HTableInterface t = null;
    try {
      t = htp.getTable(clusterTableName);
      rs = t.getScanner(scan);
      byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.ClusterConfTableCF1);
      for (Result r : rs) {
        String thisCluster = Bytes.toString(r.getRow());
//        if (!thisCluster.equals("master"))
//          continue; // TODO
        HashSet<String> hosts = result.get(thisCluster);
        if (hosts == null) {
          hosts = new HashSet<String>();
          result.put(thisCluster, hosts);
        }
        NavigableMap<byte[], byte[]> m = r.getFamilyMap(cfBytes);
        for (Entry<byte[], byte[]> e : m.entrySet()) {
          long heartBeat = Bytes.toLong(e.getValue());
          // the last heart beat is too long ago, ignore it.
          if (heartBeat < aliveFrom)
            continue;
          hosts.add(Bytes.toString(e.getKey()));
        }
      }
    } catch (IOException e) {
      throw e;
    } finally {
      t.close();
    }
    return result;
  }
  
  private HashMap<String, HashSet<String>> getClusterSelf(long aliveFrom)
      throws IOException {
    HashMap<String, HashSet<String>> result = new HashMap<String, HashSet<String>>();
    Scan scan = new Scan();
    ResultScanner rs = null;
    HTableInterface t = null;
    int tableIndex = -1;
    try {
      tableIndex = triger.take();
      t = clusterConfTables.get(tableIndex);
      rs = t.getScanner(scan);
      byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.ClusterConfTableCF1);
      for (Result r : rs) {
        String thisCluster = Bytes.toString(r.getRow());
//        if (!thisCluster.equals("master"))
//          continue; // TODO
        HashSet<String> hosts = result.get(thisCluster);
        if (hosts == null) {
          hosts = new HashSet<String>();
          result.put(thisCluster, hosts);
        }
        NavigableMap<byte[], byte[]> m = r.getFamilyMap(cfBytes);
        for (Entry<byte[], byte[]> e : m.entrySet()) {
          long heartBeat = Bytes.toLong(e.getValue());
          // the last heart beat is too long ago, ignore it.
          if (heartBeat < aliveFrom)
            continue;
          hosts.add(Bytes.toString(e.getKey()));
        }
      }
    } catch (IOException e) {
      throw e;
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      if (tableIndex != -1)
        triger.add(tableIndex);
    }
    return result;
  }
  
  /**
   * Carries out real summary calculations, and write to tableTo.  
   * @param timeTo
   * @param timeFrom
   * @param tableFrom
   * @param tableTo
   * @param hosts The set of hosts, or cluster names, to search in tableFrom.
   * @return A list of puts that can put host metrics names to HBase
   * @throws IOException
   */
  private List<Put> calcAndPut(final long timeTo, final long timeFrom,
      final String tableFromName, final String tableToName, Set<String> hosts) throws IOException {
    List<Put> result = new ArrayList<Put>();
    for (final String host : hosts) {
      // the string host may also be cluster name, if the tableFrom is a cluster table.
      final Put hostMetrics = new Put(Bytes.toBytes(host));
      List<Future<String>> execResult = new ArrayList<Future<String>>();
      for (final Long metricID : mc.getImportantIDs()) {
        Future<String> r = executor.submit(new Callable<String>(){
          @Override
          public String call() {
            byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.DataTableCF);
            byte[] qBytes = Bytes.toBytes(BigMonitorConstants.DataTableQa);
            Scan scan = new Scan(Bytes.toBytes(host + "_" + metricID + "_"
                + timeFrom), Bytes.toBytes(host + "_" + metricID + "_" + timeTo));
            MetricValue mv = new MetricValue(metricID);
            ResultScanner rs = null;
            HTableInterface t1 = null;
            HTableInterface t2 = null;
            try {
              t1 = htp.getTable(tableFromName);
              rs = t1.getScanner(scan);
              for (Result r : rs) {
                String val = Bytes.toString(r.getValue(cfBytes, qBytes));
                mv.addValue(val);
              }
              t1.close();
              if (mv.getSize() == 0) {
//                LOG.warn("Data not found: " + host + "_" + metricID + "_" + timeTo);//TODO
                return null;
              }
              byte[] aveBytes = Bytes.toBytes(mv.getAverageString());
              Put p = new Put(Bytes.toBytes(host + "_" + metricID + "_" + timeTo));
              p.add(cfBytes, qBytes, aveBytes);
              t2 = htp.getTable(tableToName);
              t2.put(p);
              t2.flushCommits();
//              LOG.warn(Bytes.toString(p.getRow()) + " " + Bytes.toString(aveBytes));//TODO
            } catch (IOException e) {
              LOG.error("Error: ", e);
              throw new RuntimeException(e);
            } finally {
              try {
                if (t2 != null){
                  t2.close();
                }
              } catch (IOException e) {
                LOG.error("Error: ", e);
              }
            }
            return metricID.toString();
          }
        });
        execResult.add(r);
      } // end for Long metricID
      for (Future<String> r : execResult) {
        String m = null;
        try {
          m = r.get();
        } catch (Exception e) {
          LOG.error("Error", e);
          continue;
        } 
        if(m != null && !m.equals(""))
          hostMetrics.add(Bytes.toBytes(BigMonitorConstants.HostConfTableCF1), 
              Bytes.toBytes(m), 
              Bytes.toBytes(""));
      }
      if (!hostMetrics.isEmpty())
        result.add(hostMetrics);
    } // end for String host
    return result;
  }
  
  private List<Put> calcAndPutSelf(final long timeTo, final long timeFrom,
      final ArrayList<HTableInterface> tablesFrom, 
      final ArrayList<HTableInterface> tablesTo, 
      Set<String> hosts) throws IOException {
    List<Put> result = new ArrayList<Put>();
    for (final String host : hosts) {
      // the string host may also be cluster name, if the tableFrom is a cluster table.
      final Put hostMetrics = new Put(Bytes.toBytes(host));
      List<Future<String>> execResult = new ArrayList<Future<String>>();
      for (final Long metricID : mc.getImportantIDs()) {
        Future<String> r = executor.submit(new Callable<String>(){
          @Override
          public String call() {
            byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.DataTableCF);
            byte[] qBytes = Bytes.toBytes(BigMonitorConstants.DataTableQa);
            Scan scan = new Scan(Bytes.toBytes(host + "_" + metricID + "_"
                + timeFrom), Bytes.toBytes(host + "_" + metricID + "_" + timeTo));
            MetricValue mv = new MetricValue(metricID);
            ResultScanner rs = null;
            HTableInterface t = null;
            int tableIndex = -1;
            try {
              tableIndex = triger.take();
              t = tablesFrom.get(tableIndex);
              rs = t.getScanner(scan);
              for (Result r : rs) {
                String val = Bytes.toString(r.getValue(cfBytes, qBytes));
                mv.addValue(val);
              }
              if (mv.getSize() == 0) {
//                LOG.warn("Data not found: " + host + "_" + metricID + "_" + timeTo);//TODO
                return null;
              }
              byte[] aveBytes = Bytes.toBytes(mv.getAverageString());
              Put p = new Put(Bytes.toBytes(host + "_" + metricID + "_" + timeTo));
              p.add(cfBytes, qBytes, aveBytes);
              t = tablesTo.get(tableIndex);
              t.put(p);
              t.flushCommits();
//              LOG.warn(Bytes.toString(p.getRow()) + " " + Bytes.toString(aveBytes));//TODO
            } catch (Exception e) {
              LOG.error("Error: ", e);
              throw new RuntimeException(e);
            } finally {
              if (tableIndex != -1)
                triger.add(tableIndex);
            }
            return metricID.toString();
          }
        });
        execResult.add(r);
      } // end for Long metricID
      for (Future<String> r : execResult) {
        String m = null;
        try {
          m = r.get();
        } catch (Exception e) {
          LOG.error("Error", e);
          continue;
        } 
        if(m != null && !m.equals(""))
          hostMetrics.add(Bytes.toBytes(BigMonitorConstants.HostConfTableCF1), 
              Bytes.toBytes(m), 
              Bytes.toBytes(""));
      }
      if (!hostMetrics.isEmpty())
        result.add(hostMetrics);
    } // end for String host
    return result;
  }

  /**
   * Calculates the day summary (from timeTo-day to timeTo) using data from 
   * hour tables, and write to day tables.  
   * @param timeTo
   * @throws IOException
   */
  private void daySummary(long timeTo) throws IOException {
    LOG.info("Day summary starts. ");
    long timeStarted = System.currentTimeMillis();
    long timeFrom = timeTo - BigMonitorConstants.DayTableInterval;//1200
    List<Put> metricPuts = null;
    HTableInterface hostConfTable = htp.getTable(hostTableName);
    HTableInterface clusterConfTable = htp.getTable(clusterTableName);
    HashMap<String, HashSet<String>> cluster = getCluster(timeFrom);
    try {
      for (HashSet<String> hosts : cluster.values()) {
        metricPuts = calcAndPut(timeTo, timeFrom, prefix + "HostHour", prefix + "HostDay", hosts);
        if (metricPuts.size() > 0)
          hostConfTable.put(metricPuts);
      }
      metricPuts = calcAndPut(timeTo, timeFrom, prefix + "ClusterHour", prefix + "ClusterDay",
          cluster.keySet());
      if (metricPuts.size() > 0)
        clusterConfTable.put(metricPuts);
      hostConfTable.flushCommits();
      clusterConfTable.flushCommits();
    } finally {
      hostConfTable.close();
      clusterConfTable.close();
    }
    LOG.info("Day summary finished, using time "
        + (System.currentTimeMillis() - timeStarted) + " ms");
  }
  
  private void daySummarySelf(long timeTo) throws Exception {
    LOG.info("Day summary starts. ");
    long timeStarted = System.currentTimeMillis();
    long timeFrom = timeTo - BigMonitorConstants.DayTableInterval;
    List<Put> metricPuts = null;
    int tableIndex = -1;
    HashMap<String, HashSet<String>> cluster = getClusterSelf(timeFrom);
    try {
      tableIndex = triger.take();
      HTableInterface hostConfTable = hostConfTables.get(tableIndex);
      HTableInterface clusterConfTable = clusterConfTables.get(tableIndex);
      for (HashSet<String> hosts : cluster.values()) {
        metricPuts = calcAndPutSelf(timeTo, timeFrom, HostHourTables, HostDayTables, hosts);
        if (metricPuts.size() > 0)
          hostConfTable.put(metricPuts);
      }
      metricPuts = calcAndPutSelf(timeTo, timeFrom, ClusterHourTables, ClusterDayTables,
          cluster.keySet());
      if (metricPuts.size() > 0)
        clusterConfTable.put(metricPuts);
      hostConfTable.flushCommits();
      clusterConfTable.flushCommits();
    } finally {
      if (tableIndex != -1)
        triger.add(tableIndex);
    }
    LOG.info("Day summary finished, using time "
        + (System.currentTimeMillis() - timeStarted) + " ms");
  }

  /**
   * Calculates the week summary (from timeTo-week to timeTo) using data from 
   * day tables, and write to week tables.  
   * @param timeTo
   * @throws IOException
   */
  private void weekSummary(long timeTo) throws IOException {
    LOG.info("Week summary starts. ");
    long timeStarted = System.currentTimeMillis();
    long timeFrom = timeTo - BigMonitorConstants.WeekTableInterval;
    HashMap<String, HashSet<String>> cluster = getCluster(timeFrom);
    for (HashSet<String> hosts : cluster.values()) {
      calcAndPut(timeTo, timeFrom, prefix + "HostDay", prefix + "HostWeek", hosts);
    }
    calcAndPut(timeTo, timeFrom, prefix + "ClusterDay", prefix + "ClusterWeek", cluster.keySet());
    LOG.info("Week summary finished, using time "
        + (System.currentTimeMillis() - timeStarted) + " ms");
  }
  
  private void weekSummarySelf(long timeTo) throws IOException {
    LOG.info("Week summary starts. ");
    long timeStarted = System.currentTimeMillis();
    long timeFrom = timeTo - BigMonitorConstants.WeekTableInterval;
    HashMap<String, HashSet<String>> cluster = getClusterSelf(timeFrom);
    for (HashSet<String> hosts : cluster.values()) {
      calcAndPutSelf(timeTo, timeFrom, HostDayTables, HostWeekTables, hosts);
    }
    calcAndPutSelf(timeTo, timeFrom, ClusterDayTables, ClusterWeekTables, cluster.keySet());
    LOG.info("Week summary finished, using time "
        + (System.currentTimeMillis() - timeStarted) + " ms");
  }

  /**
   * Calculates the month summary (from timeTo-month to timeTo) using data from 
   * week tables, and write to month tables.  
   * @param timeTo
   * @throws IOException
   */
  private void monthSummary(long timeTo) throws IOException {
    LOG.info("Month summary starts. ");
    long timeStarted = System.currentTimeMillis();
    long timeFrom = timeTo - BigMonitorConstants.MonthTableInterval;
    HashMap<String, HashSet<String>> cluster = getCluster(timeFrom);
    for (HashSet<String> hosts : cluster.values()) {
      calcAndPut(timeTo, timeFrom, prefix + "HostWeek", prefix + "HostMonth", hosts);
    }
    calcAndPut(timeTo, timeFrom, prefix + "ClusterWeek", prefix + "ClusterMonth", cluster.keySet());
    LOG.info("Month summary finished, using time "
        + (System.currentTimeMillis() - timeStarted) + " ms");
  }
  
  private void monthSummarySelf(long timeTo) throws IOException {
    LOG.info("Month summary starts. ");
    long timeStarted = System.currentTimeMillis();
    long timeFrom = timeTo - BigMonitorConstants.MonthTableInterval;
    HashMap<String, HashSet<String>> cluster = getCluster(timeFrom);
    for (HashSet<String> hosts : cluster.values()) {
      calcAndPutSelf(timeTo, timeFrom, HostWeekTables, HostMonthTables, hosts);
    }
    calcAndPutSelf(timeTo, timeFrom, ClusterWeekTables, ClusterMonthTables, cluster.keySet());
    LOG.info("Month summary finished, using time "
        + (System.currentTimeMillis() - timeStarted) + " ms");
  }

  /**
   * This function dispatch work to day, week or month summary. 
   * @param currTime The current time used to calculate summary start time.
   * @throws IOException
   */
  public void doWork(long currTime) {
    // round currTime to minutes
    long roundedTime = currTime / 1000 / 60 * 60; // seconds
    if (roundedTime % BigMonitorConstants.DayTableInterval == 0)
      try {
        daySummary(roundedTime);
      } catch (Throwable e) {
        LOG.error("Error happens", e);
      }
    else 
      LOG.info("I am up and did nothing.");
    if (roundedTime % BigMonitorConstants.WeekTableInterval == 0)
      try {
        weekSummary(roundedTime);
      } catch (Throwable e) {
        LOG.error("Error happens", e);
      }
    
    if (roundedTime % BigMonitorConstants.MonthTableInterval == 0)
      try {
        monthSummary(roundedTime);
      } catch (Throwable e) {
        LOG.error("Error happens", e);
      }
  }
  
  public void doWorkSelf(long currTime) {
    // round currTime to minutes
    long roundedTime = currTime / 1000 / 60 * 60; // seconds
    if (roundedTime % BigMonitorConstants.DayTableInterval == 0)
      try {
        daySummarySelf(roundedTime);
      } catch (Throwable e) {
        LOG.error("Error happens", e);
      }
    else 
      LOG.info("I am up and did nothing.");
    if (roundedTime % BigMonitorConstants.WeekTableInterval == 0)
      try {
        weekSummarySelf(roundedTime);
      } catch (Throwable e) {
        LOG.error("Error happens", e);
      }
    if (roundedTime % BigMonitorConstants.MonthTableInterval == 0)
      try {
        monthSummarySelf(roundedTime);
      } catch (Throwable e) {
        LOG.error("Error happens", e);
      }
  }

  /**
   * Close HTables 
   * @throws IOException
   */
  public void close() throws IOException {
    mc.close();
    htp.close();
    executor.shutdown();
    for (int i = 0; i < htpConcurrency; ++i) {
      clusterConfTables.get(i).close();
      hostConfTables.get(i).close();
      HostHourTables.get(i).close();
      HostDayTables.get(i).close();
      HostWeekTables.get(i).close();
      HostMonthTables.get(i).close();
      ClusterHourTables.get(i).close();
      ClusterDayTables.get(i).close();
      ClusterWeekTables.get(i).close();
      ClusterMonthTables.get(i).close();
    }
  }

  @Override
  public void run() {
    try {
      doWork(System.currentTimeMillis());
    } catch (Throwable e) {
      LOG.error("Error happens", e);
    }
  }
  
  /**
   * Starts a non-daemon timer and checks time every minutes.
   * This timer will never stop. 
   */
  public void startAndNeverStop() {
    Timer t1 = new Timer(false);
    // start the summarizer once a minute;
    long delay = 60 * 1000 - (System.currentTimeMillis() % (60 * 1000));
    t1.scheduleAtFixedRate(this, delay, 60 * 1000);
    // update metric conf once an hour
    delay = 60 * 60 * 1000 - (System.currentTimeMillis() % (60 * 60 * 1000));
    t1.scheduleAtFixedRate(mc, delay, 60 * 60 * 1000);
  }
  
  public void catchup(long timeFromInMillSec) {
    long timeNow = System.currentTimeMillis();
    for (long t = timeFromInMillSec; t <= timeNow; t += 60*1000) {
      LOG.info("Catch time " + new Date(t));
      doWorkSelf(t);
    }
  }

  public static void main(String argv[]) throws IOException {
    if (argv.length != 1 || !argv[0].equals("-catchup")) {
      printUsage();
      return;
    }
    Configuration conf = new Configuration();
    Summarizer1 worker = new Summarizer1(conf);
    if (argv[0].equalsIgnoreCase("-catchup")) {
      long time = System.currentTimeMillis();
      time = time - 30L*24*3600*1000;
      worker.catchup(time);
      worker.close();
      return;
    }
    //TODO
//    worker.daySummary(1346349600L);
//    worker.startAndNeverStop();
//    worker.run();
    worker.close();
    return;
  }
  
  public static void printUsage(){
    System.err.println("java " + Summarizer1.class.getName() + " -start");
  }

}
