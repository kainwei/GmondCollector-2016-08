package com.sina.data.bigmonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import com.sina.data.bigmonitor.metric.MetricConf;

/*
 * 管理收集 数据线程 与 更新内存中的metric 的类
 * 
 * 
 */

public class Coordinator {
  public static final Logger LOG = Logger.getLogger(Coordinator.class.getName());
  private boolean isAlive = false;//启动置为true ，停止则置为false
  private Configuration conf = null;
  private int handlerCount = 0;//启动几个线程来收集数据
  private int mcUpdateInterval = -1;//多长时间更新一次内存中的metric
  private MetricConf mc = null;
  private ArrayList<Thread> workers = null;
  private HashMap<String, Cluster> clusters = null;
  private BlockingQueue<Integer> tigers = null;
  private Timer updateTimer = null;
  private Timer workTimer = null;

  public Coordinator(Configuration conf) throws IOException, ParserConfigurationException, SAXException {
    this.conf = conf;
    this.handlerCount = conf.getInt(BigMonitorConstants.HandlerCountKey,
        BigMonitorConstants.HandlerDefaultCount);
    this.mcUpdateInterval = conf.getInt(BigMonitorConstants.MetricConfUpdateKey, 
        BigMonitorConstants.MetricConfDefaultUpdate);// 1 hour
    tigers = new LinkedBlockingQueue<Integer>();//默认15s往tigers中加入一个数，worker线程看到如果有数据就取出来，并执行获取ganglia数据的操作
    updateTimer = new Timer(true);//每小时更新一次metricConf
    workTimer = new Timer(true);//start()中每15s往tigers中加入一个数
    //参数初始化结束----
    init();
  }
  
  private void init() throws IOException, ParserConfigurationException, SAXException {
    mc = new MetricConf(conf);//获取hbase表中配置的metric,可以根据metric
    clusters = new HashMap<String, Cluster>();
    workers = new ArrayList<Thread>(handlerCount);//实例化若干线程进行数据收集，防止有的线程时间过长或者阻塞，超过15s
    for (int i = 0; i < handlerCount; ++i){
      workers.add(new Thread(new Worker(conf, clusters, mc, tigers), "Worker "+i));
    }
  }

  public void start() {
    for (Thread t : workers)
      t.start();
    long delay = mcUpdateInterval - System.currentTimeMillis() % (mcUpdateInterval);
    updateTimer.scheduleAtFixedRate(mc, mcUpdateInterval, mcUpdateInterval);
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
    }, delay, BigMonitorConstants.CollectionInterval*1000);//每15s执行一次
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
