package com.sina.data.bigmonitor.web;

import com.google.gson.Gson;
import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;
import com.sina.data.bigmonitor.metric.MetricDescriptor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Timer;

public class GetdataServiceServelet extends HttpServlet {

  static {

    Configuration.addDefaultResource(BigMonitorConstants.CONFFILE);
   // PropertyConfigurator.configure("conf/BigMonitorSetting.xml");
    PropertyConfigurator.configure("conf/log4j.properties");
  }

  public static final Logger LOG = Logger
      .getLogger(GetdataServiceServelet.class.getName());

  private static final long serialVersionUID = 4201360440820033500L;
  private HTablePool pool;
  private ClustersJson clustersJson = null;
  private static MetricConf mc = null;
  private static Configuration conf = null;
  Gson gson = new Gson();

  public static synchronized MetricConf getMetricConf() {
    if (mc == null) {
      try {
        mc = new MetricConf(conf);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(mc, 60 * 60 * 1000, 60 * 60 * 1000);
      } catch (Exception e) {
        LOG.error("Error", e);
      }
    }
    return mc;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    LOG.info(request.getRemoteAddr() + " requests page "
        + request.getRequestURL().append('?').append(request.getQueryString()));
    String method = request.getParameter("method");
    if (method == null || method.equals("")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          "Should provide enough parameters");
      return;
    }
    response.addDateHeader("Last-Modified", System.currentTimeMillis());

    try {
      if (method.equalsIgnoreCase("getClusters"))
        getClusters(request, response);
      else if (method.equalsIgnoreCase("getClusterInfo"))
        getClusterInfo(request, response);
      else if (method.equalsIgnoreCase("getHostInfo"))
        getHostInfo(request, response);
      else if (method.equalsIgnoreCase("getHostMetric"))
        getHostMetric(request, response);
      else if (method.equalsIgnoreCase("getClusterMetric"))
        getClusterMetric(request, response);
      else if (method.equalsIgnoreCase("getClusterMetricRanking"))
        getClusterMetricRanking(request, response);
      else {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      }
    } catch (IOException e) {
      LOG.error("Error", e);
      throw e;
    } finally {
      response.flushBuffer();
    }
  }

  private void getClusterMetricRanking(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    if (System.currentTimeMillis() - request.getDateHeader("If-Modified-Since") < 60 * 1000) {
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }
    String jsoncallback = request.getParameter("jsoncallback");
    ServletOutputStream sos = response.getOutputStream();
    String name = request.getParameter("clustername");
    String metricName = request.getParameter("metric");
    if (name == null || name.equals("") || metricName == null
        || metricName.equals("")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          "Should provide enough parameters");
      sos.close();
      return;
    }
    synchronized (this) {
      if (clustersJson == null) {
        clustersJson = new ClustersJson(conf);
      }
    }
    ClusterInfoJson cij = clustersJson.getClusterInfoJson(name);
    if (cij == null){
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          "This cluster does not exist");
      sos.close();
      return;
    }
      
    ClusterMetricRankingJson cmrj = new ClusterMetricRankingJson(name,
        metricName);
    long mId = mc.getDescriptor(metricName).getID();
    long time = (System.currentTimeMillis() / 1000 - 60) / 60 * 60;
    String prefix = conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
        BigMonitorConstants.MetricDataTableDefaultPrefix);
    String tableName = prefix + "HostHour";
    byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.DataTableCF);
    byte[] qBytes = Bytes.toBytes(BigMonitorConstants.DataTableQa);
    List<Get> gets = new ArrayList<Get>();
    for (String host : cij.getHosts()) {
      gets.add(new Get(Bytes.toBytes(host + "_" + mId + "_" + time)));
    }
    HTableInterface h = null;
    try {
      h = pool.getTable(tableName);
      Result[] results = h.get(gets);
      for (Result r : results) {
        byte[] b = r.getRow();
        if (b == null)
          continue;
        String row = Bytes.toString(b);
        String host = row.substring(0, row.indexOf('_'));
        b = r.getValue(cfBytes, qBytes);
        if (b == null)
          continue;
        String valueString = Bytes.toString(b);
        try {
          Double v = Double.parseDouble(valueString);
          cmrj.add(host, v);
        } catch (NumberFormatException e) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Metrics not comparable");
          sos.close();
          return;
        }
      }
      cmrj.finishedAdding();
      sos.print(jsoncallback + "(" + gson.toJson(cmrj) + ")");
      sos.close();
    } catch (IOException e) {
      LOG.error("Error", e);
      throw e;
    } finally {
      if (h != null)
        h.close();
    }
  }

  private void getClusterMetric(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    if (request.getDateHeader("If-Modified-Since") > 0) {
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }
    String jsoncallback = request.getParameter("jsoncallback");
    ServletOutputStream sos = response.getOutputStream();
    String name = request.getParameter("clustername");
    String metricName = request.getParameter("metric");
    String timeFrom = request.getParameter("timeFrom");
    String timeTo = request.getParameter("timeTo");
    if (name == null || name.equals("") || metricName == null
        || metricName.equals("") || timeFrom == null || timeFrom.equals("")
        || timeTo == null || timeTo.equals("")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          "Should provide enough parameters");
      sos.close();
      return;
    }
    long tFrom = -1;
    long tTo = -1;
    try {
      tFrom = Long.parseLong(timeFrom);
      tTo = Long.parseLong(timeTo);
    } catch (NumberFormatException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Time should be Unix time stamp in long");
      sos.close();
      return;
    }
    if (tTo <= tFrom) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          "timeFrom should be smaller than timeTo");
      sos.close();
      return;
    }
    MetricDescriptor md = getMetricConf().getDescriptor(metricName);
    if (md == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "The metric is not found");
      sos.close();
      return;
    }
    String metricTableName = null;
    String prefix = conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
        BigMonitorConstants.MetricDataTableDefaultPrefix);
    long interval = (tTo - tFrom) / 60;
    if (interval <= BigMonitorConstants.CollectionInterval) {
      metricTableName = prefix + "ClusterOrig";
    } else if (interval <= BigMonitorConstants.HourTableInterval) {
      metricTableName = prefix + "ClusterHour";
    } else if (interval <= BigMonitorConstants.DayTableInterval) {
      metricTableName = prefix + "ClusterDay";
    } else if (interval <= BigMonitorConstants.WeekTableInterval) {
      metricTableName = prefix + "ClusterWeek";
    } else {
      metricTableName = prefix + "ClusterMonth";
    }
    HTableInterface h = pool.getTable(metricTableName);
    try {
      List<String> metricValues = new ArrayList<String>();
      List<String> metricTimes = new ArrayList<String>();
      getMetricDataFromHBase(h, name, md, tTo, tFrom, metricValues, metricTimes);
      if (metricValues == null || metricTimes == null) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      } else {
        ClusterMetricJson hmj = new ClusterMetricJson(name, metricName,
            metricValues, metricTimes);
        sos.print(jsoncallback + "(" + gson.toJson(hmj) + ")");
      }
      sos.flush();
    } catch (Exception e) {
      LOG.error("Error", e);
    } finally {
      sos.close();
      h.close();
    }
  }

  private void getHostMetric(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    if (request.getDateHeader("If-Modified-Since") > 0) {
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }
    String jsoncallback = request.getParameter("jsoncallback");
    ServletOutputStream sos = response.getOutputStream();
    String name = request.getParameter("hostname");
    String metricName = request.getParameter("metric");
    String timeFrom = request.getParameter("timeFrom");
    String timeTo = request.getParameter("timeTo");
    if (name == null || name.equals("") || metricName == null
        || metricName.equals("") || timeFrom == null || timeFrom.equals("")
        || timeTo == null || timeTo.equals("")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Should provide enough parameters");
      sos.close();
      return;
    }
    long tFrom = -1;
    long tTo = -1;
    try {
      tFrom = Long.parseLong(timeFrom);
      tTo = Long.parseLong(timeTo);
    } catch (NumberFormatException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Time should be Unix time stamp in long");
      sos.close();
      return;
    }
    if (tTo <= tFrom) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "timeFrom should be smaller than timeTo");
      sos.close();
      return;
    }
    MetricDescriptor md = getMetricConf().getDescriptor(metricName);
    if (md == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          "The metric is not found");
      sos.close();
      return;
    }
    String metricTableName = null;
    String prefix = conf.get(BigMonitorConstants.MetricDataTablePrefixKey,
        BigMonitorConstants.MetricDataTableDefaultPrefix);
    long interval = (tTo - tFrom) / 60;
    if (interval <= BigMonitorConstants.HourTableInterval) {
      metricTableName = prefix + "HostOrig";
    } else if (interval <= BigMonitorConstants.DayTableInterval) {
      metricTableName = prefix + "HostHour";
    } else if (interval <= BigMonitorConstants.WeekTableInterval) {
      metricTableName = prefix + "HostDay";
    } else if (interval <= BigMonitorConstants.MonthTableInterval) {
      metricTableName = prefix + "HostWeek";
    } else {
      metricTableName = prefix + "HostMonth";
    }
    HTableInterface h = pool.getTable(metricTableName);
    try {
      List<String> metricValues = new ArrayList<String>();
      List<String> metricTimes = new ArrayList<String>();
      getMetricDataFromHBase(h, name, md, tTo, tFrom, metricValues, metricTimes);
      if (metricValues == null || metricTimes == null) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No such host");
      } else {
        HostMetricJson hmj = new HostMetricJson(name, metricName, metricValues,
            metricTimes);
        sos.print(jsoncallback + "(" + gson.toJson(hmj) + ")");
      }
      sos.flush();
    } catch (Exception e) {
      LOG.error("Error", e);
    } finally {
      sos.close();
      h.close();
    }
  }

  private void getMetricDataFromHBase(HTableInterface h, String host,
      MetricDescriptor md, long tTo, long tFrom, List<String> metricValues,
      List<String> metricTimes) throws IOException {
    byte[] cfBytes = Bytes.toBytes(BigMonitorConstants.DataTableCF);
    byte[] qBytes = Bytes.toBytes(BigMonitorConstants.DataTableQa);
    String idString = Long.toString(md.getID());
    Scan sc = new Scan(Bytes.toBytes(host + "_" + idString + "_" + tFrom),
        Bytes.toBytes(host + "_" + idString + "_" + tTo));
    ResultScanner rs = h.getScanner(sc);
    for (Result r : rs) {
      String row = Bytes.toString(r.getRow());
      String tsString = row.substring(row.lastIndexOf('_') + 1, row.length());
      metricTimes.add(tsString);
      String v = Bytes.toString(r.getValue(cfBytes, qBytes));
      metricValues.add(v);
    }
  }

  private void getHostInfo(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    if (System.currentTimeMillis() - request.getDateHeader("If-Modified-Since") < 5 * 60 * 1000) {
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }
    String jsoncallback = request.getParameter("jsoncallback");
    String name = request.getParameter("hostname");
    if (name == null || name.equals("")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          "Should provide enough parameters");
      return;
    }
    Get get = new Get(Bytes.toBytes(name));
    String hostConfTableName = conf.get(BigMonitorConstants.HostConfTableKey,
        BigMonitorConstants.HostConfDefaultTableName);
    HTableInterface table = pool.getTable(hostConfTableName);
    Result r = null;
    ServletOutputStream sos = response.getOutputStream();
    try {
      r = table.get(get);
      NavigableMap<byte[], byte[]> m = r.getFamilyMap(Bytes
          .toBytes(BigMonitorConstants.HostConfTableCF1));
      if (m.size() > 0) {
        List<String> metricNames = new ArrayList<String>();
        for (byte[] k : m.keySet()) {
          Long id = Long.parseLong(Bytes.toString(k));
          MetricDescriptor md = getMetricConf().getDescriptor(id);
          if (md == null)
            continue;
          metricNames.add(md.getType() + "#" + md.getName());
        }
        HostInfoJson hij = new HostInfoJson(name, metricNames);
        sos.print(jsoncallback + "(" + gson.toJson(hij) + ")");
      } else {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "No such host");
      }
      sos.flush();
    } catch (Exception e) {
      LOG.error("Error", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } finally {
      sos.close();
      table.close();
    }
  }

  private void getClusterInfo(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    if (System.currentTimeMillis() - request.getDateHeader("If-Modified-Since") < 5 * 60 * 1000) {
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }
    String name = request.getParameter("clustername");
    if (name == null || name.equals("")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          "Should provide enough parameters");
      return;
    }
    String jsoncallback = request.getParameter("jsoncallback");
    synchronized (this) {
      if (clustersJson == null) {
        clustersJson = new ClustersJson(conf);
      }
    }
    ServletOutputStream sos = response.getOutputStream();
    try {
      clustersJson.update(pool);
      ClusterInfoJson cij = clustersJson.getClusterInfoJson(name);
      if (cij == null) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "No such cluster");
      } else
        sos.print(jsoncallback + "(" + gson.toJson(cij) + ")");
      sos.flush();
    } catch (IOException e) {
      LOG.error("Error", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
          e.getStackTrace().toString());
    } finally {
      sos.close();
    }
  }

  private void getClusters(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    if (System.currentTimeMillis() - request.getDateHeader("If-Modified-Since") < 5 * 60 * 1000) {
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }
    String jsoncallback = request.getParameter("jsoncallback");
    synchronized (this) {
      if (clustersJson == null) {
        clustersJson = new ClustersJson(conf);
      }
    }
    ServletOutputStream sos = response.getOutputStream();
    try {
      clustersJson.update(pool);
      sos.print(jsoncallback + "(" + gson.toJson(clustersJson) + ")");
      sos.flush();
    } catch (Exception e) {
      LOG.error("Error", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          e.getStackTrace().toString());
    } finally {
      sos.close();
    }
  }

  @Override
  public void destroy() {
    try {
      pool.close();
    } catch (IOException e) {
      LOG.error("Error", e);
    }
    try {
      mc.close();
    } catch (IOException e) {
      LOG.error("Error", e);
    }
    super.destroy();
  }

  @Override
  public void init() throws ServletException {
    conf = new Configuration();
    pool = new HTablePool(conf, 30);
    getMetricConf();
    super.init();
  }
  public static void main(String[] args){

  }

}