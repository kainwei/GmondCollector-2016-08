package com.sina.data.bigmonitor.hbase_monitor.model;


public class Bean {
	RegionServerBean[] RegionServers;
//	List<String> Coprocessors;
	long MasterActiveTime;
	
	
	
public long getMasterActiveTime() {
		return MasterActiveTime;
	}
	public void setMasterActiveTime(long masterActiveTime) {
		MasterActiveTime = masterActiveTime;
	}
	/*	public List<String> getCoprocessors() {
		return Coprocessors;
	}
	public void setCoprocessors(List<String> coprocessors) {
		Coprocessors = coprocessors;
	}*/
	public RegionServerBean[] getRegionServers() {
		return RegionServers;
	}
	public void setRegionServers(RegionServerBean[] regionServers) {
		RegionServers = regionServers;
	}
	
}
