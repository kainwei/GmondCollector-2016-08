package com.sina.data.util;

import com.sina.data.util.xmlParser.GmondXMLParser;
import org.apache.log4j.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.Socket;


/**
 * 
 * 获取ganglia的xml数据工具  结果写到get_ganglia_"+gmondAddress+".txt下面
 * 
 * @author jingyi4
 */
public class GetGangLiaXml  {
  public static final Logger LOG = Logger.getLogger(GetGangLiaXml.class.getName());
      
 


  
  //调用解析xml的方法
  public static void main(String args[]){
    Socket s = null;
    BufferedInputStream bis = null;
    try {
    	String gmondAddress="10.39.5.27";
    	int gmondPort=8649;
    	System.out.println("Socket:"+gmondAddress+"\t"+ gmondPort);
    	 s = new Socket(gmondAddress, gmondPort);
      bis = new BufferedInputStream(s.getInputStream());
      
       SAXParserFactory spf = null;
       SAXParser saxParser = null;
       GmondXMLParser parser = null;//具体解析gmond信息的方法类
       PrintWriter pw=new PrintWriter(new OutputStreamWriter(
    		    new FileOutputStream("get_ganglia_"+gmondAddress+".txt")), true);
       parser = new GmondXMLParser(pw);
      
      spf = SAXParserFactory.newInstance();
      saxParser = spf.newSAXParser();
      
      saxParser.parse(bis, parser);
      
      
      
 //     GetGangLiaXml.sys2File(bis);
      saxParser.parse(bis, parser);
      
      bis.close(); 
    } catch (IOException e) {
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
  
  public static void sysout(BufferedInputStream bis) throws IOException{
      do{
    	  System.out.print((char)bis.read());  
      }while(bis.available() > 0);
  }
  
  public static void sys2File(BufferedInputStream bis) throws IOException{
	  BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("aa.txt"));
	  int i=0;
	  do{  
          i = bis.read();  
          if(i != -1){  
              bos.write(i);  
          }  
      }while(i != -1);  
	  
	  bos.close();
  }
  

}
