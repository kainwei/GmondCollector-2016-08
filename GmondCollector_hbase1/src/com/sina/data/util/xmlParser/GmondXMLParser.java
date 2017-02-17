package com.sina.data.util.xmlParser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
  private MetricConf mc = null;
  private String currentClusterName = null;
  private String currentHostIP = null;
  private String currentMetricName = null;
  PrintWriter pw=null;
  
  String ip="";

  public GmondXMLParser( PrintWriter pw) {
	  this.pw=pw;
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
      System.out.println(currentClusterName);
      pw.println("CLUSTER name:"+currentClusterName);
      pw.println("CLUSTER time:"+attributes.getValue("LOCALTIME"));
    } else if ("HOST".equalsIgnoreCase(qName)) {
    	ip=attributes.getValue("IP").trim();
//    	if(attributes.getValue("IP").trim().equals("10.39.3.63")){
    	      pw.println("===============================");
    	      pw.println("HOST IP:"+attributes.getValue("IP").trim());
    	      pw.println("HOST NAME:"+attributes.getValue("NAME").trim());
    	      pw.println("HOST REPORTED:"+attributes.getValue("REPORTED"));
    		
//    	}
      
      
      
    } else if ("METRIC".equalsIgnoreCase(qName)) {
//      if(ip.equals("10.39.3.63")){
    	     pw.println("METRIC NAME:"+attributes.getValue("NAME").trim());
    	     //   pw.println("METRIC TN:"+attributes.getValue("TN").trim());
    	     //   pw.println("METRIC TMAX:"+attributes.getValue("TMAX"));
    	        pw.println("METRIC VAL:"+attributes.getValue("VAL"));
    	        pw.println("METRIC TYPE:"+attributes.getValue("TYPE"));
    	  
//      }
 
      
      
    } else if ("EXTRA_DATA".equalsIgnoreCase(qName)) {
      // ignore for now
    } else if ("EXTRA_ELEMENT".equalsIgnoreCase(qName)) {
      // ignore for now
    } else {
      System.err.println("Got malformatted xml elemetn. " + qName + " "
          + attributes);
    }
  }



}