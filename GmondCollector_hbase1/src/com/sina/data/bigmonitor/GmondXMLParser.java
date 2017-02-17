package com.sina.data.bigmonitor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;
import com.sina.data.bigmonitor.metric.MetricDescriptor;
import com.sina.data.util.DNSCache;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GmondXMLParser extends DefaultHandler {
  public static final Logger LOG = Logger.getLogger(GmondXMLParser.class.getName());
  private Configuration conf = null;
  private HashMap<String, Cluster> clusterMap = null;//集群名 对应的 Cluster对象
  private MetricConf mc = null;
  private Cluster currCluster = null;
  private Host currHost = null;
  private String currentClusterName = null;
  private String currentHostIP = null;
  private String currentMetricName = null;

  public GmondXMLParser(Configuration c, HashMap<String, Cluster> cm,
      MetricConf mc) {
    this.conf = c;
    this.clusterMap = cm;
    this.mc = mc;
  }

  public void startDocument() throws SAXException {
  }

  public void endDocument() throws SAXException {
  }

  
  //解析xml
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    if ("GANGLIA_XML".equalsIgnoreCase(qName)) {
      // XML start point, ignore for now
    } else if ("CLUSTER".equalsIgnoreCase(qName)) {
      currentClusterName = attributes.getValue("NAME");
   //   LOG.info("collector currentClusterName"+currentClusterName);
      synchronized (clusterMap) {
        currCluster = clusterMap.get(currentClusterName);
        if (currCluster == null) {
          try {
            currCluster = new Cluster(currentClusterName, mc, conf);
            clusterMap.put(currentClusterName, currCluster);
          } catch (IOException e) {
            LOG.error("Error happens", e);
            return;
          }
        }
      }
      currCluster
          .setLocalTime(Long.parseLong(attributes.getValue("LOCALTIME")));
    } else if ("HOST".equalsIgnoreCase(qName)) {
      currentHostIP = attributes.getValue("IP").trim();
      if (currentHostIP == null || currentHostIP.length() == 0) {
        String currHostName = attributes.getValue("NAME").trim();
        try {
          currentHostIP = DNSCache.getInstance().getIP(currHostName);
        } catch (UnknownHostException e) {
          LOG.error("Error happens", e);
          return;
        }
      }
      currHost = currCluster.getHost(currentHostIP);//根据host的ip从hashmap中获取ip对应的host对象
      currHost.setLastReport(Long.parseLong(attributes.getValue("REPORTED")));//这是一个时间戳格式数据
    } else if ("METRIC".equalsIgnoreCase(qName)) {
      currentMetricName = attributes.getValue("NAME");
      MetricDescriptor md = mc.getDescriptor(currentMetricName);
      if (md == null)
        return;
      long ID = md.getID();
      long tn = Long.parseLong(attributes.getValue("TN"));
      long tmax = Long.parseLong(attributes.getValue("TMAX"));
//      if (tn <= tmax) {
      if (true) {
        currHost.addMetricValue(ID, attributes.getValue("VAL"),
            attributes.getValue("TYPE"));//attributes.getValue("TYPE")存储的是数据类型
      }
    } else if ("EXTRA_DATA".equalsIgnoreCase(qName)) {
      // ignore for now
    } else if ("EXTRA_ELEMENT".equalsIgnoreCase(qName)) {
      // ignore for now
    } else {
      System.err.println("Got malformatted xml elemetn. " + qName + " "
          + attributes);
    }
  }

  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if ("GANGLIA_XML".equalsIgnoreCase(qName)) {

    } else if ("CLUSTER".equalsIgnoreCase(qName)) {
      try {
        currCluster.thisRoundFinished(true);//执行入hbase表的流程
      } catch (IOException e) {
        LOG.error("Error happens", e);
      }
    } else if ("HOST".equalsIgnoreCase(qName)) {

    } else if ("METRIC".equalsIgnoreCase(qName)) {

    } else if ("EXTRA_DATA".equalsIgnoreCase(qName)) {

    } else if ("EXTRA_ELEMENT".equalsIgnoreCase(qName)) {

    } else {

    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
  }

  public static void main(String[] args) throws IOException {
    Configuration.addDefaultResource(BigMonitorConstants.CONFFILE);
    SAXParserFactory spf = SAXParserFactory.newInstance();
    Configuration conf = new Configuration();
    HashMap<String, Cluster> clusterMap = new HashMap<String, Cluster>();
    MetricConf mc = new MetricConf(conf);
    GmondXMLParser xp = new GmondXMLParser(conf, clusterMap, mc);
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    try {
      
      SAXParser saxParser = spf.newSAXParser();
      long timestarted = System.currentTimeMillis();
      fis = new FileInputStream("metricdump.xml");
      bis = new BufferedInputStream(fis);
      saxParser.parse(bis, xp);
      bis.close();
      fis.close();
      
//      fis = new FileInputStream("tests/sample2.xml");
//      bis = new BufferedInputStream(fis);
//      saxParser.parse(bis, xp);
//      bis.close();
//      fis.close();
//      
//      fis = new FileInputStream("tests/sample3.xml");
//      bis = new BufferedInputStream(fis);
//      saxParser.parse(bis, xp);
//      bis.close();
//      fis.close();
//      
//      fis = new FileInputStream("tests/sample4.xml");
//      bis = new BufferedInputStream(fis);
//      saxParser.parse(bis, xp);

      System.out.println("Used " + (System.currentTimeMillis() - timestarted));
//      for (Cluster c : clusterMap.values()){
//        System.out.println(c);
//        System.out.println();
//      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bis.close();
      fis.close();
      mc.close();
      for (Cluster c : clusterMap.values()){
        c.close();
      }
    }
    // System.out.print(xp.toString());
  }

}