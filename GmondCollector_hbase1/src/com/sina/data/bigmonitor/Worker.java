package com.sina.data.bigmonitor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;

/**
 * This worker gets the xml stream, parses it, records
 * data in a cluster object, and write data to HBase. 
 * 实际收集数据的线程
 * 
 * @author housong
 */
public class Worker implements Runnable {
  public static final Logger LOG = Logger.getLogger(Worker.class.getName());
      
  private boolean running = false;
  private Configuration conf;
  private HashMap<String, Cluster> clusterMap;
  private MetricConf mc = null;
  private InetAddress gmondAddress = null;//gmond ip
  private int gmondPort = 0;//gmond 端口
  private SAXParserFactory spf = null;
  private SAXParser saxParser = null;
  private GmondXMLParser parser = null;//具体解析gmond信息的方法类
  private BlockingQueue<Integer> tigers = null;

  public Worker(Configuration conf, HashMap<String, Cluster> clusterM,
      MetricConf mc, BlockingQueue<Integer> trigers) throws UnknownHostException, ParserConfigurationException,
      SAXException {
    this.conf = conf;
    this.clusterMap = clusterM;
    this.mc = mc;
    this.tigers = trigers;//tigers中有元素就执行收集
    this.running = true;
    init();
  }
  
  public Worker() {
	// TODO Auto-generated constructor stub
  }

  private void init() throws UnknownHostException, ParserConfigurationException, SAXException {
    gmondAddress = InetAddress.getByName(conf.get(BigMonitorConstants.GmondAddressKey));
    gmondPort = conf.getInt(BigMonitorConstants.GmondPortKey, 
        BigMonitorConstants.GmondDefaultPort);
    spf = SAXParserFactory.newInstance();
    saxParser = spf.newSAXParser();
    parser = new GmondXMLParser(conf, clusterMap, mc);
  }
  
  @Override
  public void run() {
    if (LOG.isDebugEnabled())
      LOG.debug("Worker " + Thread.currentThread().getName() + " started");
    while(running){
      try {
        tigers.take(); // wait until coordinator sends signals  取不到元素就阻塞
        long timeStarted = System.currentTimeMillis();
        if (LOG.isDebugEnabled())
          LOG.debug(Thread.currentThread().getName()
              + " gets one triger at time stamp " + timeStarted);
        doWork();//调用解析xml的方法
        long timeLasted = System.currentTimeMillis() - timeStarted;
        if (timeLasted >= BigMonitorConstants.CollectionInterval * 1000)
          LOG.warn(Thread.currentThread().getName()
              + " finishes this round using time " + timeLasted + " ms");
        else 
          LOG.info(Thread.currentThread().getName()
              + " finishes this round using time "
              + timeLasted + " ms");
      } catch (InterruptedException e) {
        LOG.error("This thread should not be interrupted forever.");
        close();
      } catch (Exception e) {
        LOG.error("Error happens", e);
      }
    }
  }
  
  //调用解析xml的方法
  private void doWork(){
    Socket s = null;
    BufferedInputStream bis = null;
    try {
    	LOG.info("Socket:"+gmondAddress+"\t"+ gmondPort);
      s = new Socket(gmondAddress, gmondPort);
      bis = new BufferedInputStream(s.getInputStream());
      saxParser.parse(bis, parser);
    } catch (IOException e) {
      LOG.error("Error happens", e);
    } catch (SAXException e) {
      LOG.error("Error happens", e);
    } catch (Exception e) {
      LOG.error("Error happens", e);
    } finally {
      if (bis != null)
        try {
          bis.close();
        } catch (IOException e) {
          LOG.error("Error happens", e);
        }
      if (s != null)
        try {
          s.close();
        } catch (IOException e) {
          LOG.error("Error happens", e);
        }
    }
  }
  
  public void close() {
    running = false;
  }
}
