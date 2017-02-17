package com.sina.data.bigmonitor.cli;

import com.sina.data.bigmonitor.Coordinator;
import com.sina.data.bigmonitor.Summarizer1;
import com.sina.data.bigmonitor.collector_hbase.Coordinator_HBase;
import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.web.HttpServer;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

public class GmondCollectorManager {
  
  static{
    Configuration.addDefaultResource(BigMonitorConstants.CONFFILE);
    PropertyConfigurator.configure("conf/log4j.properties");
  }
  
  public static final Logger LOG = Logger.getLogger(GmondCollectorManager.class.getName());
  
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      printUsage();
      return;
    }
    if (args[0].equalsIgnoreCase("-startCollector"))//启动数据收集，从ganglia收集数据到hbase
      startCollector();
    else if (args[0].equalsIgnoreCase("-startSummarizer"))
      startSummarizer();
    else if (args[0].equalsIgnoreCase("-startWeb"))
      startWeb();
    else if (args[0].equalsIgnoreCase("-startHBaseCollector"))
    	startHBaseCollector();
  }

  private static void startWeb() {
    /*try {
      Configuration conf = new Configuration();
      String bindAddress = conf.get(BigMonitorConstants.WebAddressKey, "0.0.0.0");
      int port = conf.getInt(BigMonitorConstants.WebPortKey, 8080);
      HttpServer server = new HttpServer(bindAddress, port, "get");
      server.addInternalServlet("get", "/get", GetdataServiceServelet.class);
      server.start();
    } catch (Exception e) {
      LOG.error("Error", e);
    }*/
      System.out.println("from here");
      HttpServer infoServer = null;
      int tmpInfoPort = 8080;
      //String infoHost = "szwg-hadoop-con1.szwg01";
      String infoHost = "0.0.0.0";
      Configuration conf = new Configuration();
      conf.addResource("../conf/hadoop-site.xml");
      try {
          infoServer = new HttpServer("tentacles", infoHost, tmpInfoPort, tmpInfoPort == 0, conf);
          infoServer.start();
      } catch (IOException e) {
          e.printStackTrace();
      }
      //infoServer.addServlet("HelloForm","/HelloForm",HelloForm.class);
//        addServlet(String name, String pathSpec,
//                Class<? extends HttpServlet > clazz)

  }

  private static void printUsage() {
    System.err.println("Use -startCollector to start collector");
    System.err.println("Use -startSummarizer to start summarizer");
    System.err.println("Use -startWeb to start web");
    System.err.println("Use -startHBaseCollector to start HBase Collector");
  }
  
  public static void startCollector() {
    Configuration conf = new Configuration();
    Coordinator co = null;
    try {
      co = new Coordinator(conf);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    co.start();
    while (true) {
      if (!co.isAlive())
        break;
      try {
        Thread.sleep(15 * 1000);
      } catch (InterruptedException e) {
        co.stop();
      }
    }
  }
  
  public static void startHBaseCollector() {
	    Configuration conf = new Configuration();
	    Coordinator_HBase co = null;
	    try {
	      co = new Coordinator_HBase(conf);
	    } catch (Exception e) {
	      e.printStackTrace();
	      return;
	    }
	    co.start();
	    while (true) {
	      if (!co.isAlive())
	        break;
	      try {
	        Thread.sleep(15 * 1000);
	      } catch (InterruptedException e) {
	        co.stop();
	      }
	    }
  }

  public static void startSummarizer() {
    Configuration conf = new Configuration();
    try {
      Summarizer1 worker = new Summarizer1(conf);
      worker.startAndNeverStop();
      return;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
