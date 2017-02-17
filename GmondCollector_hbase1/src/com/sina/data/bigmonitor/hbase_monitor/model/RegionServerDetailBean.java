package com.sina.data.bigmonitor.hbase_monitor.model;

public class RegionServerDetailBean {
String [] coprocessors;
int load;
RegionBean [] regionsLoad;
public String[] getCoprocessors() {
	return coprocessors;
}
public void setCoprocessors(String[] coprocessors) {
	this.coprocessors = coprocessors;
}
public RegionBean[] getRegionsLoad() {
	return regionsLoad;
}
public void setRegionsLoad(RegionBean[] regionsLoad) {
	this.regionsLoad = regionsLoad;
}
public int getLoad() {
	return load;
}
public void setLoad(int load) {
	this.load = load;
}

}
