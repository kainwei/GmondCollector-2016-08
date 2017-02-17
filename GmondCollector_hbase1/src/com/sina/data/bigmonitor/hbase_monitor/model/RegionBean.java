package com.sina.data.bigmonitor.hbase_monitor.model;

public class RegionBean {
int [] key;
RegionDetailBean value;
public int[] getKey() {
	return key;
}
public void setKey(int[] key) {
	this.key = key;
}
public RegionDetailBean getValue() {
	return value;
}
public void setValue(RegionDetailBean value) {
	this.value = value;
}

}
