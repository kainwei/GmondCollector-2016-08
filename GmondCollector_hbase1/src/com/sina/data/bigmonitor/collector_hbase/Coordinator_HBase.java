package com.sina.data.bigmonitor.collector_hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;

public class Coordinator_HBase {
  public static final Logger LOG = Logger.getLogger(Coordinator_HBase.class.getName());
  private boolean isAlive = false;
  private Configuration conf = null;
  private int handlerCount = 0;
  private int mcUpdateInterval = -1;
  private MetricConf mc = null;
  private ArrayList<Thread> workers = null;
  private BlockingQueue<Integer> tigers = null;
  private Timer updateTimer = null;
  private Timer workTimer = null;

  public Coordinator_HBase(Configuration conf) throws IOException, ParserConfigurationException {
    this.conf = conf;
    this.handlerCount = conf.getInt(BigMonitorConstants.HandlerCountKey,
        BigMonitorConstants.HandlerDefaultCount);
    this.mcUpdateInterval = conf.getInt(BigMonitorConstants.MetricConfUpdateKey, 
        BigMonitorConstants.MetricConfDefaultUpdate);
    tigers = new LinkedBlockingQueue<Integer>();
    updateTimer = new Timer(true);
    workTimer = new Timer(true);
    init();
  }
  
  private void init() throws IOException {
    mc = new MetricConf(conf);
    workers = new ArrayList<Thread>(handlerCount);
    for (int i = 0; i < handlerCount; ++i){
      workers.add(new Thread(new Worker_HBase(conf,  mc, tigers), "Worker "+i));
    }
  }

  public void start() {
    for (Thread t : workers)
      t.start();
    long delay = mcUpdateInterval - System.currentTimeMillis() % (mcUpdateInterval);
    updateTimer.scheduleAtFixedRate(mc, mcUpdateInterval, mcUpdateInterval);//隔段时间更新一次mc
    delay = (BigMonitorConstants.CollectionInterval * 1000) - 
        System.currentTimeMillis() % (BigMonitorConstants.CollectionInterval*1000);
    workTimer.scheduleAtFixedRate(new TimerTask(){
      @Override
      public void run() {
        try {
          tigers.put(new Integer(0));
        } catch (InterruptedException e) {
        }
      }
    }, delay, BigMonitorConstants.CollectionInterval*1000);
    isAlive = true;
  }

  public void stop() {
    workTimer.cancel();
    updateTimer.cancel();
    for (Thread t : workers){
      t.interrupt();
      try {
        t.join();
      } catch (InterruptedException e) {
        LOG.error("Error happens", e);
      }
    }
    isAlive = false;
  }
  
  public boolean isAlive(){
    return isAlive;
  }
}
