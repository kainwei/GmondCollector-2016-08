package com.sina.data.bigmonitor.collector_hbase;

import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;


import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import com.sina.data.bigmonitor.Worker;
import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.hbase_monitor.Parser.HBaseJsonParser;
import com.sina.data.bigmonitor.metric.MetricConf;

/**
 * This worker gets the xml stream, parses it, records
 * data in a cluster object, and write data to HBase. 
 * @author housong
 */
public class Worker_HBase extends Worker {
  public static final Logger LOG = Logger.getLogger(Worker_HBase.class.getName());
      
  private boolean running = false;
  private Configuration conf;
  private MetricConf mc = null;
  private BlockingQueue<Integer> tigers = null;
  private String hbase_master_jmx_url="";
  private HBaseJsonParser hbaseJsonParser=null;

  public Worker_HBase(Configuration conf, MetricConf mc, BlockingQueue<Integer> trigers) throws UnknownHostException {
	  super();
	  this.conf = conf;
	  this.mc = mc;
	  this.tigers = trigers;
	  this.running = true;
	  init();
	  LOG.info("Worker_HBase init end...");
 }
  
  private void init() {
	  hbase_master_jmx_url =conf.get(BigMonitorConstants.HBaseMasterJmxUrl);//获取hbase_master:60010/jmx
	  hbaseJsonParser = new HBaseJsonParser(conf,  mc,hbase_master_jmx_url);
  }
  
  @Override
  public void run() {
    if (LOG.isDebugEnabled())
      LOG.debug("Worker " + Thread.currentThread().getName() + " started");
    while(running){
      try {
        tigers.take(); // wait until coordinator sends signals
        long timeStarted = System.currentTimeMillis();
        if (LOG.isDebugEnabled())
          LOG.debug(Thread.currentThread().getName()
              + " gets one triger at time stamp " + timeStarted);
        doWork();
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
  
  private void doWork(){
	  hbaseJsonParser.Parser();
  }
  
  public void close() {
    running = false;
  }
}
