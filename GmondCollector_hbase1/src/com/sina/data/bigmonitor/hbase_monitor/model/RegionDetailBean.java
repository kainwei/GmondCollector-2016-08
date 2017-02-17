package com.sina.data.bigmonitor.hbase_monitor.model;

public class RegionDetailBean {
	int[] name;
	String nameAsString;
	long requestsCount;
	long currentCompactedKVs;
	long memStoreSizeMB;
	long readRequestsCount;
	long rootIndexSizeKB;
	long storefileIndexSizeMB;
	long storefileSizeMB;
	long storefiles;
	long stores;
	long totalCompactingKVs;
	long totalStaticBloomSizeKB;
	long totalStaticIndexSizeKB;
	long version;
	long writeRequestsCount;
	
	public static String [] param_arr={
		"nameAsString",
		"requestsCount",
		"currentCompactedKVs",
		"memStoreSizeMB",
		"readRequestsCount",
		"rootIndexSizeKB",
		"storefileIndexSizeMB",
		"storefileSizeMB",
		"storefiles",
		"stores",
		"totalCompactingKVs",
		"totalStaticBloomSizeKB",
		"totalStaticIndexSizeKB",
		"version",
		"writeRequestsCount"
	} ;
	

	public long getCurrentCompactedKVs() {
		return currentCompactedKVs;
	}

	public void setCurrentCompactedKVs(long currentCompactedKVs) {
		this.currentCompactedKVs = currentCompactedKVs;
	}

	public long getMemStoreSizeMB() {
		return memStoreSizeMB;
	}

	public void setMemStoreSizeMB(long memStoreSizeMB) {
		this.memStoreSizeMB = memStoreSizeMB;
	}

	public long getReadRequestsCount() {
		return readRequestsCount;
	}

	public void setReadRequestsCount(long readRequestsCount) {
		this.readRequestsCount = readRequestsCount;
	}

	public long getRootIndexSizeKB() {
		return rootIndexSizeKB;
	}

	public void setRootIndexSizeKB(long rootIndexSizeKB) {
		this.rootIndexSizeKB = rootIndexSizeKB;
	}

	public long getStorefileIndexSizeMB() {
		return storefileIndexSizeMB;
	}

	public void setStorefileIndexSizeMB(long storefileIndexSizeMB) {
		this.storefileIndexSizeMB = storefileIndexSizeMB;
	}

	public long getStorefileSizeMB() {
		return storefileSizeMB;
	}

	public void setStorefileSizeMB(long storefileSizeMB) {
		this.storefileSizeMB = storefileSizeMB;
	}

	public long getStorefiles() {
		return storefiles;
	}

	public void setStorefiles(long storefiles) {
		this.storefiles = storefiles;
	}

	public long getStores() {
		return stores;
	}

	public void setStores(long stores) {
		this.stores = stores;
	}

	public long getTotalCompactingKVs() {
		return totalCompactingKVs;
	}

	public void setTotalCompactingKVs(long totalCompactingKVs) {
		this.totalCompactingKVs = totalCompactingKVs;
	}

	public long getTotalStaticBloomSizeKB() {
		return totalStaticBloomSizeKB;
	}

	public void setTotalStaticBloomSizeKB(long totalStaticBloomSizeKB) {
		this.totalStaticBloomSizeKB = totalStaticBloomSizeKB;
	}

	public long getTotalStaticIndexSizeKB() {
		return totalStaticIndexSizeKB;
	}

	public void setTotalStaticIndexSizeKB(long totalStaticIndexSizeKB) {
		this.totalStaticIndexSizeKB = totalStaticIndexSizeKB;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public long getWriteRequestsCount() {
		return writeRequestsCount;
	}

	public void setWriteRequestsCount(long writeRequestsCount) {
		this.writeRequestsCount = writeRequestsCount;
	}

	public String getNameAsString() {
		return nameAsString;
	}

	public void setNameAsString(String nameAsString) {
		this.nameAsString = nameAsString;
	}

	public int[] getName() {
		return name;
	}

	public void setName(int[] name) {
		this.name = name;
	}

	public long getRequestsCount() {
		return requestsCount;
	}

	public void setRequestsCount(long requestsCount) {
		this.requestsCount = requestsCount;
	}

	public String getVal(String param){
		String val="";
		if(param.toLowerCase().equals("nameAsString".toLowerCase())){
			val=this.getNameAsString();
		}
				else if(param.toLowerCase().equals("requestsCount".toLowerCase())){
					
					val=String.valueOf(this.getRequestsCount());
				}
				else if(param.toLowerCase().equals("currentCompactedKVs".toLowerCase())){
					val=String.valueOf(this.getCurrentCompactedKVs());
				}
				else if(param.toLowerCase().equals("memStoreSizeMB".toLowerCase())){
					val=String.valueOf(this.getMemStoreSizeMB());
				}
				else if(param.toLowerCase().equals("readRequestsCount".toLowerCase())){
					val=String.valueOf(this.getReadRequestsCount());
				}
				else if(param.toLowerCase().equals("rootIndexSizeKB".toLowerCase())){
					val=String.valueOf(this.getRootIndexSizeKB());
				}
				else if(param.toLowerCase().equals("storefileIndexSizeMB".toLowerCase())){
					val=String.valueOf(this.getStorefileIndexSizeMB());
				}
				else if(param.toLowerCase().equals("storefileSizeMB".toLowerCase())){
					val=String.valueOf(this.getStorefileSizeMB());
				}
				else if(param.toLowerCase().equals("storefiles".toLowerCase())){
					val=String.valueOf(this.getStorefiles());
				}
				else if(param.toLowerCase().equals("stores".toLowerCase())){
					val=String.valueOf(this.getStores());
				}
				else if(param.toLowerCase().equals("totalCompactingKVs".toLowerCase())){
					
					val=String.valueOf(this.getTotalCompactingKVs());
				}
				else if(param.toLowerCase().equals("totalStaticBloomSizeKB".toLowerCase())){
					val=String.valueOf(this.getTotalStaticBloomSizeKB());
					
				}
				else if(param.toLowerCase().equals("totalStaticIndexSizeKB".toLowerCase())){
					val=String.valueOf(this.getTotalStaticIndexSizeKB());
				}
				else if(param.toLowerCase().equals("version".toLowerCase())){
					val=String.valueOf(this.getVersion());
				}
				else if(param.toLowerCase().equals("writeRequestsCount".toLowerCase())){
					val=String.valueOf(this.getWriteRequestsCount());
				}
		
		
		return val;
	}
}
