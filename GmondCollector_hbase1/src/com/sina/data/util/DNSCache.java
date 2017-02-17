package com.sina.data.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

public class DNSCache {
  private static DNSCache instance;
  private ConcurrentHashMap<String, String> host2ip = new ConcurrentHashMap<String, String>();
  
  private DNSCache(){
    
  }
  
  public synchronized static DNSCache getInstance(){
    if(instance == null)
      instance = new DNSCache();
    return instance;
  }
  
  public String getIP(String hostName) throws UnknownHostException{
    String ip = host2ip.get(hostName);
    if (ip == null || ip.equals("")) {
      ip = InetAddress.getByName(hostName).getHostAddress();
      host2ip.put(hostName, ip);
    }
    return ip;
  }
  
  /*public static void main(String args[]) throws UnknownHostException{
    DNSBuffer b = DNSBuffer.getInstance();
    System.out.println(b.getIP("www.baidu.com"));
  }*/
}
